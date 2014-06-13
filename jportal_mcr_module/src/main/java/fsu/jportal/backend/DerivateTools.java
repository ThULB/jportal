package fsu.jportal.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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

    public static class Derivates {
        private DerivatePath path;

        public Derivates(String path) {
            this(new DerivatePath(path));
        }

        public Derivates(DerivatePath path) {
            this.setPath(path);
        }

        public MCRFilesystemNode getFileNode() {
            String derivateID = getPath().getDerivateID();
            String absPath = getPath().getAbsolutePath();

            if (derivateID == null || absPath == null) {
                return null;
            }

            MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(derivateID);
            return rootNode.getChildByPath(absPath);
        }

        public DerivatePath getPath() {
            return path;
        }

        private void setPath(DerivatePath path) {
            this.path = path;
        }

        public MCRDirectory getParentNode() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    public static void cp(String oldFilePathStr, String newFilePathStr) {
        if (oldFilePathStr == null || newFilePathStr == null) {
            LOGGER.info("Usage: copy {path_to_file} {new_path}");
        }

        try {
            Derivates oldDerivate = new Derivates(oldFilePathStr.trim());
            MCRFilesystemNode oldFileNode = oldDerivate.getFileNode();

            if (oldFileNode == null) {
                LOGGER.info("cp: " + oldFilePathStr + " does not exists (not copied)");
                return;
            }

            if (oldFileNode instanceof MCRDirectory) {
                LOGGER.info("cp: " + oldFilePathStr + " is a directory (not copied)");
                return;
            }

            Derivates newDerivate = new Derivates(newFilePathStr.trim());
            MCRFilesystemNode newFileNode = newDerivate.getFileNode();

            MCRDirectory newParentFolder = null;
            String newFileName = null;
            if (newFileNode == null) {
                newFileName = newDerivate.getPath().getFileName();
                newParentFolder = newDerivate.getParentNode();
            }else if(newFileNode instanceof MCRDirectory){
                newParentFolder = (MCRDirectory) newFileNode;
            }else if(newFileNode instanceof MCRFile){
                LOGGER.info("cp: " + newFilePathStr + " allready exists (not copied)");
                return;
            }else{
                LOGGER.info("cp: " + newFilePathStr + " unknown error (not copied)");
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
            DerivatePath newFilePath = new DerivatePath(newFilePathStr.trim());
            MCRDirectory newRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(newFilePath.getDerivateID());
            String newPath = newFilePath.getAbsolutePath();

            if (newPath == null) {
                newPath = newFilePath.getFileName();
            }

            if (newPath == null) {
                newParentFolder = newRootNode;
                newFileName = newFilePath.getFileName();
                if (newFileName == null) {
                    newFileName = oldFileNode.getName();
                }
            } else {
                MCRFilesystemNode childNode = newRootNode.getChildByPath(newPath);

                if (childNode == null) {
                    newFileName = newFilePath.getFileName();
                    String parentPath = newFilePath.getParentPath();
                    if (parentPath == null) {
                        newParentFolder = newRootNode;
                    } else {
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
        DerivatePath derivPath = new DerivatePath(filePath);
        String absPath = derivPath.getAbsolutePath();

        String derivateID = derivPath.getDerivateID();
        MCRDirectory rootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(derivateID);
        if (rootNode == null) {
            throw new FileNotFoundException("Cannot find root node of derivate " + derivateID);
        }
        MCRFilesystemNode file = rootNode.getChildByPath(absPath);
        if (file == null) {
            throw new FileNotFoundException(filePath);
        }
        file.setName(newName);
    }

    public static void mv(String oldPath, String newPath) {
        DerivatePath oldFilePath = new DerivatePath(oldPath.trim());
        DerivatePath newFilePath = new DerivatePath(newPath.trim());

        String oldDerivId = oldFilePath.getDerivateID();
        String oldAbsolutPath = oldFilePath.getAbsolutePath();
        String newDerivId = newFilePath.getDerivateID();
        String newDirPath = newFilePath.getParentPath();
        String newFileName = newFilePath.getFileName();

        if (oldDerivId == null || oldAbsolutPath == null) {
            return;
        }

        MCRDirectory oldRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(oldDerivId);
        MCRFilesystemNode file = oldRootNode.getChildByPath(oldAbsolutPath);

        MCRDirectory newRootNode = null;
        if (!oldDerivId.equals(newDerivId)) {
            newRootNode = (MCRDirectory) MCRFilesystemNode.getRootNode(newDerivId);
        }

        mv(file, newRootNode, newDirPath, newFileName);
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
