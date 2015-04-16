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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jdom2.JDOMException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.multipart.FormDataParam;

import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.DocumentTools;

@Path("derivatebrowser")
public class DerivateBrowserResource {

    private static final MCRConfiguration CONFIG = MCRConfiguration.instance();

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
        if (derivateJson == null){
            Response.serverError().build();
        }
        return Response.ok(derivateJson.toString()).build();
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
            status = DerivateTools.delete(jsonO.get("deriID").getAsString(), jsonO.get("path").getAsString());
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
            @QueryParam("mainFile") Boolean start) {
        try {
            DerivateTools.rename(file, name);
        } catch (FileAlreadyExistsException e) {
            return Response.status(Status.CONFLICT).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.serverError().build();
        }
        if (start) {
            String path = file.substring(0, file.lastIndexOf("/") + 1) + name;
            DerivateTools.setAsMain(file.substring(0, file.lastIndexOf(":")), path.substring(path.lastIndexOf(":") + 1));
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
    public Response setMainDoc(@PathParam("derivID") String deriID, @PathParam("path") String path) throws IOException {
        DerivateTools.setAsMain(deriID, path);
        return Response.ok().build();
    }

    @GET
    @Path("folders/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFolders(@PathParam("derivID") String deriID) throws IOException, JDOMException, SAXException {
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
        List<String> fileTyps = CONFIG.getStrings("MCR.Derivate.Upload.SupportedFileTypes");
        if (fileTyps.contains(type)){
            if (overwrite) {
                if (DerivateTools.delete(documentID, path + "/" + filename) != 1) {
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
                e.printStackTrace();
                return Response.serverError().build();
            }
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("derivateID", derivateID);
            jsonObject.addProperty("md5", DerivateTools.getMD5forFile(derivateID, filePath));

            return Response.ok(jsonObject.toString()).build();
        }
        return Response.status(Status.UNSUPPORTED_MEDIA_TYPE).build();
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
        if (docID != null && !docID.equals("") && !docID.contains("derivate") && imgPath != null && !imgPath.equals("")) {
            try {
                DerivateTools.setLink(docID, imgPath);
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
                DerivateTools.removeLink(docID, imgPath);
            } catch (MCRActiveLinkException e) {
                e.printStackTrace();
                return Response.serverError().build();
            }
            return Response.ok().build();
        } else {
            return Response.serverError().build();
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
}
