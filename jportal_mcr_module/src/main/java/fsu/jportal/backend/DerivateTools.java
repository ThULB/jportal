package fsu.jportal.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.urn.hibernate.MCRURN;

import fsu.jportal.mets.MetsTools;
import fsu.jportal.urn.URNTools;
import fsu.jportal.util.DerivatePath;

public class DerivateTools {
    static Logger LOGGER = Logger.getLogger(DerivateTools.class);

    private static boolean cp(final MCRPath source, MCRPath targetDir, String newName, final Map<MCRPath, MCRPath> copyHistory) {
        if (newName == null) {
            newName = source.getFileName().toString();
        }
        
        if (!Files.isDirectory(targetDir)){
            LOGGER.info("cp: " + targetDir + " is not a directory.");
            return false;
        }

        
        if (Files.exists(targetDir.resolve(newName))) {
            LOGGER.info("cp: " + newName + " allready exists (not copied)");
            return false;
        }

        try {
            if (Files.isRegularFile(source)) {
//                MCRFile newFile = new MCRFile(newName, targetDir);
                Path path = targetDir.resolve(newName);
//                Path path = Files.createTempFile(targetDir, "", newName);
//                Path path = targetDir.resolve(newName);
                Path newPath = Files.copy(source, path, StandardCopyOption.COPY_ATTRIBUTES);
//                newFile.setContentFrom(((MCRFile) source).getContentAsInputStream());
                copyHistory.put(source, MCRPath.toMCRPath(newPath));
            } else {
//                MCRDirectory newDir = new MCRDirectory(newName, targetDir);
                final Path mcrPath = Files.createDirectory(targetDir.resolve(newName));
                Files.walkFileTree(source, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path tempPath = mcrPath.resolve(source.relativize(dir));
                        try {
                            Files.copy(dir, tempPath);
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isDirectory(tempPath)){
                                throw e;
                            }
                        }
                        copyHistory.put(MCRPath.toMCRPath(dir), MCRPath.toMCRPath(tempPath));
                        return super.preVisitDirectory(dir, attrs);
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Path tempPath = mcrPath.resolve(source.relativize(file));
                        Files.copy(file, tempPath, StandardCopyOption.COPY_ATTRIBUTES);
                        copyHistory.put(MCRPath.toMCRPath(file), MCRPath.toMCRPath(tempPath));
                        return super.visitFile(file, attrs);
                    }                    
                });
//                MCRFilesystemNode[] children = ((MCRDirectory) source).getChildren();
//                for (MCRFilesystemNode child : children) {
//                    cp(child, newDir, null, copyHistory);
//                }
            }
            return true;
        } catch (MCRPersistenceException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return false;
    }

    public static boolean cp(String sourcePath, String targetPath, boolean delAfterCopy) {
        if (sourcePath == null || targetPath == null) {
            LOGGER.info("Usage: copy {path_to_file} {new_path}");
        }

//        DerivatePath sourceLocation = new DerivatePath(sourcePath.trim());

        try {
            String[] sourcePathArray = sourcePath.split(":");
            MCRPath sourceNode = MCRPath.getPath(sourcePathArray[0], sourcePathArray[1]);
            
            if (!Files.exists(sourceNode)) {
                LOGGER.info("cp: source " + sourcePath + " does not exists (not copied)");
                return false;
            }
            

//            DerivatePath targetLocation = new DerivatePath(targetPath.trim());
//            MCRDirectory targetDir = getTargetDir(targetLocation);
            String[] targetPathArray = targetPath.split(":");
            MCRPath targetNode = MCRPath.getPath(targetPathArray[0], targetPathArray[1]);

            if (!Files.exists(targetNode)) {
                LOGGER.info("cp: target " + targetPath + " does not exists (not copied)");
                return false;
            }
            String newFileName = null;
            if (!Files.isDirectory(targetNode)){
                newFileName = targetNode.getFileName().toString();
            }                    
//            String newFileName = targetDir.getName().equals(targetLocation.getFileName()) ? null : targetLocation
//                    .getFileName();

            Map<MCRPath, MCRPath> copyHistory = new HashMap<MCRPath, MCRPath>();
            if(cp(sourceNode, targetNode, newFileName, copyHistory)){
                if (delAfterCopy){
                    Derivate derivate = new Derivate(sourceNode.getOwner());
                    moveAttachedData(derivate, copyHistory);
//                    sourceNode.delete();
                    delete(sourceNode);
                }
                return true;
            }
            return false;
        } catch (MCRUsageException e) {
            e.printStackTrace();
        } catch (MCRPersistenceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    private static void moveAttachedData(Derivate srcDerivate, Map<MCRPath, MCRPath> copyHistory) {
        String maindoc = srcDerivate.getMaindoc();
        for (MCRPath sourceNode : copyHistory.keySet()) {
            MCRPath target = copyHistory.get(sourceNode);
            
            String sourcePath = getPathNoLeadingRoot(sourceNode);
            if(sourcePath.equals(maindoc)){
                Derivate targetDerivate = new Derivate(target.getOwner());
                targetDerivate.setMaindoc(getPathNoLeadingRoot(target));
            }
            
            URNTools.updateURN(sourceNode, target);
            MetsTools.updateFileEntry(sourceNode, target);
        }
    }

    public static String getPathNoLeadingRoot(MCRPath node) {
       String nodePath = node.getOwnerRelativePath();
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

    public static void rename(String filePath, String newName) throws IOException {
//        DerivatePath fileLocation = new DerivatePath(filePath);
        
//        MCRFilesystemNode file = fileLocation.toFileNode();
        String[] pathArray = filePath.split(":");
        final Path source = MCRPath.getPath(pathArray[0], pathArray[1]);
        final Path target = source.resolveSibling(newName);
        if (!Files.exists(source)) {
            throw new FileNotFoundException(filePath);
        }
        
        
        
        if (!Files.isDirectory(source)){
            MCRURN urn = URNTools.getURNForFile(MCRPath.toMCRPath(source));
            Files.move(source, target);
            if(urn != null){
                URNTools.updateURNFileName(urn, null, newName);
            }
        }
        else{
            final Path mcrPath = Files.createDirectories(target);
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path tempPath = mcrPath.resolve(source.relativize(dir));
                    try{
                        Files.copy(dir, tempPath);
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(tempPath)){
                            throw e;
                        }
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path tempPath = mcrPath.resolve(source.relativize(file));
                    mvSingleFile(file, tempPath);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
                
            });
        }
    }
    
    private static void mvSingleFile(Path src, Path tgt) throws IOException{
        MCRURN urn = URNTools.getURNForFile(MCRPath.toMCRPath(src));
        Files.move(src, tgt);
        if(urn != null){
            String path = MCRPath.toMCRPath(tgt).getParent().getOwnerRelativePath();
            if(!path.endsWith("/")){
                path += "/";
            }
            URNTools.updateURNFileName(urn, path , tgt.getFileName().toString());
        }
    }

    public static boolean mv(String sourcePath, String targetPath) {
        return cp(sourcePath, targetPath, true);
    }

    public static void cp(String sourcePath, String targetPath) {
        cp(sourcePath, targetPath, false);
    }
    
    public static boolean mkdir(String path){
//        DerivatePath dirPath = new DerivatePath(path);
//        MCRFilesystemNode dirNode = dirPath.toFileNode();
        String[] pathArray = path.split(":");
        MCRPath mcrPath = MCRPath.getPath(pathArray[0], pathArray[1]);
        
        if(Files.exists(mcrPath)){
            LOGGER.info("mkdir: " + mcrPath.getFileName().toString() + " allready exists.");
            return false;
        }
        
        MCRPath parentPath = mcrPath.getParent();
//        MCRFilesystemNode parentNode = dirPath.getParent().toFileNode();
        
        if(Files.isDirectory(parentPath)){
            try {
                Files.createDirectory(mcrPath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
//            new MCRDirectory(dirPath.getFileName(), (MCRDirectory) parentNode);
            return true;
        } else {
            LOGGER.info("mkdir: " + parentPath.getFileName().toString() + " does not exists or is not a directory.");
            return false;
        }
    }
    
    public static int delete(MCRPath mcrPath){
        if (!Files.exists(mcrPath)) {
            return 2; //NOT_FOUND
        }
        
        if (!Files.isDirectory(mcrPath)) {
            try {
                Files.delete(mcrPath);
            } catch (IOException e) {
                e.printStackTrace();
                return 0;
            }
            return 1; //OK
        }
        try {
            Files.walkFileTree(mcrPath, new SimpleFileVisitor<java.nio.file.Path>(){

                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return super.visitFile(file, attrs);
                }

                @Override
                public FileVisitResult postVisitDirectory(java.nio.file.Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return super.postVisitDirectory(dir, exc);
                }
                
            });
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
        return 1; //OK
    }
    
    public static String uploadFile(InputStream inputStream, long filesize, String documentID, String derivateID,
            String filePath) throws Exception {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        if (derivateID == null) {
            String projectID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID", "MCR");
            derivateID = MCRObjectID.getNextFreeId(projectID + '_' + "derivate").toString();
        }
        MCRUploadHandlerIFS handler = new MCRUploadHandlerIFS(documentID, derivateID, null);
        handler.startUpload(1);
        session.commitTransaction();
        handler.receiveFile(filePath, inputStream, filesize, null);
        session.beginTransaction();
        handler.finishUpload();
        handler.unregister();
        return derivateID;
    }
}
