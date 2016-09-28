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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUsageException;
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
import org.mycore.urn.hibernate.MCRURN;
import org.mycore.urn.services.MCRURNAdder;
import org.mycore.urn.services.MCRURNManager;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import fsu.jportal.gson.DerivateTypeAdapter;
import fsu.jportal.gson.FileNodeWrapper;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;
import fsu.jportal.mets.MetsTools;
import fsu.jportal.urn.URNTools;
import fsu.jportal.util.DerivateLinkUtil;

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
        }

        try {
            String[] sourcePathArray = sourcePath.split(":");
            MCRPath sourceNode = MCRPath.getPath(sourcePathArray[0], sourcePathArray[1]);

            if (!Files.exists(sourceNode)) {
                LOGGER.info("cp: source " + sourcePath + " does not exists (not copied)");
                return false;
            }

            String[] targetPathArray = targetPath.split(":");
            MCRPath targetNode = MCRPath.getPath(targetPathArray[0], targetPathArray[1]);

            if (!Files.exists(targetNode)) {
                LOGGER.info("cp: target " + targetPath + " does not exists (not copied)");
                return false;
            }
            String newFileName = null;
            if (!Files.isDirectory(targetNode)) {
                newFileName = targetNode.getFileName().toString();
            }

            Map<MCRPath, MCRPath> copyHistory = new HashMap<MCRPath, MCRPath>();
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
        } catch (MCRUsageException e) {
            e.printStackTrace();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String getMaindoc(JPDerivateComponent derivate){
        return derivate.getObject().getDerivate().getInternals().getMainDoc();
    }

    private static void moveAttachedData(JPDerivateComponent srcDerivate, Map<MCRPath, MCRPath> copyHistory)
            throws MCRAccessException {
        String maindoc = getMaindoc(srcDerivate);
        for (MCRPath sourceNode : copyHistory.keySet()) {
            MCRPath target = copyHistory.get(sourceNode);
            String sourcePath = getPathNoLeadingRoot(sourceNode);

            List<MCRObjectID> idList = new ArrayList<MCRObjectID>();
            try {
                idList = DerivateLinkUtil.getLinks(sourceNode);
                DerivateLinkUtil.setLinks(idList, MCRPath.toMCRPath(target));
                DerivateLinkUtil.deleteFileLinks(idList, sourceNode);
            } catch (SolrServerException e) {
                LOGGER.error("unable to get or set all file links");
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (sourcePath.equals(maindoc)) {
                setAsMain(target.getOwner(), getPathNoLeadingRoot(target));
            }
            URNTools.updateURN(sourceNode, target);
            MetsTools.updateFileEntry(sourceNode, target);
        }
    }

    public static String getPathNoLeadingRoot(MCRPath node) {
        String nodePath = node.getOwnerRelativePath();
        if (!node.equals("/")) {
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

    private static void mvSingleFile(MCRPath src, MCRPath tgt) throws IOException, MCRAccessException {
        MCRURN urn = URNTools.getURNForFile(src);
        List<MCRObjectID> idList = new ArrayList<MCRObjectID>();
        try {
            idList = DerivateLinkUtil.getLinks(src);
        } catch (SolrServerException e) {
            LOGGER.error("unable to get all file links");
        }
        DerivateLinkUtil.deleteFileLinks(idList, src);

        Files.move(src, tgt);

        DerivateLinkUtil.setLinks(idList, tgt);
        if (urn != null) {
            String path = tgt.getParent().getOwnerRelativePath();
            if (!path.endsWith("/")) {
                path += "/";
            }
            URNTools.updateURNFileName(urn, path, tgt.getFileName().toString());
        }
    }

    public static boolean mv(String sourcePath, String targetPath) throws MCRAccessException {
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
            } catch(Exception exc) {
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
                    } catch (SolrServerException | MCRAccessException e) {
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
     * @throws Exception
     */
    public static String uploadFile(InputStream inputStream, long filesize, String documentID, String derivateID,
                                    String filePath) throws Exception {
        if (derivateID == null) {
            String projectID = MCRConfiguration.instance().getString("MCR.SWF.Project.ID", "MCR");
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
        MCRMetadataManager.updateMCRDerivateXML(mcrDerivate);
    }

    public static JsonObject getChildAsJson(String derivateID, String path, String file) {
        MCRDirectory node = getRootDir(derivateID);
        if(node == null){
            return null;
        }

        if (path != null && !"".equals(path.trim())) {
            getDirByPath(derivateID, path);
            node = (MCRDirectory) node.getChildByPath(path);
            if(node == null){
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

    private static MCRFilesystemNode getDirByPath(String derivateID, String path) {
        MCRDirectory rootDir = getRootDir(derivateID);
        if(rootDir == null){
            return null;
        }

        return rootDir.getChildByPath(path);
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
        MCRURNAdder urnAdder = new MCRURNAdder();
        try {
            return urnAdder.addURNToDerivates(derivateID);
        } catch (IOException | JDOMException | SAXException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String addURNToFile(String derivatID, String path) {
        MCRURNAdder urnAdder = new MCRURNAdder();
        try {
            if (urnAdder.addURNToSingleFile(derivatID, path)) {
                return getURNforFile(derivatID, path);
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String getURNforFile(String derivate, String path) {
        String fileName = path;
        String pathToFile = "/";
        if (path.contains("/")) {
            pathToFile = path.substring(0, path.lastIndexOf("/") + 1);
            fileName = path.substring(path.lastIndexOf("/") + 1);
        }
        return MCRURNManager.getURNForFile(derivate, pathToFile, fileName);
    }

    public static void setLink(String documentID, String path) throws MCRActiveLinkException, MCRAccessException {
        DerivateLinkUtil.setLink(MCRJerseyUtil.getID(documentID), path);
    }

    public static void removeLink(String documentID, String path) throws MCRActiveLinkException, MCRAccessException {
        DerivateLinkUtil.removeLink(MCRJerseyUtil.getID(documentID), path);
    }

    public static JsonObject getDerivateAsJson(String derivateID, String path) {
        MCRJSONManager gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new DerivateTypeAdapter());
        gsonManager.registerAdapter(new MCRFilesystemNodeTypeAdapter());

        MCRDirectory root = getRootDir(derivateID);
        MCRFilesystemNode node = root;
        if (root != null && path != null && !path.equals("")){
            node = root.getChildByPath(path);
        }

        JPDerivateComponent derivateComponent = new JPDerivateComponent(derivateID);
        String maindoc = getMaindoc(derivateComponent);

        FileNodeWrapper wrapper = new FileNodeWrapper(node, maindoc);
        JsonObject json = gsonManager.createGson().toJsonTree(wrapper).getAsJsonObject();

        json.addProperty("display", isHidden(derivateID));
        json.addProperty("urnEnabled", urnEnabled());

        return json;
    }

    private static boolean isHidden(String derivateID) {
        MCRDerivate derObj = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateID));
        Document xml = derObj.createXML();
        XPathFactory xpF = XPathFactory.instance();
        XPathExpression<Element> xpE = xpF.compile("mycorederivate/derivate", Filters.element());
        Element derivateNode = (Element) xpE.evaluateFirst(xml);
        Attribute displayAttr = derivateNode.getAttribute("display");
        if (displayAttr != null) {
            return Boolean.parseBoolean(displayAttr.getValue());
        }
        return false;
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

        if(rootDir == null){
            return "";
        }


        MCRFile mcrFile = (MCRFile) rootDir.getChildByPath(path);
        if (mcrFile != null) {
            return mcrFile.getMD5();
        }
        return "";
    }

    private static boolean urnEnabled() {
        String urnObjects = "";
        try {
            urnObjects = MCRConfiguration.instance().getString("MCR.URN.Enabled.Objects");
        } catch (MCRConfigurationException e) {
            LOGGER.info("Property MCR.URN.Enabled.Object not set, URN allocation not possible.");
        }
        if (urnObjects.contains("derivate")) {
            return true;
        }
        return false;
    }

}
