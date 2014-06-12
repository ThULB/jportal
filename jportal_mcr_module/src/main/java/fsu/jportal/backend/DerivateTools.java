package fsu.jportal.backend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFileMetadataManager;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.util.FileLocation;
import org.mycore.imagetiler.MCRImage;
import org.mycore.iview2.services.MCRIView2Tools;

import com.google.common.io.Files;

public class DerivateTools {

    public static void rename(String filePath, String newName) throws FileNotFoundException {
        FileLocation fileLocation = new FileLocation(filePath);
        String absolutePath = fileLocation.getAbsolutPath();

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

        FileLocation oldFileLocation = new FileLocation(oldFile);
        FileLocation newFileLocation = new FileLocation(newFile);

        String oldDerivId = oldFileLocation.getOwnerID();
        String oldAbsolutPath = oldFileLocation.getAbsolutPath();
        String newDerivId = newFileLocation.getOwnerID();
        String newPath = newFileLocation.getPath();
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
