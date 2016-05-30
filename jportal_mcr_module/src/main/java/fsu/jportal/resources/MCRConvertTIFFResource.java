/**
 * 
 */
package fsu.jportal.resources;

import java.io.IOException;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.util.MCRConvertTIFF;

@Path("convertTIFF")
public class MCRConvertTIFFResource {

    protected Logger LOGGER = LogManager.getLogger(MCRConvertTIFFResource.class);

    @POST
    @Path("{derivateId}/{fileName:.*}")
    public Response convert(@PathParam("derivateId") String derivateId, @PathParam("fileName") String fileName) throws IOException {
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateId));

        if (!MCRAccessManager.checkPermission(derivate.getId(), MCRAccessManager.PERMISSION_WRITE)) {
            return Response.status(Status.FORBIDDEN).build();
        }

        if (!derivate.isValid()) {
            return Response.status(Status.NOT_FOUND).entity("Not valid Derivate ID!!").build();
        }

        if (!MCRConvertTIFF.isLibTiffInstalled()) {
            return Response.status(Status.NOT_FOUND).entity("Libtiff is not installed!").build();
        }

        try {
            MCRConvertTIFF.startProcessTiff(derivate, fileName);
        } catch (UnsupportedOperationException uoEx) {
            LOGGER.error("Something gone wrong. Maybe the file is not ready, please wait a bit and try again. ", uoEx);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Something gone wrong. Maybe the file is not ready, please wait a bit and try again. " + uoEx.getMessage()).build();
        } catch (Exception e) {
            LOGGER.error("Problems to process the File! ", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Problems to process the File! " + e.getMessage()).build();
        }
        
        return Response.ok().build();
    }
}
