package fsu.jportal.backend;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fsu.jportal.gson.DerivateTypeAdapter;
import fsu.jportal.gson.FileNodeWrapper;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;
import fsu.jportal.mets.MetsTools;
import fsu.jportal.urn.URNTools;
import fsu.jportal.util.DerivateLinkUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.iview2.frontend.MCRIView2Commands;
import org.mycore.iview2.services.MCRIView2Tools;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.MCRPIServiceManager;
import org.mycore.pi.MCRPersistentIdentifier;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;

public class DerivateTools {
    static Logger LOGGER = LogManager.getLogger(DerivateTools.class);

    private static boolean cp(final MCRPath source, MCRPath targetDir, String newName,
        final Map<MCRPath, MCRPath> copyHistory) {
        if (newName == null) {
            newName = source.getFileName().toString();
        }

        if (!Files.isDirectory(targetDir)) {
            LOGGER.info("cp: " + targetDir + " is not a directory.");
            return false;
        }

        if (Files.exists(targetDir.resolve(newName))) {
            LOGGER.info("cp: " + newName + " allready exists (not copied)");
            return false;
        }

        try {
            if (Files.isRegularFile(source)) {
                Path path = targetDir.resolve(newName);
                Path newPath = Files.copy(source, path, StandardCopyOption.COPY_ATTRIBUTES);
                copyHistory.put(source, MCRPath.toMCRPath(newPath));
            } else {
                final Path mcrPath = Files.createDirectory(targetDir.resolve(newName));
                Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                        Path tempPath = mcrPath.resolve(source.relativize(dir));
                        try {
                            Files.copy(dir, tempPath);
                        } catch (FileAlreadyExistsException e) {
                            if (!Files.isDirectory(tempPath)) {
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
            }
            return true;
        } catch (MCRPersistenceException | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean cp(String sourcePath, String targetPath, boolean delAfterCopy) throws MCRAccessException {
        if (sourcePath == null || targetPath == null) {
            LOGGER.info("Usage: copy {path_to_file} {new_path}");
            return false;
        }
        String[] sourcePathArray = sourcePath.split(":");
        MCRPath sourceNode = MCRPath.getPath(sourcePathArray[0], sourcePathArray[1]);

        String[] targetPathArray = targetPath.split(":");
        MCRPath targetNode = MCRPath.getPath(targetPathArray[0], targetPathArray[1]);

        return cp(sourceNode, targetNode, delAfterCopy);
    }

    public static boolean cp(MCRPath sourceNode, MCRPath targetNode, boolean delAfterCopy) throws MCRAccessException {
        if (!Files.exists(sourceNode)) {
            LOGGER.info("cp: source " + sourceNode + " does not exists (not copied)");
            return false;
        }

        if (!Files.exists(targetNode)) {
            LOGGER.info("cp: target " + targetNode + " does not exists (not copied)");
            return false;
        }
        String newFileName = null;
        if (!Files.isDirectory(targetNode)) {
            newFileName = targetNode.getFileName().toString();
        }

        Map<MCRPath, MCRPath> copyHistory = new HashMap<>();
        if (cp(sourceNode, targetNode, newFileName, copyHistory)) {
            if (delAfterCopy) {
                String derivID = sourceNode.getOwner();
                JPDerivateComponent derivate = new JPDerivateComponent(derivID);
                moveAttachedData(derivate, copyHistory);
                delete(sourceNode);
            }
            return true;
        }
        return false;
    }

    public static String getMaindoc(JPDerivateComponent derivate) {
        return derivate.getObject().getDerivate().getInternals().getMainDoc();
    }

    private static void moveAttachedData(JPDerivateComponent srcDerivate, Map<MCRPath, MCRPath> copyHistory)
        throws MCRAccessException {
        String maindoc = getMaindoc(srcDerivate);
        for (MCRPath sourceNode : copyHistory.keySet()) {
            MCRPath target = copyHistory.get(sourceNode);
            String sourcePath = getPathNoLeadingRoot(sourceNode);

            List<MCRObjectID> idList = DerivateLinkUtil.getLinks(sourceNode);
            DerivateLinkUtil.setLinks(idList, MCRPath.toMCRPath(target));
            DerivateLinkUtil.deleteFileLinks(idList, sourceNode);

            if (sourcePath.equals(maindoc)) {
                setAsMain(target.getOwner(), getPathNoLeadingRoot(target));
            }
            URNTools.updateURN(sourceNode, target);
            MetsTools.updateFileEntry(sourceNode, target);
        }
    }

    public static String getPathNoLeadingRoot(MCRPath node) {
        String nodePath = node.getOwnerRelativePath();
        if (!nodePath.equals("/")) {
            nodePath = nodePath.substring(1);
        }
        return nodePath;
    }

    public static void rename(String filePath, String newName) throws IOException, MCRAccessException {
        String[] pathArray = filePath.split(":");
        final Path source = MCRPath.getPath(pathArray[0], pathArray[1]);
        final Path target = source.resolveSibling(newName);
        if (!Files.exists(source)) {
            throw new FileNotFoundException(filePath);
        }

        if (!Files.isDirectory(source)) {
            mvSingleFile(MCRPath.toMCRPath(source), MCRPath.toMCRPath(target));
        } else {
            final Path mcrPath = Files.createDirectories(target);
            Files.walkFileTree(source, new SimpleFileVisitor<Path>() {

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    Path tempPath = mcrPath.resolve(source.relativize(dir));
                    try {
                        Files.copy(dir, tempPath);
                    } catch (FileAlreadyExistsException e) {
                        if (!Files.isDirectory(tempPath)) {
                            throw e;
                        }
                    }
                    return super.preVisitDirectory(dir, attrs);
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Path tempPath = mcrPath.resolve(source.relativize(file));
                    try {
                        mvSingleFile(MCRPath.toMCRPath(file), MCRPath.toMCRPath(tempPath));
                    } catch (MCRAccessException e) {
                        e.printStackTrace();
                    }
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

    public static Map<String, String> renameFiles(String derivateID, String pattern, String newName)
        throws IOException {
        MCRPath derivateRoot = MCRPath.getPath(derivateID, "/");
        Pattern patternObj = Pattern.compile(pattern);
        Map<String, String> resultMap = new HashMap<>();
        MCRDerivate mcrDerivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateID));
        String mainDoc = mcrDerivate.getDerivate().getInternals().getMainDoc();

        Files.walkFileTree(derivateRoot, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                throws IOException {
                Matcher matcher = patternObj.matcher(file.getFileName().toString());
                if (matcher.matches()) {
                    String newFilename;

                    try {
                        newFilename = matcher.replaceAll(newName);
                    } catch (IndexOutOfBoundsException e) {
                        LOGGER.info("The file " + file + " can't be renamed to " + newName + ". To many groups!");
                        return FileVisitResult.CONTINUE;
                    } catch (IllegalArgumentException e) {
                        LOGGER.info("The new name '" + newName + "' contains illegal characters!");
                        return FileVisitResult.CONTINUE;
                    }

                    try {
                        mvSingleFile(MCRPath.toMCRPath(file), MCRPath.toMCRPath(file.resolveSibling(newFilename)));
                    } catch (MCRAccessException e) {
                        e.printStackTrace();
                        return FileVisitResult.CONTINUE;
                    }

                    if (MCRPath.toMCRPath(file).getOwnerRelativePath().equals(mainDoc)) {
                        String path = file.toString().substring(0, file.toString().lastIndexOf("/") + 1) + newFilename;
                        DerivateTools.setAsMain(file.toString().substring(0, file.toString().lastIndexOf(":")),
                            path.substring(path.lastIndexOf(":") + 1));
                    }

                    LOGGER.info("The file " + file + " was renamed to " + newFilename);
                    resultMap.put(file.getFileName().toString(), newFilename);
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return resultMap;
    }

    private static void mvSingleFile(MCRPath src, MCRPath tgt) throws IOException, MCRAccessException {
        List<MCRObjectID> idList = DerivateLinkUtil.getLinks(src);
        DerivateLinkUtil.deleteFileLinks(idList, src);

        Files.move(src, tgt);

        DerivateLinkUtil.setLinks(idList, tgt);
        MCRPI urn = URNTools.getURNForFile(src);
        if (urn != null) {
            URNTools.updateURNFileName(urn, tgt);
        }
    }

    public static boolean mv(String sourcePath, String targetPath) throws MCRAccessException {
        return cp(sourcePath, targetPath, true);
    }

    public static boolean mv(MCRPath sourcePath, MCRPath targetPath) throws MCRAccessException {
        return cp(sourcePath, targetPath, true);
    }

    public static void cp(String sourcePath, String targetPath) throws MCRAccessException {
        cp(sourcePath, targetPath, false);
    }

    public static boolean mkdir(String path) {
        String[] pathArray = path.split(":");
        MCRPath mcrPath = MCRPath.getPath(pathArray[0], pathArray[1]);

        if (Files.exists(mcrPath)) {
            LOGGER.info("mkdir: " + mcrPath.getFileName().toString() + " allready exists.");
            return false;
        }

        MCRPath parentPath = mcrPath.getParent();

        if (Files.isDirectory(parentPath)) {
            try {
                Files.createDirectory(mcrPath);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            LOGGER.info("mkdir: " + parentPath.getFileName().toString() + " does not exists or is not a directory.");
            return false;
        }
    }

    public static int delete(MCRPath mcrPath) {
        if (!Files.exists(mcrPath)) {
            return 2; //NOT_FOUND
        }
        if (!Files.isDirectory(mcrPath)) {
            try {
                DerivateLinkUtil.deleteFileLink(mcrPath);
            } catch (Exception exc) {
                LOGGER.error("Unable to delete links of path " + mcrPath);
                return 0;
            }
            try {
                Files.delete(mcrPath);
            } catch (Exception exc) {
                LOGGER.error("Unable to delete file " + mcrPath);
                return 0;
            }
            return 1; //OK
        }
        try {
            Files.walkFileTree(mcrPath, new SimpleFileVisitor<java.nio.file.Path>() {
                @Override
                public FileVisitResult visitFile(java.nio.file.Path file, BasicFileAttributes attrs)
                    throws IOException {
                    try {
                        DerivateLinkUtil.deleteFileLink(MCRPath.toMCRPath(file));
                    } catch (MCRAccessException e) {
                        LOGGER.error("Unable to delete links of path " + mcrPath);
                    }
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
            LOGGER.error("Error while walking file tree of " + mcrPath, e);
            return 0;
        }
        return 1; //OK
    }

    /**
     * Uploads a new file. No database transaction is required. In fact, a "nested transaction
     * is not supported" error is thrown when a transaction is already started.
     *
     * @see #uploadFile(InputStream, long, String, String, String)
     */
    public static String uploadFileWithoutTransaction(InputStream inputStream, long filesize, String documentID,
        String derivateID, String filePath) throws Exception {
        MCRSession session = MCRSessionMgr.getCurrentSession();
        try {
            session.beginTransaction();
            derivateID = uploadFile(inputStream, filesize, documentID, derivateID, filePath);
            session.commitTransaction();
        } finally {
            session.rollbackTransaction();
        }
        return derivateID;
    }

    /**
     * Uploads a new file. A valid database transaction is required.
     *
     * @param inputStream input stream to upload
     * @param filesize size of the file
     * @param documentID mycore object where the derivate is added
     * @param derivateID derivate where the file is added (when null, a new derivate is created)
     * @param filePath path of the file
     * @return derivateId
     */
    public static String uploadFile(InputStream inputStream, long filesize, String documentID, String derivateID,
        String filePath) throws IOException, MCRAccessException {
        if (derivateID == null) {
            String projectID = MCRConfiguration.instance().getString("MCR.Metadata.Project", "MCR");
            derivateID = MCRObjectID.getNextFreeId(projectID + '_' + "derivate").toString();
        }
        MCRUploadHandlerIFS handler = new MCRUploadHandlerIFS(documentID, derivateID);
        try {
            handler.startUpload(1);
            handler.receiveFile(filePath, inputStream, filesize, null);
            handler.finishUpload();
        } finally {
            handler.unregister();
        }
        return derivateID;
    }

    public static void setAsMain(String derivateID, String path) {
        JPDerivateComponent derivateComponent = new JPDerivateComponent(derivateID);
        MCRDerivate mcrDerivate = derivateComponent.getObject();
        mcrDerivate.getDerivate().getInternals().setMainDoc(path);
        try {
            MCRMetadataManager.update(mcrDerivate);
        } catch (MCRAccessException e) {
            LOGGER.error("Could not update derivate " + derivateID);
            e.printStackTrace();
        }
    }

    public static JsonObject getChildAsJson(String derivateID, String path, String file) {
        MCRDirectory node = getRootDir(derivateID);
        if (node == null) {
            return null;
        }

        if (path != null && !"".equals(path.trim())) {
            node = (MCRDirectory) node.getChildByPath(path);
            if (node == null) {
                return null;
            }
        }

        MCRFilesystemNode child = node.getChild(file);
        if (child != null) {
            JsonObject childJson = new JsonObject();
            childJson.addProperty("name", child.getName());
            childJson.addProperty("size", child.getSize());
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
            childJson.addProperty("lastmodified", sdf.format(child.getLastModified().getTime()));
            return childJson;
        } else {
            return null;
        }
    }

    private static MCRDirectory getRootDir(String derivateID) {
        return MCRDirectory.getRootDirectory(derivateID);
    }

    public static JsonObject getDerivateFolderAsJson(String derivateID) {
        MCRDirectory node = getRootDir(derivateID);

        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("name", node.getName());
        jsonObject.addProperty("absPath", node.getAbsolutePath());
        jsonObject.addProperty("isRoot", true);
        if (node.getNumChildren(2, 2) > 0) {
            jsonObject.addProperty("hasChildren", true);
            jsonObject.add("children", getFolderChildren(node.getChildren()));
        } else {
            jsonObject.addProperty("hasChildren", false);
        }
        return jsonObject;
    }

    private static JsonArray getFolderChildren(MCRFilesystemNode[] childs) {
        JsonArray jsonArray = new JsonArray();
        for (MCRFilesystemNode child : childs) {
            if (child instanceof MCRDirectory) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("name", child.getName());
                jsonObj.addProperty("absPath", child.getAbsolutePath());
                jsonObj.addProperty("isRoot", false);
                if (((MCRDirectory) child).getNumChildren(2, 1) > 0) {
                    jsonObj.addProperty("hasChildren", true);
                    jsonObj.add("children", getFolderChildren(((MCRDirectory) child).getChildren()));
                } else {
                    jsonObj.addProperty("hasChildren", false);
                }
                jsonArray.add(jsonObj);
            }
        }
        return jsonArray;
    }

    public static boolean addURNToDerivate(String derivateID) {
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(derivateID);
        return !addURNToFile(mcrObjectID, "").equals("")
            && URNTools
                .registerURNs(mcrObjectID)
                .noneMatch(pi -> pi.getRegistered() == null);
    }

    public static String addURNToFile(String derivatID, String path) {
        return addURNToFile(MCRObjectID.getInstance(derivatID), path);
    }

    public static String addURNToFile(MCRObjectID derivID, String path) {
        MCRPIService<MCRPersistentIdentifier> dnburnGranular = MCRPIServiceManager.getInstance()
            .getRegistrationService(
                URNTools.SERVICEID);

        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivID);
        try {
            MCRPersistentIdentifier urn = dnburnGranular.register(derivate, path);
            return urn.asString();
        } catch (ExecutionException | InterruptedException | MCRAccessException | MCRActiveLinkException
            | MCRPersistentIdentifierException e) {
            LOGGER.error("Unable to add URN to file {}", path);
        }

        return "";
    }

    public static void setLink(String documentID, String path) throws MCRActiveLinkException, MCRAccessException {
        DerivateLinkUtil.setLink(MCRJerseyUtil.getID(documentID), path);
    }

    public static void removeLink(String documentID, String path) throws MCRActiveLinkException, MCRAccessException {
        DerivateLinkUtil.removeLink(MCRJerseyUtil.getID(documentID), path);
    }

    public static JsonObject getDerivateAsJson(String derivateID, String path, boolean noChilds) {
        MCRJSONManager gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new DerivateTypeAdapter());
        gsonManager.registerAdapter(new MCRFilesystemNodeTypeAdapter());

        MCRDirectory root = getRootDir(derivateID);
        MCRFilesystemNode node = root;
        if (root != null && path != null && !path.equals("")) {
            node = root.getChildByPath(path);
        }

        JPDerivateComponent derivateComponent = new JPDerivateComponent(derivateID);
        String maindoc = getMaindoc(derivateComponent);

        FileNodeWrapper wrapper = new FileNodeWrapper(node, maindoc);
        JsonObject json = gsonManager.createGson().toJsonTree(wrapper).getAsJsonObject();

        json.addProperty("display", isDisplayEnabled(derivateID));
        json.addProperty("urnEnabled", urnEnabled());

        return json;
    }

    public static boolean isDisplayEnabled(String derivateID) {
        MCRDerivate derObj = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateID));
        return derObj.getDerivate().isDisplayEnabled();
    }

    public static boolean tileDerivate(String derivateID) {
        if (!MCRIView2Tools.isDerivateSupported(derivateID)) {
            LOGGER.info("Skipping tiling of derivate " + derivateID + " as it's main file is not supported by IView2.");
            return false;
        }
        MCRPath derivateRoot = MCRPath.getPath(derivateID, "/");

        if (!Files.exists(derivateRoot)) {
            LOGGER.info("Derivate " + derivateID + " does not exist or is not a directory!");
            return false;
        }

        try {
            Files.walkFileTree(derivateRoot, new SimpleFileVisitor<java.nio.file.Path>() {

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Objects.requireNonNull(file);
                    Objects.requireNonNull(attrs);
                    if (MCRIView2Tools.isFileSupported(file)) {
                        MCRIView2Commands.tileImage(MCRPath.toMCRPath(file));
                    }
                    return FileVisitResult.CONTINUE;
                }

            });
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static String getMD5forFile(String derivateID, String path) {
        MCRDirectory rootDir = getRootDir(derivateID);

        if (rootDir == null) {
            return "";
        }

        MCRFile mcrFile = (MCRFile) rootDir.getChildByPath(path);
        if (mcrFile != null) {
            return mcrFile.getMD5();
        }
        return "";
    }

    public static boolean urnEnabled() {
        String urnObjects = "";
        try {
            urnObjects = MCRConfiguration.instance().getString("MCR.URN.Enabled.Objects");
        } catch (MCRConfigurationException e) {
            LOGGER.info("Property MCR.URN.Enabled.Object not set, URN allocation not possible.");
        }
        return urnObjects.contains("derivate");
    }

    public static Object checkFileType(String derivateID, String path) {
        MCRDirectory rootDir = getRootDir(derivateID);

        if (rootDir == null) {
            return "";
        }
        if ("".equals(path)) {
            return "directory";
        }

        MCRFilesystemNode mcrFile = rootDir.getChildByPath(path);
        if (mcrFile instanceof MCRDirectory) {
            return "directory";
        }
        if (mcrFile instanceof MCRFile) {
            return "file";
        } else {
            return "";
        }
    }
}
