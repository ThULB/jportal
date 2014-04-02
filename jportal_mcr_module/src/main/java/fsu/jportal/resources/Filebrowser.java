package fsu.jportal.resources;

import static org.mycore.access.MCRAccessManager.PERMISSION_DELETE;

import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.MessageFormat;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

import fsu.jportal.gson.MCRDirectoryTypeAdapter;
import fsu.jportal.gson.MCRFilesystemNodeTypeAdapter;

@Path("filebrowser")
public class Filebrowser {
    private MCRJSONManager gsonManager;

    public Filebrowser() {
        gsonManager = MCRJSONManager.instance();
        gsonManager.registerAdapter(new MCRFilesystemNodeTypeAdapter());
        gsonManager.registerAdapter(new MCRDirectoryTypeAdapter());
    }

    @GET
    @Path("{id}{path:(/.*)*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response browsePath(@PathParam("id") String id, @PathParam("path") String path) {
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        if (rootNode == null) {
            return Response.status(Status.NOT_FOUND).build();
        }

        if (rootNode instanceof MCRDirectory && path != null && !"".equals(path.trim())) {
            MCRFilesystemNode node = ((MCRDirectory) rootNode).getChildByPath(path);
            if (node == null) {
                return Response.status(Status.NOT_FOUND).build();
            }

            return createJSON(node);
        }

        return createJSON(rootNode);
    }

    private Response createJSON(MCRFilesystemNode node) {
        Type nodeType = MCRFilesystemNode.class;

        if (node instanceof MCRDirectory) {
            nodeType = MCRDirectory.class;
        }

        String json = gsonManager.createGson().toJson(node, nodeType);
        return Response.ok(json).build();
    }

    @GET
    @Path("gui/{id}{path:(/.*)*}")
    @Produces(MediaType.TEXT_HTML)
    public Response gui(@PathParam("id") String id) {
        //        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        //        if(rootNode == null){
        //            return Response.status(Status.NOT_FOUND).build();
        //        }
        InputStream mainGui = getClass().getResourceAsStream("/gui/main.html");
        return Response.ok(mainGui).build();
    }

    @DELETE
    @Path("{id}{path:(/.*)*}")
    public Response deleteFile(@PathParam("id") String id, @PathParam("path") String path) {
        //        if (MCRAccessManager.checkPermission(derivateId, PERMISSION_DELETE)) {
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
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
    public Response rename(@QueryParam("newFile") String newFile, @QueryParam("oldFile") String oldFile){
        JPortalCommands.renameFileInIFS(oldFile, newFile);
        return Response.ok().build();
    }
}