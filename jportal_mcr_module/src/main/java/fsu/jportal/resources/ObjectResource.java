package fsu.jportal.resources;

import static org.mycore.access.MCRAccessManager.PERMISSION_DELETE;
import static org.mycore.access.MCRAccessManager.PERMISSION_READ;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.mycore.common.content.MCRContent;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.util.JerseyUtil;

@Path("object")
public class ObjectResource {
    static Logger LOGGER = Logger.getLogger(ObjectResource.class);

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Response get(@PathParam("id") String id) {
        MCRObjectID mcrId = JerseyUtil.getID(id);
        JerseyUtil.checkPermission(mcrId, PERMISSION_READ);
        try {
            MCRContent content = MCRXMLMetadataManager.instance().retrieveContent(mcrId);
            return Response.ok(content.asString(), MediaType.APPLICATION_XML).build();
        } catch (Exception exc) {
            LOGGER.error("while creating xml for object " + id, exc);
            throw new WebApplicationException(exc, Status.INTERNAL_SERVER_ERROR);
        }
    }

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@PathParam("id") String id) {
        MCRObjectID mcrId = JerseyUtil.getID(id);
        JerseyUtil.checkPermission(mcrId, PERMISSION_DELETE);
        if (mcrId.getTypeId().equals("derivate")) {
            MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrId);
            MCRMetadataManager.delete(mcrDer);
        } else {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
            try {
                MCRMetadataManager.delete(mcrObj);
            } catch (MCRActiveLinkException mcrActExc) {
                return Response.status(Status.FORBIDDEN).entity(mcrActExc.getMessage()).build();
            }
        }
        return Response.ok().build();
    }


}
