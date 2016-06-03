package fsu.jportal.resources;

import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.MCRJerseyUtil;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.JPComponentUtil;

/**
 * Simple resource to set the autsort {@link JPSorter} for mycore objects.
 * 
 * @author Matthias Eichner
 */
@Path("sorter/{id}")
public class SorterResource {

    @POST
    public void update(@PathParam("id") String id, @QueryParam("sorter") String sorterClass,
        @QueryParam("order") String orderString) {
        JPContainer jpContainer = get(id);
        try {
            Class<? extends JPSorter> sorter = Class.forName(sorterClass).asSubclass(JPSorter.class);
            Order order = Order.valueOf(orderString.toUpperCase());
            jpContainer.setSortBy(sorter, order);
            jpContainer.store();
        } catch (Exception exc) {
            throw new WebApplicationException(exc,
                Response.status(Status.INTERNAL_SERVER_ERROR)
                        .entity("Unable to set sorter " + sorterClass + " for " + id)
                        .build());
        }
    }

    @DELETE
    public void delete(@PathParam("id") String id) {
        JPContainer jpContainer = get(id);
        try {
            jpContainer.setSortBy(null, null);
            jpContainer.store();
        } catch (Exception exc) {
            throw new WebApplicationException(exc,
                Response.status(Status.INTERNAL_SERVER_ERROR).entity("Unable to remove sorter for " + id).build());
        }
    }

    private JPContainer get(String id) {
        MCRObjectID mcrId = MCRObjectID.getInstance(id);
        MCRJerseyUtil.checkPermission(mcrId, MCRAccessManager.PERMISSION_WRITE);
        return JPComponentUtil.getContainer(mcrId).orElse(null);
    }

}
