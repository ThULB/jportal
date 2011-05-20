package fsu.jportal.resources;

import java.net.URI;
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

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import com.google.gson.Gson;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.metadata.Rubric;

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
        
        MCRCategoryDAOFactory.getInstance().addCategory(parentID, category);
        URI uri = buildGetURI(categoryID.getRootID(), categoryID.getID());
        return Response.created(uri).build();
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
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@QueryParam("rootID") String rootID, @QueryParam("categID") String categID) {
        MCRCategoryID id = toMCRCategID(rootID, categID);
        MCRCategory category = MCRCategoryDAOFactory.getInstance().getCategory(id, 0);
        Gson gson = GsonManager.instance().createGson();
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
            MCRCategoryDAOFactory.getInstance().replaceCategory(category);
            return Response.ok(json).build();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        }

    }

    @DELETE
    @Path("{id}")
    public Response deleteClassification(@PathParam("id") String id) {
        MCRObjectID objId = MCRObjectID.getInstance(id);

        try {
            MCRMetadataManager.deleteMCRObject(objId);
            return Response.status(Status.GONE).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            return Response.status(Status.CONFLICT).build();
        }
    }
}
