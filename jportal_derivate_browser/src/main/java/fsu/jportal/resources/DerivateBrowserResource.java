package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;
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
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRJSONManager;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.MCRJDOMContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.content.transformer.MCRParameterizedTransformer;
import org.mycore.common.xml.MCRLayoutService;
import org.mycore.common.xsl.MCRParameterCollector;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
//import org.mycore.urn.hibernate.MCRURN;
//import org.mycore.urn.services.MCRURNManager;
import org.xml.sax.SAXException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fsu.jportal.backend.Derivate;
import fsu.jportal.backend.DerivateTools;
import fsu.jportal.backend.MetaDataTools;
import fsu.jportal.gson.DerivateTypeAdapter;
import fsu.jportal.gson.FileNodeWraper;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;

@Path("derivatebrowser")
public class DerivateBrowserResource {
    private MCRJSONManager gsonManager;
    
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
        if (path != null && !"".equals(path.trim())) {
            node = derivate.getChildByPath(path);
        } else {
            node = derivate.getRootDir();
        }

        if (node == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        String maindoc = derivate.getMaindoc();
        FileNodeWraper wrapper = new FileNodeWraper(node, maindoc);
        String json = gsonManager.createGson().toJson(wrapper);
        return Response.ok(json).build();
    }
    
     @GET
     @Path("start")
     @Produces(MediaType.TEXT_HTML)
     public Response start(@PathParam("derivID") String derivID) {
         InputStream mainGui = getClass().getResourceAsStream("/META-INF/resources/modules/derivate-browser/gui/derivatebrowser.html");
         return Response.ok(mainGui).build();
     }

    @DELETE
    @Path("{derivID}{path:(/.*)*}")
    //    @MCRRestrictedAccess(ResourceAccess.class)
    public Response deleteFile(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        int status =  doDelete(derivID, path);
        if (status == 1) return Response.ok().build();
        if (status == 2) return Response.status(Status.NOT_FOUND).build();
        return Response.serverError().build();
    }
    
    @DELETE
    @Path("multiple")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    //    @MCRRestrictedAccess(ResourceAccess.class)
    public Response deleteFiles(String data) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject jsonO = jsonArray.get(i).getAsJsonObject();
            int status = doDelete( jsonO.get("deriID").getAsString(),  jsonO.get("path").getAsString());
            jsonO.addProperty("status", status);
        }
        return Response.ok(jsonObject.toString()).build();
    }
    
    @POST
    @Path("{derivID}{path:(/.*)*}")
    public Response createFolder(@PathParam("derivID") String derivID, @PathParam("path") String path) throws Exception {
        DerivateTools.mkdir(derivID + ":" + path);
        return Response.ok().build();
    }


    @POST
    @Path("rename")
    public Response rename(@QueryParam("file") String file, @QueryParam("name") String name) throws Exception {
        DerivateTools.rename(file, name);
        return Response.ok().build();
    }
    
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("move")
    public Response moveFiles(String data) throws Exception {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = jsonParser.parse(data).getAsJsonObject();
        JsonArray jsonArray = jsonObject.getAsJsonArray("files");
        String moveTo = jsonObject.get("moveTo").getAsString();
        for (int i = 0; i < jsonArray.size(); i++) {
            if (!jsonArray.get(i).getAsString().equals(moveTo)){
                DerivateTools.mv(jsonArray.get(i).getAsString(), moveTo);
            }
        }
        return Response.ok().build();
    }

    @PUT
    @Path("{derivID}{path:(/.*)*}/main")
    public Response setMainDoc(@PathParam("derivID") String derivID, @PathParam("path") String path) throws IOException {
        //        if (MCRAccessManager.checkPermission(derivID, PERMISSION_WRITE)) {
        Derivate derivate = new Derivate(derivID);
        derivate.setMaindoc(path);

        return Response.ok().build();

        //        } else {
        //            response.sendError(HttpServletResponse.SC_FORBIDDEN,
        //                MessageFormat.format("User has not the \"" + PERMISSION_WRITE + "\" permission on object {0}.", derivID));
        //        }
    }
    
//    @GET
//    @Path("{derivID}/urnUpdate")
//    public Response urnUpdate(@PathParam("derivID") String derivID) throws IOException {
//        List<MCRURN> urnList = MCRURNManager.get(MCRObjectID.getInstance(derivID));
//        for (MCRURN mcrurn : urnList) {
//            mcrurn.setRegistered(false);
//            MCRURNManager.update(mcrurn);
//        }
//        return Response.ok().build();
//    }
    
    @GET
    @Path("deriid/{derivID}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeriID(@PathParam("derivID") String derivID) throws IOException, JDOMException, SAXException {
//        MCRXMLMetadataManager metaManger = MCRXMLMetadataManager.instance();
        MCRObjectID mcrid =MCRObjectID.getInstance(derivID);
//        Document doc = metaManger.retrieveXML(mcrid);
        List<MCRObjectID> derivateIds = MCRMetadataManager.getDerivateIds(mcrid, 0, TimeUnit.MILLISECONDS);
        JsonArray jsonArray = new JsonArray();
        for (MCRObjectID objid : derivateIds){
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
        if (node.getNumChildren(2, 2) > 0){
            jsonObject.addProperty("hasChildren", true);
            jsonObject.add("children", getFolderChildren(node.getChildren()));
        }
        else{
            jsonObject.addProperty("hasChildren", false);
        }
               
        return Response.ok(jsonObject.toString()).build();
    }
    
    private JsonArray getFolderChildren(MCRFilesystemNode[] childs){
        JsonArray jsonArray = new JsonArray();
        for (MCRFilesystemNode child : childs){
            if (child instanceof MCRDirectory) {
                JsonObject jsonObj = new JsonObject();
                jsonObj.addProperty("name", child.getName());
                jsonObj.addProperty("absPath", child.getAbsolutePath());
                jsonObj.addProperty("isRoot", false);
                if (((MCRDirectory) child).getNumChildren(2, 1) > 0){
                    jsonObj.addProperty("hasChildren", true);
                    jsonObj.add("children", getFolderChildren(((MCRDirectory) child).getChildren()));
                }
                else{
                    jsonObj.addProperty("hasChildren", false);
                }
                jsonArray.add(jsonObj);
            }
        }
        return jsonArray;
    }
    
    
    private int doDelete(String derivID, String path){
        if (MCRAccessManager.checkPermission(derivID, "delete")) {

        }
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(derivID);
        if (rootNode == null) {
            return 2; //NOT_FOUND
        }

        if (rootNode instanceof MCRDirectory && path != null && !"".equals(path.trim())) {
            MCRFilesystemNode node = ((MCRDirectory) rootNode).getChildByPath(path);
            if (node == null) {
                return 2; //NOT_FOUND
            }

            node.delete();
            return 1; //OK
        }
        //        } else {
        //            response.sendError(HttpServletResponse.SC_FORBIDDEN,
        //                MessageFormat.format("User has not the \"" + PERMISSION_DELETE + "\" permission on object {0}.", derivateId));
        //        }

        return 0; //SERVER_ERROR
    }
}
