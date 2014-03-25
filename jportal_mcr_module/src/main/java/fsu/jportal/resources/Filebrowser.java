package fsu.jportal.resources;

import java.lang.reflect.Type;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.common.MCRJSONManager;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;

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
    public Response browsePath(@PathParam("id") String id, @PathParam("path") String path){
        MCRFilesystemNode rootNode = MCRFilesystemNode.getRootNode(id);
        if(rootNode == null){
            return Response.status(Status.NOT_FOUND).build();
        }
        
        if(rootNode instanceof MCRDirectory && path != null && !"".equals(path.trim())){
                MCRFilesystemNode node = ((MCRDirectory)rootNode).getChildByPath(path);
                if(node == null){
                    return Response.status(Status.NOT_FOUND).build();
                }
                
                return createJSON(node);
        }
        
        return createJSON(rootNode);
    }

    private Response createJSON(MCRFilesystemNode node) {
        Type nodeType = MCRFilesystemNode.class;
        
        if(node instanceof MCRDirectory){
            nodeType = MCRDirectory.class;
        }
        
        String json = gsonManager.createGson().toJson(node, nodeType);
        return Response.ok(json).build();
    }
}
