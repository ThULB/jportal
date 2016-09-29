package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.List;

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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.DocumentTools;

@Path("derivatebrowser")
public class DerivateBrowserResource {

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

    static Logger LOGGER = LogManager.getLogger(DerivateBrowserResource.class);

    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;

    @Context
    ServletContext context;

    @GET
    @Path("{derivID}{path:(/.*)*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response browsePath(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        JsonObject derivateJson = DerivateTools.getDerivateAsJson(derivID, path);
        if (derivateJson == null) {
            Response.serverError().build();
        }
        return Response.ok(derivateJson.toString()).build();
    }

    @GET
    @Path("start")
    @Produces(MediaType.TEXT_HTML)
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response start() {
        InputStream mainGui = getClass().getResourceAsStream(
            "/META-INF/resources/modules/derivate-browser/gui/derivatebrowser.html");
        return Response.ok(mainGui).build();
    }

    @GET
    @Path("compact")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public byte[] startCompact() throws Exception {
        return transform("/META-INF/resources/modules/derivate-browser/gui/derivatebrowserCompact.xml");
    }

    @DELETE
    @Path("docs")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response deleteDocs(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonArray jsonArray = jsonParser.parse(data).getAsJsonArray();
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonObject = jsonArray.get(i).getAsJsonObject();
            String objID = jsonObject.get("objId").getAsString();
            int respID = DocumentTools.delete(objID);
            jsonObject.addProperty("status", respID);
        }
        return Response.ok(jsonArray.toString()).build();
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
            status = DerivateTools.delete(
                MCRPath.getPath(jsonO.get("deriID").getAsString(), jsonO.get("path").getAsString()));
            jsonO.addProperty("status", status);
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("{derivID}{path:(/.*)*}")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response createFolder(@PathParam("derivID") String derivID, @PathParam("path") String path)
        throws Exception {
        if (!DerivateTools.mkdir(derivID + ":" + path)) {
            throw new WebApplicationException(derivID + ":" + path, Status.CONFLICT);
        }
        return Response.ok().build();
    }

