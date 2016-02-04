package fsu.jportal.resources;

import fsu.jportal.util.DerivateLinkUtil;
import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Path("derivate/link")
public class DerivateLinkResource {

    @Path("set/{id}")
    @POST
    public void set(@PathParam("id") String id) throws MCRActiveLinkException {
        String bookmarkedImage = DerivateLinkUtil.getBookmarkedImage();
        if (bookmarkedImage == null) {
            throw new WebApplicationException(Response.status(Status.CONFLICT).entity("bookmark an image first")
                .build());
        }
        try {
            DerivateLinkUtil.setLink(getMyCoReID(id), bookmarkedImage);
        } catch (MCRAccessException e) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("No Access")
                                                      .build());
        }
    }

    @POST
    @Path("remove/{id}")
    public void remove(@PathParam("id") String id, @QueryParam("image") String image) throws MCRActiveLinkException,
        UnsupportedEncodingException {
        if (image == null) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("image param not set").build());
        }
        try {
            DerivateLinkUtil.removeLink(getMyCoReID(id), URLDecoder.decode(image, "UTF-8"));
        } catch (MCRAccessException e) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("image param not set").build());
        }
    }

    @POST
    @Path("bookmark/{derivate}")
    public void bookmark(@PathParam("derivate") String derivate, @QueryParam("image") String image)
        throws UnsupportedEncodingException {
        if (image == null) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity("image param not set").build());
        }
        MCRJerseyUtil.checkPermission(derivate, "writedb");
        DerivateLinkUtil.bookmarkImage(derivate, URLDecoder.decode(image, "UTF-8"));
    }

    protected MCRObjectID getMyCoReID(String id) {
        MCRObjectID objectID = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(objectID, "writedb");
        return objectID;
    }

}
