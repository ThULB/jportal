package fsu.jportal.resources;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import fsu.jportal.util.DerivateLinkUtil;

@Path("derivate/link")
public class DerivateLinkResource {

    @Path("set/{id}")
    @POST
    public void set(@PathParam("id") String id) throws MCRActiveLinkException {
        String bookmarkedImage = DerivateLinkUtil.getBookmarkedImage();
        if (bookmarkedImage == null) {
            throw new WebApplicationException(
                Response.status(Status.CONFLICT).entity("bookmark an image first").build());
        }
        try {
            DerivateLinkUtil.setLink(getMyCoReID(id), bookmarkedImage);
        } catch (MCRAccessException e) {
            throw new WebApplicationException(Response.status(Status.UNAUTHORIZED).entity("No Access").build());
        }
    }

    @POST
    @Path("remove/{id}")
    public void remove(@PathParam("id") String id, @QueryParam("image") String image) throws MCRActiveLinkException {
        if (image == null) {
            throw new WebApplicationException(
                Response.status(Status.BAD_REQUEST).entity("image param not set").build());
        }
        try {
            DerivateLinkUtil.removeLink(getMyCoReID(id), image);
        } catch (MCRAccessException e) {
            throw new WebApplicationException(
                Response.status(Status.UNAUTHORIZED).entity("image param not set").build());
        }
    }

    @POST
    @Path("bookmark/{derivate}")
    public void bookmark(@PathParam("derivate") String derivate, @QueryParam("image") String image)
        throws UnsupportedEncodingException {
        if (image == null) {
            throw new WebApplicationException(
                Response.status(Status.BAD_REQUEST).entity("image param not set").build());
        }
        MCRJerseyUtil.checkPermission(derivate, "writedb");
        try {
            DerivateLinkUtil.bookmarkImage(derivate, URLDecoder.decode(image, "UTF-8"));
        } catch (URISyntaxException uriExc) {
            throw new WebApplicationException(uriExc,
                Response.status(Status.BAD_REQUEST).entity("the image couldn't be URI encoded").build());
        }
    }

    protected MCRObjectID getMyCoReID(String id) {
        MCRObjectID objectID = MCRJerseyUtil.getID(id);
        MCRJerseyUtil.checkPermission(objectID, "writedb");
        return objectID;
    }

}
