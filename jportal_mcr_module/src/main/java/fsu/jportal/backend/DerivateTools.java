package fsu.jportal.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRUsageException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.urn.hibernate.MCRURN;

import fsu.jportal.mets.MetsTools;
import fsu.jportal.urn.URNTools;
import fsu.jportal.util.DerivatePath;

public class DerivateTools {
    static Logger LOGGER = Logger.getLogger(DerivateTools.class);

    private static void cp(MCRFilesystemNode source, MCRDirectory targetDir, String newName, Map<MCRFilesystemNode, MCRFile> copyHistory) {
        if (newName == null) {
            newName = source.getName();
        }

        if (targetDir.hasChild(newName)) {
            LOGGER.info("cp: " + newName + " allready exists (not copied)");
            return;
        }

        try {
            if (source instanceof MCRFile) {
                MCRFile newFile = new MCRFile(newName, targetDir);
                newFile.setContentFrom(((MCRFile) source).getContentAsInputStream());
                copyHistory.put(source, newFile);
            } else {
                MCRDirectory newDir = new MCRDirectory(newName, targetDir);

                MCRFilesystemNode[] children = ((MCRDirectory) source).getChildren();
                for (MCRFilesystemNode child : children) {
                    cp(child, newDir, null, copyHistory);
                }
            }
        } catch (MCRPersistenceException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void cp(String sourcePath, String targetPath, boolean delAfterCopy) {
        if (sourcePath == null || targetPath == null) {
            LOGGER.info("Usage: copy {path_to_file} {new_path}");
        }

        DerivatePath sourceLocation = new DerivatePath(sourcePath.trim());

        try {
            MCRFilesystemNode sourceNode = sourceLocation.toFileNode();

            if (sourceNode == null) {
                LOGGER.info("cp: source " + sourcePath + " does not exists (not copied)");
                return;
            }
            

            DerivatePath targetLocation = new DerivatePath(targetPath.trim());
            MCRDirectory targetDir = getTargetDir(targetLocation);

            if (targetDir == null) {
                LOGGER.info("cp: target " + targetLocation.getAbsolutePath() + " does not exists (not copied)");
                return;
            }

            String newFileName = targetDir.getName().equals(targetLocation.getFileName()) ? null : targetLocation
                    .getFileName();

            Map<MCRFilesystemNode, MCRFile> copyHistory = new HashMap<MCRFilesystemNode, MCRFile>();
            cp(sourceNode, targetDir, newFileName, copyHistory);
            
            if(delAfterCopy){
                Derivate derivate = new Derivate(sourceNode.getOwnerID());
                moveAttachedData(derivate, copyHistory);
                sourceNode.delete();
            }
        } catch (MCRUsageException e) {
            e.printStackTrace();
        } catch (MCRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void moveAttachedData(Derivate derivate, Map<MCRFilesystemNode, MCRFile> copyHistory) {
        String maindoc = derivate.getMaindoc();
        for (MCRFilesystemNode sourceNode : copyHistory.keySet()) {
            MCRFile target = copyHistory.get(sourceNode);
            
            String sourcePath = getPathNoLeadingRoot(sourceNode);
            if(sourcePath.equals(maindoc)){
                derivate.setMaindoc(getPathNoLeadingRoot(target));
            }
            
            URNTools.updateURN(sourceNode, target);
            MetsTools.updateFileEntry(sourceNode, target);
        }
    }

    public static String getPathNoLeadingRoot(MCRFilesystemNode node) {
        String nodePath = node.getAbsolutePath();
        if(!node.equals("/")){
            nodePath = nodePath.substring(1);
        }
        return nodePath;
    }

    public static MCRDirectory getTargetDir(DerivatePath derivPath) {
        MCRFilesystemNode fileNode = derivPath.toFileNode();
        if (fileNode == null) {
            String parentPath = derivPath.getParentPath();
            if (parentPath != null) {
                MCRFilesystemNode parentNode = derivPath.getParent().toFileNode();

                if (parentNode instanceof MCRDirectory) {
                    return (MCRDirectory) parentNode;
                }
            }
        } else {
            return (MCRDirectory) fileNode;
        }

        return null;
    }

    public static void rename(String filePath, String newName) throws FileNotFoundException {
        DerivatePath fileLocation = new DerivatePath(filePath);
        
        MCRFilesystemNode file = fileLocation.toFileNode();
        if (file == null) {
            throw new FileNotFoundException(filePath);
        }
        
        MCRURN urn = URNTools.getURNForFile(file);
        file.setName(newName);
        
        if(urn != null){
            URNTools.updateURNFileName(urn, null, newName);
        }
    }

    public static void mv(String sourcePath, String targetPath) {
        cp(sourcePath, targetPath, true);
    }

    public static void cp(String sourcePath, String targetPath) {
        cp(sourcePath, targetPath, false);
    }
    
    public static void mkdir(String path){
        DerivatePath dirPath = new DerivatePath(path);
        MCRFilesystemNode dirNode = dirPath.toFileNode();
        
        if(dirNode != null){
            LOGGER.info("mkdir: " + dirPath.getAbsolutePath() + " allready exists.");
            return;
        }
        
        MCRFilesystemNode parentNode = dirPath.getParent().toFileNode();
        
        if(parentNode instanceof MCRDirectory){
            new MCRDirectory(dirPath.getFileName(), (MCRDirectory) parentNode);
        } else {
            LOGGER.info("mkdir: " + dirPath.getParentPath() + " does not exists or is not a directory.");
        }
    }
}
