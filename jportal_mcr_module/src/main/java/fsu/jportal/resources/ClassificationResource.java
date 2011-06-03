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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAO;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;

/**
 * This class is responsible for CRUD-operations of MCRCategories.
 * It accepts JSON objects of the form
 * <code>
 * {    "ID":{"rootID":"abcd","categID":"1234"}
 *      "label":[
 *          {"lang":"de","text":"Rubriken Test 2 fuer MyCoRe","descriptions":"test de"},
 *          {"lang":"en","text":"Rubric test 2 for MyCoRe","descriptions":"test en"}
 *      ]
 * 
 * }
 * </code>
 * @author chi
 *
 */
@Path("classifications")
public class ClassificationResource {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response newClassification(String json) {
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
        return Response.created(uri).build();
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

    @GET
    @Path("children")
    @Produces(MediaType.APPLICATION_JSON)
    public String getChildren(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryDAO mcrCategoryDAO = getCategoryDAO();
        
        if(rootID == null && categID == null){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        MCRCategoryID id = toMCRCategID(rootID, categID);
        List<MCRCategory> children = mcrCategoryDAO.getChildren(id);
        System.out.println("children size: " + children.size());
        return gson.toJson(children);
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryDAO mcrCategoryDAO = getCategoryDAO();
        
        if(rootID == null && categID == null){
            List<MCRCategory> rootCategories = mcrCategoryDAO.getRootCategories();
            return gson.toJson(rootCategories);
        }
        
        MCRCategoryID id = toMCRCategID(rootID, categID);
        MCRCategory category = mcrCategoryDAO.getCategory(id, 0);
        
        if(category == null){
            throw new WebApplicationException(Status.NOT_FOUND);
        }
        
        return gson.toJson(category);
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
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryImpl category = gson.fromJson(json, MCRCategoryImpl.class);

        try {
            getCategoryDAO().replaceCategory(category);
            return Response.ok(json).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        }

    }

    @DELETE
    public Response deleteClassification(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        MCRCategoryID mcrCategID = toMCRCategID(rootID, categID);

        try {
            getCategoryDAO().deleteCategory(mcrCategID);
            return Response.status(Status.GONE).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        }
    }
}
