package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jdom2.JDOMException;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.cli.MCRDerivateCommands;
import org.mycore.frontend.cli.MCRObjectCommands;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.frontend.jersey.MCRJerseyUtil;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.mycore.frontend.util.DerivateLinkUtil;
import org.mycore.urn.services.MCRURNAdder;
import org.mycore.urn.services.MCRURNManager;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.multipart.FormDataParam;

import fsu.jportal.backend.Derivate;
import fsu.jportal.backend.DerivateTools;
import fsu.jportal.gson.DerivateTypeAdapter;
import fsu.jportal.gson.FileNodeWraper;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;

@Path("derivatebrowser")
public class DerivateBrowserResource {
    private MCRJSONManager gsonManager;

    private static final String dateFormat = "dd.MM.yyyy HH:mm:ss";

    private static final DateFormat dateFormatter = new SimpleDateFormat(dateFormat);

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Context
    ServletContext context;

    public DerivateBrowserResource() {
        gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new DerivateTypeAdapter());
        gsonManager.registerAdapter(new MCRFilesystemNodeTypeAdapter());
    }

    @GET
    @Path("{derivID}{path:(/.*)*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response browsePath(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        Derivate derivate = new Derivate(derivID);
        MCRFilesystemNode node;
        MCRPath mcrPath = MCRPath.getPath(derivID, path);
        if (path != null && !"".equals(path.trim())) {
            node = derivate.getChildByPath(path);
        } else {
            node = derivate.getRootDir();
        }
        if (!Files.exists(mcrPath)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        String maindoc = derivate.getMaindoc();
        FileNodeWraper wrapper = new FileNodeWraper(node, maindoc);
        JsonObject json = gsonManager.createGson().toJsonTree(wrapper).getAsJsonObject();
        if (path != null && !"".equals(path.trim())) {
            json.addProperty("parentName", derivate.getRootDir().getName());
            json.addProperty("parentSize", derivate.getRootDir().getSize());
            json.addProperty("parentLastMod", dateFormatter.format(derivate.getRootDir().getLastModified().getTime()));
        }
        return Response.ok(json.toString()).build();
    }

    @GET
    @Path("start")
    @Produces(MediaType.TEXT_HTML)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response start(@PathParam("derivID") String derivID) {
        InputStream mainGui = getClass().getResourceAsStream(
                "/META-INF/resources/modules/derivate-browser/gui/derivatebrowser.html");
        return Response.ok(mainGui).build();
    }

    @DELETE
    @Path("{derivID}{path:(/.*)*}")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response deleteDoc(@PathParam("derivID") String docID) {
        MCRObjectID mcrId = MCRJerseyUtil.getID(docID);
        if (mcrId.getTypeId().equals("derivate")) {
            MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrId);
            MCRMetadataManager.delete(mcrDer);
        } else {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
            try {
                MCRMetadataManager.delete(mcrObj);
            } catch (MCRActiveLinkException mcrActExc) {
                return Response.status(Status.FORBIDDEN).entity(mcrActExc.getMessage()).build();
            }
        }
        return Response.ok().build();
    }

    @DELETE
    @Path("multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response deleteFiles(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonO = jsonArray.get(i).getAsJsonObject();
            int status;
            status = DerivateTools.delete(MCRPath.getPath(jsonO.get("deriID").getAsString(), jsonO.get("path")
                    .getAsString()));
            jsonO.addProperty("status", status);
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("{derivID}{path:(/.*)*}")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response createFolder(@PathParam("derivID") String derivID, @PathParam("path") String path) throws Exception {
        if (!DerivateTools.mkdir(derivID + ":" + path)) {
            return Response.status(Status.CONFLICT).build();
        }
        return Response.ok().build();
    }

    @POST
    @Path("rename")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response rename(@QueryParam("file") String file, @QueryParam("name") String name,
            @QueryParam("mainFile") String start) {
        try {
            DerivateTools.rename(file, name);
        } catch (FileAlreadyExistsException e) {
            return Response.status(Status.CONFLICT).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        if (Boolean.parseBoolean(start)) {
            String path = file.substring(0, file.lastIndexOf("/") + 1) + name;
            Derivate derivate = new Derivate(file.substring(0, file.lastIndexOf(":")));
            derivate.setMaindoc(path.substring(path.lastIndexOf(":") + 1));
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("moveDeriFiles")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response moveDeriFiles(String data) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        String moveTo = jsonObject.get("moveTo").getAsString();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonFile = jsonArray.get(i).getAsJsonObject();
            if (!jsonFile.get("file").getAsString().equals(moveTo)) {
                if (DerivateTools.mv(jsonFile.get("file").getAsString(), moveTo)) {
                    jsonFile.addProperty("status", "1");
                } else {
                    jsonFile.addProperty("status", "0");
                }
            }
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @PUT
    @Path("{derivID}{path:(/.*)*}/main")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response setMainDoc(@PathParam("derivID") String derivID, @PathParam("path") String path) throws IOException {
        Derivate derivate = new Derivate(derivID);
        derivate.setMaindoc(path);
        return Response.ok().build();
    }

    @GET
    @Path("deriid/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeriID(@PathParam("derivID") String derivID) throws IOException, JDOMException, SAXException {
        MCRObjectID mcrid = MCRObjectID.getInstance(derivID);
        List<MCRObjectID> derivateIds = MCRMetadataManager.getDerivateIds(mcrid, 0, TimeUnit.MILLISECONDS);
        JsonArray jsonArray = new JsonArray();
        for (MCRObjectID objid : derivateIds) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", objid.toString());
            jsonArray.add(jsonObj);
        }
        return Response.ok(jsonArray.toString()).build();
    }

    @GET
    @Path("folders/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolders(@PathParam("derivID") String derivID) throws IOException, JDOMException, SAXException {
        Derivate derivate = new Derivate(derivID);
        MCRDirectory node = derivate.getRootDir();

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
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("exists")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response existsCheck(String data) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        String deriID = jsonObject.get("deriID").getAsString();
        String path = jsonObject.get("path").getAsString();
        Derivate derivate = new Derivate(deriID);
        MCRDirectory node;
        List<String> fileTyps = CONFIG.getStrings("MCR.Derivate.Upload.SupportedFileTypes");
        if (path != null && !"".equals(path.trim())) {
            node = (MCRDirectory) derivate.getChildByPath(path);
        } else {
            node = derivate.getRootDir();
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonFile = jsonArray.get(i).getAsJsonObject();
            if (fileTyps.contains(jsonFile.get("fileType").getAsString())) {
                if (node.hasChild(jsonFile.get("file").getAsString())) {
                    MCRFilesystemNode child = node.getChild(jsonFile.get("file").getAsString());
                    JsonObject childJson = new JsonObject();
                    childJson.addProperty("name", child.getName());
                    childJson.addProperty("size", child.getSize());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
                    childJson.addProperty("lastmodified", sdf.format(child.getLastModified().getTime()));
                    jsonFile.addProperty("exists", "1");
                    jsonFile.add("existingFile", childJson);
                } else {
                    jsonFile.addProperty("exists", "0");
                }
            } else {
                jsonFile.addProperty("exists", "2");
            }
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("upload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response getUpload(@FormDataParam("file") InputStream inputStream,
            @FormDataParam("filename") String filename, @FormDataParam("size") long filesize,
            @FormDataParam("documentID") String documentID, @FormDataParam("derivateID") String derivateID,
            @FormDataParam("path") String path, @FormDataParam("overwrite") boolean overwrite) {
        if (overwrite) {
            if (DerivateTools.delete(MCRPath.getPath(documentID, path + "/" + filename)) != 1) {
                return Response.serverError().build();
            }
        }
        if (derivateID.equals("")) {
            derivateID = null;
        }
        String filePath = path + "/" + filename;

        try {
            derivateID = DerivateTools.uploadFile(inputStream, filesize, documentID, derivateID, filePath);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.serverError().build();
        }
        return Response.ok(derivateID).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addURN")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response addURN(String data) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        String deriID = jsonObject.get("deriID").getAsString();
        Boolean all = jsonObject.get("completeDeri").getAsBoolean();
        MCRURNAdder urnAdder = new MCRURNAdder();
        if (all) {
            if (urnAdder.addURNToDerivates(deriID)) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonO = jsonArray.get(i).getAsJsonObject();
            String path = jsonO.get("path").getAsString();
            if (urnAdder.addURNToSingleFile(deriID, path)) {
                jsonO.addProperty("URN", getURNforFile(deriID, path));
            } else {
                jsonO.addProperty("URN", "");
            }
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @PUT
    @Path("moveDocs")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response moveDocs(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String objID = jsonObject.get("objId").getAsString();
            String newParentID = jsonObject.get("newParentId").getAsString();
            if (!objID.equals(newParentID)) {
                try {
                    if (objID.contains("derivate")) {
                        MCRDerivateCommands.linkDerivateToObject(objID, newParentID);
                    } else {
                        MCRObjectCommands.replaceParent(objID, newParentID);
                    }
                } catch (MCRPersistenceException e) {
                    return Response.status(Status.UNAUTHORIZED).build();
                } catch (Exception e) {
                    e.printStackTrace();
                    e.printStackTrace();
                    return Response.serverError().build();
                }
            }

        }
        return Response.ok().build();
    }

    @POST
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response setLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null && !imgPath.equals("")) {
            try {
                DerivateLinkUtil.setLink(MCRJerseyUtil.getID(docID), imgPath);
            } catch (MCRActiveLinkException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    @DELETE
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response removeLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null && !imgPath.equals("")) {
            try {
                DerivateLinkUtil.removeLink(MCRJerseyUtil.getID(docID), imgPath);
            } catch (MCRActiveLinkException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
            return Response.ok().build();
        } else {
            return Response.serverError().build();
        }
    }

    private JsonArray getFolderChildren(MCRFilesystemNode[] childs) {
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

    private String getURNforFile(String derivate, String path) {
        String fileName = path;
        String pathToFile = "/";
        if (path.contains("/")) {
            pathToFile = path.substring(0, path.lastIndexOf("/") + 1);
            fileName = path.substring(path.lastIndexOf("/") + 1);
        }
        return MCRURNManager.getURNForFile(derivate, pathToFile, fileName);
    }
}
