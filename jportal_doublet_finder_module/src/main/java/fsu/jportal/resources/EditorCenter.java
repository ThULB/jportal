package fsu.jportal.resources;

import java.io.InputStream;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.mycore.common.MCRJSONManager;
import org.mycore.frontend.cli.MCRJPortalRedundancyCommands;
import org.mycore.frontend.cli.MCRKnownCommands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

@Path("editorCenter")
public class EditorCenter {
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return this.getClass().getResourceAsStream("/editorCenter/" + filename);
    }

    @GET
    @Path("numDoubletsOf/{type}")
    public String getNumOfDoublets(@PathParam("type") String type){
        int numOfDoublets = MCRJPortalRedundancyCommands.getDoubletObjsOfType(type).getNumHits();
        Gson gson = MCRJSONManager.instance().createGson();
        JsonObject numDubletJson = new JsonObject();
        numDubletJson.addProperty("num", numOfDoublets);
        return gson.toJson(numDubletJson);
    }

    @POST
    @Path("rmDuplicates/{type}")
    @RolesAllowed("")
    public Response removeDuplicatesFor(@PathParam("type") String type){
        MCRKnownCommands mcrKnownCommands = new MCRKnownCommands();
        try {
            mcrKnownCommands.invokeCommand("jp clean up " + type);
            return Response.ok().build();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return Response.serverError().build();
        }
//        MCRJPortalRedundancyCommands.cleanUp(type);
    }
}
