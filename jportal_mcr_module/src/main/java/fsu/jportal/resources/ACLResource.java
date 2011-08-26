package fsu.jportal.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import fsu.jportal.config.ResourceSercurityConf;
import fsu.jportal.gson.GsonManager;
import fsu.jportal.gson.RegResourceCollection;
import fsu.jportal.gson.RegResourceCollectionTypeAdapter;

@Path("acl")
public class ACLResource {
    @Context UriInfo info;
    
    @GET
    @Path("rsc")
    @Produces(MediaType.APPLICATION_JSON)
    public String resourceClasses(){
        Map<String, List<String>> resourceRegister = ResourceSercurityConf.instance().getResourceRegister();
        Set<String> keySet = resourceRegister.keySet();
        GsonManager gsonManager = GsonManager.instance();
        gsonManager.registerAdapter(new RegResourceCollectionTypeAdapter());
        return gsonManager.createGson().toJson(new RegResourceCollection(keySet, info.getAbsolutePath()));
    }
    
    @GET
    @Path("rsc/{rscID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String resourceMethods(@PathParam("rscID") String rscID){
        List<String> resourceMethods = ResourceSercurityConf.instance().getResourceRegister().get(rscID);
        GsonManager gsonManager = GsonManager.instance();
        gsonManager.registerAdapter(new RegResourceCollectionTypeAdapter());
        return gsonManager.createGson().toJson(new RegResourceCollection(resourceMethods, info.getAbsolutePath()));
    }
    
    @GET
    @Path("rsc/{rscID}/{methodID: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public String resourcePerm(@PathParam("rscID") String rscID, @PathParam("methodID") String methodID){
        
        return rscID + " # " + methodID;
    }
}
