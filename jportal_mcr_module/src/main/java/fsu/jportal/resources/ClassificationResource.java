package fsu.jportal.resources;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.common.MCRPersistenceException;
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
        Rubric rubric = gson.fromJson(json, Rubric.class);
        
        MCRObjectID objId = MCRObjectID.getNextFreeId("jportal_jpclassi");
        
        MCRObject mcrObject = createMCRObject(objId);
        mcrObject.getMetadata().setMetadataElement(rubric.getRubricMetaElement());
        String parentID = rubric.getParentID();
        if(parentID  != null && !"".equals(parentID)){
            mcrObject.getStructure().setParent(parentID);
        }
        
        try {
            MCRMetadataManager.create(mcrObject);
            return Response.created(URI.create(objId.toString())).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            throw new WebApplicationException(Status.CONFLICT);
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            throw new WebApplicationException(Status.CONFLICT);
        }
    }

    private MCRObject createMCRObject(MCRObjectID objId) {
        MCRObject mcrObject = new MCRObject();
        mcrObject.setId(objId);
        mcrObject.setLabel(objId.toString());
        mcrObject.setSchema("datamodel-jpclassi.xsd");
        return mcrObject;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getClassification(@PathParam("id") String id) {
        try {
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(id));
            Rubric rubric = new Rubric();
            rubric.setRubricMetaElement(mcrObject.getMetadata().getMetadataElement(Rubric.TAGNAME));
            
            Gson gson = GsonManager.instance().createGson();
            
            return gson.toJson(rubric);
        } catch (MCRPersistenceException e) {
            throw new WebApplicationException(Status.NOT_FOUND);
        }
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateClassification(@PathParam("id") String id, String json) {
        Gson gson = GsonManager.instance().createGson();
        Rubric rubric = gson.fromJson(json, Rubric.class);
        
        MCRObjectID objId = MCRObjectID.getInstance(id);

        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(objId);
        mcrObject.getMetadata().setMetadataElement(rubric.getRubricMetaElement());

        try {
            MCRMetadataManager.update(mcrObject);
            return Response.ok(json).build();
        } catch (MCRPersistenceException e) {
            e.printStackTrace();
            return Response.status(Status.NOT_FOUND).build();
        } catch (MCRActiveLinkException e) {
            e.printStackTrace();
            return Response.status(Status.CONFLICT).build();
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
