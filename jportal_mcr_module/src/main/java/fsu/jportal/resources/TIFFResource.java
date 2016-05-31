package fsu.jportal.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import fsu.jportal.util.TIFFUtil;

@Path("tiff")
public class TIFFResource {

    protected Logger LOGGER = LogManager.getLogger(TIFFResource.class);

    @POST
    @Path("convert/{derivateId}/{fileName:.*}")
    public Response convert(@PathParam("derivateId") String derivateId, @PathParam("fileName") String fileName) {
        MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(MCRObjectID.getInstance(derivateId));

        MCRJerseyUtil.checkPermission(derivate.getId(), MCRAccessManager.PERMISSION_WRITE);
        if (!derivate.isValid()) {
            MCRJerseyUtil.throwException(Status.INTERNAL_SERVER_ERROR, "Not valid Derivate ID!!");
        }
        if (!TIFFUtil.isLibTiffInstalled()) {
            MCRJerseyUtil.throwException(Status.INTERNAL_SERVER_ERROR, "Libtiff is not installed!");
        }

        try {
            TIFFUtil.startProcessTiff(derivate, fileName);
        } catch (UnsupportedOperationException uoEx) {
            MCRJerseyUtil.throwException(Status.INTERNAL_SERVER_ERROR,
                "Something gone wrong. Maybe the file is not ready, please wait a bit and try again.");
        } catch (Exception e) {
            MCRJerseyUtil.throwException(Status.INTERNAL_SERVER_ERROR, "Problems to process the File!");
        }
        return Response.ok().build();
    }

}
