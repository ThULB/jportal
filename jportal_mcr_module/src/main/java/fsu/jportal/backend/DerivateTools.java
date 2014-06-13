package fsu.jportal.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileMetadataManager;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.services.MCRIView2Tools;

import com.google.common.io.Files;

import fsu.jportal.util.DerivatePath;

public class DerivateTools {
    static Logger LOGGER = Logger.getLogger(DerivateTools.class);
    
    private static void cp(MCRFile source, MCRDirectory newParentDir, String newFileName) {
        // assume source and sink are not null
        if (newFileName == null) {
            newFileName = source.getName();
        }

        if (newParentDir.hasChild(newFileName)) {
            LOGGER.info("cp: " + newFileName + " allready exists (not copied)");
            return;
        }

        MCRFile newFile = new MCRFile(newFileName, newParentDir);
        try {
            newFile.setContentFrom(source.getContentAsInputStream());
        } catch (MCRPersistenceException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private static void cp(MCRDirectory sourceDir, MCRDirectory newParentDir, String newDirName) {
        if (newDirName == null) {
            newDirName = sourceDir.getName();
        }

        MCRDirectory newDir = new MCRDirectory(newDirName, newParentDir);

        MCRFilesystemNode[] children = sourceDir.getChildren();
        for (MCRFilesystemNode child : children) {
            if (child instanceof MCRFile) {
                cp((MCRFile) child, newDir, null);
            } else {
                cp((MCRDirectory) child, newDir, null);
            }
        }
    }

    public static void cp(String oldFilePath, String newFilePath) {
        if (oldFilePath == null || newFilePath == null) {
            LOGGER.info("Usage: copy {path_to_file} {new_path}");
        }

        oldFilePath = oldFilePath.trim();
        DerivatePath oldFileLocation = new DerivatePath(oldFilePath);

        try {
            MCRDirectory oldRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(oldFileLocation.getOwnerID());
            String oldFileParent = oldFileLocation.getAbsolutePath();

            if (oldFileParent == null) {
                LOGGER.info("cp: " + oldFilePath + " is a directory or does not exists (not copied)");
            }

            MCRFilesystemNode oldFileNode = oldRootNode.getChildByPath(oldFileParent);
            if (oldFileNode == null) {
                LOGGER.info("cp: " + oldFilePath + " does not exists (not copied)");
                return;
            }

            /* 
             * Zielpfad
             * /jportal_derivate_00000024/path/to/file
             * - ownerID muss existieren
             * - /path/to kann vorhanden sein
             * - file kann Zielordner oder neuer Dateiname sein
             */
            
            
            // New parent checking etc.
            newFilePath = newFilePath.trim();
            DerivatePath newFileLocation = new DerivatePath(newFilePath);
            MCRDirectory newRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(newFileLocation.getOwnerID());
            String newPath = newFileLocation.getAbsolutePath();
            
            if(newPath == null){
                newPath = newFileLocation.getFileName();
            }
            
            MCRDirectory newParentFolder = null;
            String newFileName = null;
            if (newPath == null) {
                newParentFolder = newRootNode;
                newFileName = newFileLocation.getFileName();
                if(newFileName == null){
                    newFileName = oldFileNode.getName();
                }
            } else {
                MCRFilesystemNode childNode = newRootNode.getChildByPath(newPath);

                if (childNode == null) {
                    newFileName = newFileLocation.getFileName();
                    String parentPath = newFileLocation.getDirectoryPath();
                    if(parentPath == null){
                        newParentFolder = newRootNode;
                    }else{
                        newParentFolder = (MCRDirectory) newRootNode.getChildByPath(parentPath);
                    }
                } else if (childNode instanceof MCRDirectory) {
                    newParentFolder = (MCRDirectory) childNode;
                    newFileName = oldFileNode.getName();
                } else {
                    LOGGER.info("cp: " + newPath + " allready exists (not copied)");
                }
            }
            //--------------------------------------------------------------

            if (oldFileNode instanceof MCRDirectory) {
                cp((MCRDirectory) oldFileNode, newParentFolder, newFileName);
            } else {
                cp((MCRFile) oldFileNode, newParentFolder, newFileName);
            }
        } catch (MCRUsageException e) {
            e.printStackTrace();
        } catch (MCRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void rename(String filePath, String newName) throws FileNotFoundException {
        DerivatePath fileLocation = new DerivatePath(filePath);
        String absolutePath = fileLocation.getAbsolutePath();

        String derivateID = fileLocation.getOwnerID();
        MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(derivateID);
        if (rootNode == null) {
            throw new FileNotFoundException("Cannot find root node of derivate " + derivateID);
        }
        MCRFilesystemNode file = rootNode.getChildByPath(absolutePath);
        if (file == null) {
            throw new FileNotFoundException(filePath);
        }
        file.setName(newName);
    }

    public static void mv(String oldFile, String newFile) {
        oldFile = oldFile.trim();
        newFile = newFile.trim();

        DerivatePath oldFileLocation = new DerivatePath(oldFile);
        DerivatePath newFileLocation = new DerivatePath(newFile);

        String oldDerivId = oldFileLocation.getOwnerID();
        String oldAbsolutPath = oldFileLocation.getAbsolutePath();
        String newDerivId = newFileLocation.getOwnerID();
        String newPath = newFileLocation.getDirectoryPath();
        String newName = newFileLocation.getFileName();

        if (oldDerivId == null || oldAbsolutPath == null) {
            return;
        }

        MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(oldDerivId);
        MCRFilesystemNode file = rootNode.getChildByPath(oldAbsolutPath);

        MCRDirectory newRootNode = null;
        if (!oldDerivId.equals(newDerivId)) {
            newRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(newDerivId);
        }

        mv(file, newRootNode, newPath, newName);
    }

    public static void cp(MCRFilesystemNode node, MCRDirectory newRootDir, String newPath, String newName) {
        if (node instanceof MCRDirectory) {
            //is a directory (not copied)
            return;
        }

    }

    public static void mv(MCRFilesystemNode node, MCRDirectory newRootDir, String newPath, String newName) {
        String absolutePathOfNode = node.getAbsolutePath();
        String nodeOwnerID = node.getOwnerID();
        MCRDirectory newParent = null;
        File newTiledFile = null;

        if (node instanceof MCRDirectory) {

        }

        File tileDir = MCRIView2Tools.getTileDir();
        if (newRootDir != null) {
            newParent = newRootDir;
            newTiledFile = MCRImage.getTiledFile(tileDir, newRootDir.getOwnerID(), null);
        } else {
            newTiledFile = MCRImage.getTiledFile(tileDir, nodeOwnerID, null);
        }

        if (newPath != null && !"".equals(newPath)) {
            newParent = createDir(newParent, newPath);
            newTiledFile = new File(newTiledFile, newPath);
            newTiledFile.mkdirs();
        }

        if (newName != null && !"".equals(newName)) {
            node.setName(newName);
            newTiledFile = new File(newTiledFile, createTileName(newName));
        }

        if (newParent != null) {
            node.move(newParent);
        } else {
            MCRFileMetadataManager.instance().storeNode(node);
        }

        try {
            File tiledFile = MCRImage.getTiledFile(tileDir, nodeOwnerID, absolutePathOfNode);
            Files.move(tiledFile, newTiledFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        MCRDerivate der = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(nodeOwnerID));
        der.getDerivate().deleteFileMetaData(absolutePathOfNode);
        MCRMetadataManager.updateMCRDerivateXML(der);
    }

    private static String createTileName(String newName) {
        int pos = newName.lastIndexOf('.');
        String tileName = newName.substring(0, pos > 0 ? pos : newName.length()) + ".iview2";
        return tileName;
    }

    public static MCRDirectory createDir(MCRDirectory parent, String newPath) {
        if (parent == null) {
            return null;
        }

        String[] pathSegments = newPath.split("/");

        for (String pathSegment : pathSegments) {
            parent = new MCRDirectory(pathSegment, parent, true);
        }

        return parent;
    }
}
