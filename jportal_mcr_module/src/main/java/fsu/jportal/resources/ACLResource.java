package fsu.jportal.resources;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.hibernate.Session;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.mycore.backend.hibernate.MCRHIBConnection;
import org.mycore.backend.hibernate.tables.MCRACCESSRULE;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

import fsu.jportal.config.ResourceSercurityConf;
import fsu.jportal.gson.AccessRuleList;
import fsu.jportal.gson.GsonManager;
import fsu.jportal.gson.AccessRuleListTypeAdapter;
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
        return gsonManager.createGson().toJson(new RegResourceCollection(resourceRegister, info.getAbsolutePath()));
    }
    
    @GET
    @Path("rules")
    @Produces(MediaType.APPLICATION_JSON)
    public String getRules(){
        MCRSession currentSession = MCRSessionMgr.getCurrentSession();
        currentSession.beginTransaction();
    	new SchemaUpdate(MCRHIBConnection.instance().getConfiguration()).execute(true, true);
    	Session session = MCRHIBConnection.instance().getSession();
        List<MCRACCESSRULE> ruleList = session.createCriteria(MCRACCESSRULE.class).list();
        GsonManager gsonManager = GsonManager.instance();
        gsonManager.registerAdapter(new AccessRuleListTypeAdapter());
        String json = gsonManager.createGson().toJson(new AccessRuleList(ruleList, info.getAbsolutePath()));
        currentSession.commitTransaction();
        return json;
    }
    
    @GET
    @Path("rsc/{rscID}")
    @Produces(MediaType.APPLICATION_JSON)
    public String resourceMethods(@PathParam("rscID") String rscID){
        List<String> resourceMethods = ResourceSercurityConf.instance().getResourceRegister().get(rscID);
        GsonManager gsonManager = GsonManager.instance();
        gsonManager.registerAdapter(new RegResourceCollectionTypeAdapter());
        //return gsonManager.createGson().toJson(new RegResourceCollection(resourceMethods, info.getAbsolutePath()));
        return "";
    }
    
    @GET
    @Path("rsc/{rscID}/{methodID: .+}")
    @Produces(MediaType.APPLICATION_JSON)
    public String resourcePerm(@PathParam("rscID") String rscID, @PathParam("methodID") String methodID){
        
        return rscID + " # " + methodID;
    }
    
    @PUT
    @Path("rsc/{rscID}/{methodID: .+}")
    public void updateAccess(String rid){
        System.out.println("### new rid: " + rid);
    }
}
