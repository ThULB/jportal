package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.frontend.cli.JPortalCommands;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import fsu.jportal.backend.Derivate;
import fsu.jportal.gson.DerivateTypeAdapter;
import fsu.jportal.gson.FileNodeWraper;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;

@Path("filebrowser")
@MCRRestrictedAccess(ResourceAccess.class)
public class Filebrowser {
    private MCRJSONManager gsonManager;

    public Filebrowser() {
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
    @Path("gui/{derivID}{path:(/.*)*}")
    @Produces(MediaType.TEXT_HTML)
    public Response gui(@PathParam("derivID") String derivID) {
        //        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        //        if(rootNode == null){
        //            return Response.status(Status.NOT_FOUND).build();
        //        }
        InputStream mainGui = getClass().getResourceAsStream("/gui/main.html");
        return Response.ok(mainGui).build();
    }

    @DELETE
    @Path("{derivID}{path:(/.*)*}")
    //    @MCRRestrictedAccess(ResourceAccess.class)
    public Response deleteFile(@PathParam("derivID") String derivID, @PathParam("path") String path) {
        if (MCRAccessManager.checkPermission(derivID, "delete")) {

        }
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(derivID);
        if (rootNode == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (rootNode instanceof MCRDirectory && path != null && !"".equals(path.trim())) {
            MCRFilesystemNode node = ((MCRDirectory) rootNode).getChildByPath(path);
            if (node == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            node.delete();
            return Response.ok().build();
        }
        //        } else {
        //            response.sendError(HttpServletResponse.SC_FORBIDDEN,
        //                MessageFormat.format("User has not the \"" + PERMISSION_DELETE + "\" permission on object {0}.", derivateId));
        //        }

        return Response.serverError().build();
    }

    @POST
    @Path("rename")
    public Response rename(@QueryParam("newFile") String newFile, @QueryParam("oldFile") String oldFile) {
        JPortalCommands.renameFileInIFS(oldFile, newFile);
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
}