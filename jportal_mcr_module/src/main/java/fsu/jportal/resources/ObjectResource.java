package fsu.jportal.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

@Path("object")
public class ObjectResource {
    static Logger LOGGER = Logger.getLogger(ObjectResource.class);

    @DELETE
    @Path("{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response delete(@PathParam("id") String id) {
        MCRObjectID mcrId;
        try {
            mcrId = MCRObjectID.getInstance(id);
        } catch(MCRException mcrExc) {
            return Response.status(Status.BAD_REQUEST).entity("invalid mycore id").build();
        }
        if(!MCRAccessManager.checkPermission(mcrId, "deletedb")) {
            return Response.status(Status.UNAUTHORIZED).build();
        }
        if(!MCRMetadataManager.exists(mcrId)) {
            return Response.status(Status.NOT_FOUND).build();
        }
        if(mcrId.getTypeId().equals("derivate")) {
            MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(mcrId);
            MCRMetadataManager.delete(mcrDer);
        } else {
            MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
            try {
                MCRMetadataManager.delete(mcrObj);
            } catch(MCRActiveLinkException mcrActExc) {
                return Response.status(Status.FORBIDDEN).entity(mcrActExc.getMessage()).build();
            }
        }
        return Response.ok().build();
    }

}
