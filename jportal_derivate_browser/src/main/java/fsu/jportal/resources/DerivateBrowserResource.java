package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
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
import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.fileupload.MCRUploadHandlerIFS;
import org.mycore.frontend.fileupload.MCRUploadHelper;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.DocumentTools;
import fsu.jportal.backend.JPUploader;
import fsu.jportal.backend.MetaDataTools;

@Path("derivatebrowser")
public class DerivateBrowserResource {

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
    public Response browsePath(@PathParam("derivID") String derivID, @PathParam("path") String path,
        @DefaultValue("false") @QueryParam("noChilds") boolean noChilds) {
        JsonObject derivateJson = DerivateTools.getDerivateAsJson(derivID, path, noChilds);
        if (derivateJson == null) {
            return Response.serverError().build();
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
        return MetaDataTools.transformMCRWebPage(request,
            "/META-INF/resources/modules/derivate-browser/gui/derivatebrowserCompact.xml");
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
    public Response createFolder(@PathParam("derivID") String derivID, @PathParam("path") String path) {
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
    public Response existsCheck(String data) {
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
    @Path("startUpload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response startUpload(@QueryParam("documentID") String documentID,
        @QueryParam("derivateID") String derivateID, @QueryParam("num") int num) {
        if (derivateID.equals("")) {
            derivateID = null;
        }
        UUID uuid = JPUploader.start(documentID, derivateID, num);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uploadID", uuid.toString());
        return Response.ok(jsonObject.toString()).build();
    }

    @POST
    @Path("finishUpload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Response finishUpload(@QueryParam("uploadID") String uploadID) {
        try {
            JPUploader.finish(UUID.fromString(uploadID));
        } catch (Exception exc) {
            throw new InternalServerErrorException("Unable to finish upload with id " + uploadID, exc);
        }
        return Response.ok().build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("upload")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response getUpload(@FormDataParam("uploadID") String uploadID,
        @FormDataParam("file") InputStream inputStream, @FormDataParam("filename") String filename,
        @FormDataParam("size") long filesize, @FormDataParam("path") String path,
        @FormDataParam("overwrite") boolean overwrite, @FormDataParam("type") String type) {

        String filePath = path + "/" + filename;
        MCRUploadHelper.checkPathName(filePath.startsWith("/") ? filePath.substring(1) : filePath);
        UUID uuid = UUID.fromString(uploadID);
        MCRUploadHandlerIFS uploadHandler = JPUploader.get(uuid);
        if (uploadHandler == null) {
            throw new WebApplicationException(
                Response.status(Status.NOT_FOUND).entity("No upload handler with id " + uploadID + " found!").build());
        }
        type = type.toLowerCase();

        // TODO: ignore supported files types because there are some issues with windows 7 and
        // firefox V. 49.0.1. the type is octet stream and not pdf
        // List<String> fileTypes = CONFIG.getStrings("MCR.Derivate.Upload.SupportedFileTypes");
        // if (fileTypes.contains(type)) {
        if (overwrite) {
            MCRPath mcrFilePath = MCRPath.getPath(uploadHandler.getDocumentID(), path + "/" + filename);
            if (DerivateTools.delete(mcrFilePath) != 1) {
                throw new WebApplicationException(
                    "Unable to delete/overwrite " + filePath + " while uploading " + filename);
            }
        }

        try {
            JPUploader.upload(uuid, filePath, inputStream, filesize);
        } catch (Exception e) {
            throw new WebApplicationException("Error while uploading file " + filename, e);
        }
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("uploadID", uploadID);
        jsonObject.addProperty("derivateID", uploadHandler.getDerivateID());
        jsonObject.addProperty("md5", DerivateTools.getMD5forFile(uploadHandler.getDerivateID(), filePath));

        return Response.ok(jsonObject.toString()).build();
        // }
        // throw new WebApplicationException(
        //    new MCRException("Unsupported media type " + type + ". Only one of " + fileTypes + " is allowed."),
        //    Status.UNSUPPORTED_MEDIA_TYPE);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("addURN")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response addURN(String data) {
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

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("renameMultiple")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response renameMultiple(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();

        try {
            Map<String, String> resultMap = DerivateTools.renameFiles(jsonObject.get("deriID").getAsString(),
                jsonObject.get("pattern").getAsString(), jsonObject.get("newName").getAsString());

            JsonArray jsonArray = new JsonArray();
            for (Map.Entry<String, String> entry : resultMap.entrySet()) {
                JsonObject renameJson = new JsonObject();
                renameJson.addProperty("oldName", entry.getKey());
                renameJson.addProperty("newName", entry.getValue());
                jsonArray.add(renameJson);
            }

            return Response.ok(jsonArray.toString()).build();

        } catch (Exception e) {
            throw new WebApplicationException("Error while renaming!", e);
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("renameMultiple/test")
    @MCRRestrictedAccess(DerivateBrowserPermission.class)
    public Response renameMultipleTest(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();

        String filename = jsonObject.get("fileName").getAsString();
        String pattern = jsonObject.get("pattern").getAsString();
        String newName = jsonObject.get("newName").getAsString();
        String newFilename = "";

        try {
            Pattern patternObj = Pattern.compile(pattern);
            Matcher matcher = patternObj.matcher(filename);
            newFilename = matcher.replaceAll(newName);
            LOGGER.info("The file {} will be renamed to {}", filename, newFilename);
        } catch (PatternSyntaxException e) {
            LOGGER.info("The pattern '{}' contains errors!", pattern);
        } catch (IndexOutOfBoundsException e) {
            LOGGER.info("The file {} can't be renamed to {}. To many groups!", filename, newName);
        } catch (IllegalArgumentException e) {
            LOGGER.info("The new name '{}' contains illegal characters!", newName);
        }

        return Response.ok(newFilename).build();
    }

    @GET
    @Path("checkFileType/{derivID}{path:(/.*)*}")
    public Response checkFileType(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        return Response.ok(DerivateTools.checkFileType(derivID, path)).build();
    }

}
