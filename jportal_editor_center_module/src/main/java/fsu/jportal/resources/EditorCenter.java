package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.mycore.frontend.cli.MCRJPortalRedundancyCommands;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fsu.jportal.gson.GsonManager;

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
        Gson gson = GsonManager.instance().createGson();
        JsonObject numDubletJson = new JsonObject();
        numDubletJson.addProperty("num", numOfDoublets);
        return gson.toJson(numDubletJson);
    }
}
