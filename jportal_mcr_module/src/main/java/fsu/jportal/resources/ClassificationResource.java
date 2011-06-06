package fsu.jportal.resources;

import java.net.URI;
import java.util.List;
import java.util.UUID;

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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.wrapper.MCRCategoryListWrapper;

/**
 * This class is responsible for CRUD-operations of MCRCategories.
 * It accepts JSON objects of the form
 * <code>
 * [{    "ID":{"rootID":"abcd","categID":"1234"}
 *      "label":[
 *          {"lang":"de","text":"Rubriken Test 2 fuer MyCoRe","descriptions":"test de"},
 *          {"lang":"en","text":"Rubric test 2 for MyCoRe","descriptions":"test en"}
 *      ],
 *      "parentID":{"rootID":"abcd","categID":"parent"}
 *      "children:"URL"
 * 
 * }
 * ...
 * ]
 * </code>
 * @author chi
 *
 */
@Path("classifications")
public class ClassificationResource {
    private MCRSession currentSession = null;
    private boolean useSession = MCRConfiguration.instance().getBoolean("ClassificationResouce.useSession", true);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newClassification(String json) {
        openSession();
        
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryImpl category = gson.fromJson(json, MCRCategoryImpl.class);

        MCRCategoryID categoryID = generateIDIfNeeded(category);
        category.setId(categoryID);

        MCRCategoryID parentID = null;
        MCRCategory parentCateg = category.getParent();
        if (parentCateg != null) {
            parentID = parentCateg.getId();
        }
        getCategoryDAO().addCategory(parentID, category);
        URI uri = buildGetURI(categoryID.getRootID(), categoryID.getID());
        closeSession();
        return Response.created(uri).build();
    }

    private void openSession(){
        if (useSession) {
            currentSession = MCRSessionMgr.getCurrentSession();
            currentSession.beginTransaction();
        }
    }
    
    private void closeSession(){
        if (useSession) {
            currentSession.commitTransaction();
            currentSession.close();
            currentSession = null;
        }
    }
    
    private MCRCategoryDAO getCategoryDAO() {
        return MCRCategoryDAOFactory.getInstance();
    }

    private URI buildGetURI(String rootID, String categID) {
        UriBuilder uriBuilder = UriBuilder.fromPath("");
        addQueryParam("rootID", rootID, uriBuilder);
        addQueryParam("categID", categID, uriBuilder);
        
        URI uri = uriBuilder.build();
        return uri;
    }

    private void addQueryParam(String paramName, String paramStr, UriBuilder uriBuilder) {
        if(paramStr != null && !"".equals(paramStr)){
            uriBuilder.queryParam(paramName, paramStr);
        }
    }

    private MCRCategoryID generateIDIfNeeded(MCRCategory category) {
        MCRCategoryID categoryID = category.getId();
        if (categoryID == null) {
            categoryID = MCRCategoryID.rootID(UUID.randomUUID().toString());
        } else if (categoryID.isRootID() && category.getParent() != null) {
            categoryID = new MCRCategoryID(categoryID.getRootID(), UUID.randomUUID().toString());
        }
        return categoryID;
    }
    
    /**
     * @param idStr rootID|categID
     * @return
     */
    @GET
    @Path("id/{idStr}")
    @Produces(MediaType.APPLICATION_JSON)
    public String get(@PathParam("idStr") String idStr){
        openSession();
        String[] splittedID = idStr.split("\\.");
        
        String rootID = splittedID[0];
        String categID = null;
        if(splittedID.length > 1) {
            categID = splittedID[1];
        }
        
        MCRCategoryID id = toMCRCategID(rootID, categID);
        MCRCategory category = getCategoryDAO().getCategory(id, 1);
        Gson gson = GsonManager.instance().createGson();
        closeSession();
        return gson.toJson(category);
    }

    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChildren(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        openSession();
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryDAO mcrCategoryDAO = getCategoryDAO();
        
        if(rootID == null && categID == null){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        MCRCategoryID id = toMCRCategID(rootID, categID);
        List<MCRCategory> children = mcrCategoryDAO.getChildren(id);
        closeSession();
        return gson.toJson(children);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification() {
        openSession();
        Gson gson = GsonManager.instance().createGson();
        List<MCRCategory> rootCategories = getCategoryDAO().getRootCategories();
        return gson.toJson(new MCRCategoryListWrapper(rootCategories));
    }

    private MCRCategoryID toMCRCategID(String rootID, String categID) {
        if(categID == null){
            categID = "";
        }
        
        MCRCategoryID id = new MCRCategoryID(rootID, categID);
        return id;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateClassification(String json) {
        openSession();
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryImpl category = gson.fromJson(json, MCRCategoryImpl.class);

        try {
            getCategoryDAO().replaceCategory(category);
            closeSession();
            return Response.ok(json).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            closeSession();
            return Response.status(Status.NOT_FOUND).build();
        }

    }

    @DELETE
    public Response deleteClassification(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        openSession();
        MCRCategoryID mcrCategID = toMCRCategID(rootID, categID);

        try {
            getCategoryDAO().deleteCategory(mcrCategID);
            closeSession();
            return Response.status(Status.GONE).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            closeSession();
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