    @POST
    @Path("rename")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response rename(@QueryParam("file") String file, @QueryParam("name") String name,
        @QueryParam("mainFile") Boolean start) {
        try {
            DerivateTools.rename(file, name);
        } catch (FileAlreadyExistsException e) {
            throw new WebApplicationException(file + " does already exists", e, Status.CONFLICT);
        } catch (IOException e) {
            throw new WebApplicationException("Unable to rename file " + file + " to " + name, e);
        } catch (MCRAccessException e) {
            throw new WebApplicationException(Status.UNAUTHORIZED);
        }
        if (start) {
            String path = file.substring(0, file.lastIndexOf("/") + 1) + name;
            DerivateTools.setAsMain(file.substring(0, file.lastIndexOf(":")),
                path.substring(path.lastIndexOf(":") + 1));
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
    public Response setMainDoc(@PathParam("derivID") String deriID, @PathParam("path") String path) {
        DerivateTools.setAsMain(deriID, path);
        return Response.ok().build();
    }

    @GET
    @Path("folders/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolders(@PathParam("derivID") String deriID) {
        return Response.ok(DerivateTools.getDerivateFolderAsJson(deriID).toString()).build();
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
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonFile = jsonArray.get(i).getAsJsonObject();
            JsonObject childJson = DerivateTools.getChildAsJson(deriID, path, jsonFile.get("file").getAsString());
            if (childJson != null) {
                jsonFile.addProperty("exists", "1");
                jsonFile.add("existingFile", childJson);
            } else {
                jsonFile.addProperty("exists", "0");
            }
        }
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response getUpload(@FormDataParam("file") InputStream inputStream,
        @FormDataParam("filename") String filename, @FormDataParam("size") long filesize,
        @FormDataParam("documentID") String documentID, @FormDataParam("derivateID") String derivateID,
        @FormDataParam("path") String path, @FormDataParam("overwrite") boolean overwrite,
        @FormDataParam("type") String type) {
        type = type.toLowerCase();
        List<String> fileTypes = CONFIG.getStrings("MCR.Derivate.Upload.SupportedFileTypes");
        if (fileTypes.contains(type)) {
            if (overwrite) {
                MCRPath filePath = MCRPath.getPath(documentID, path + "/" + filename);
                if (DerivateTools.delete(filePath) != 1) {
                    throw new WebApplicationException(
                        "Unable to delete/overwrite " + filePath + " while uploading " + filename);
                }
            }
            if (derivateID.equals("")) {
                derivateID = null;
            }
            String filePath = path + "/" + filename;

            try {
                derivateID = DerivateTools.uploadFile(inputStream, filesize, documentID, derivateID, filePath);
            } catch (Exception e) {
                throw new WebApplicationException("Error while uploading file " + filename, e);
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("derivateID", derivateID);
            jsonObject.addProperty("md5", DerivateTools.getMD5forFile(derivateID, filePath));

            return Response.ok(jsonObject.toString()).build();
        }
        throw new WebApplicationException(
            new MCRException("Unsupported media type " + type + ". Only one of " + fileTypes + " is allowed."),
            Status.UNSUPPORTED_MEDIA_TYPE);
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
        if (all) {
            if (DerivateTools.addURNToDerivate(deriID)) {
                return Response.ok().build();
            }
            return Response.serverError().build();
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonO = jsonArray.get(i).getAsJsonObject();
            jsonO.addProperty("URN", DerivateTools.addURNToFile(deriID, jsonO.get("path").getAsString()));
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
            Boolean success = DocumentTools.move(objID, newParentID);
            jsonObject.addProperty("success", success);
        }
        return Response.ok(jsonArray.toString()).build();
    }

    @POST
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response setLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null
            && !imgPath.equals("")) {
            try {
                DerivateTools.setLink(docID, imgPath);
            } catch (MCRActiveLinkException e) {
                throw new WebApplicationException("Unable to set link " + imgPath + " from " + docID, e);
            } catch (MCRAccessException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
            return Response.ok().build();
        } else {
            throw new WebApplicationException("Unable to set link " + imgPath + " from " + docID);
        }
    }

    @DELETE
    @Path("link")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response removeLink(@QueryParam("docID") String docID, @QueryParam("imgPath") String imgPath) {
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null
            && !imgPath.equals("")) {
            try {
                DerivateTools.removeLink(docID, imgPath);
            } catch (MCRActiveLinkException e) {
                throw new WebApplicationException("Unable to remove link " + imgPath + " from " + docID, e);
            } catch (MCRAccessException e) {
                throw new WebApplicationException(Status.UNAUTHORIZED);
            }
            return Response.ok().build();
        } else {
            throw new WebApplicationException("Unable to remove link " + imgPath + " from " + docID);
        }
    }

    @POST
    @Path("tileDerivate")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response tileDerivate(@QueryParam("deriID") String deriID) {
        if (DerivateTools.tileDerivate(deriID)) {
            return Response.ok().build();
        }
        return Response.serverError().build();
    }

    protected byte[] transform(String xmlFile) throws Exception {
        InputStream is = getClass().getResourceAsStream(xmlFile);
        if (is == null) {
            LOGGER.error("Unable to locate xmlFile of move object resource");
            throw new WebApplicationException(Response.status(Status.INTERNAL_SERVER_ERROR).build());
        }
        SAXBuilder saxBuilder = new SAXBuilder();
        Document webPage = saxBuilder.build(is);
        MCRJDOMContent source = new MCRJDOMContent(webPage);
        MCRParameterCollector parameter = new MCRParameterCollector(request);
        MCRContentTransformer transformer = MCRLayoutService.getContentTransformer("MyCoReWebPage", parameter);
        MCRContent result;
        if (transformer instanceof MCRParameterizedTransformer) {
            result = ((MCRParameterizedTransformer) transformer).transform(source, parameter);
        } else {
            result = transformer.transform(source);
        }
        return result.asByteArray();
    }
}
