package fsu.jportal.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.mycore.frontend.jersey.MCRJerseyUtil;

import fsu.jportal.backend.gnd.GNDLocation;
import fsu.jportal.backend.gnd.GNDLocationService;

@Path("gnd")
public class GNDResource {

    @Inject
    private GNDLocationService locationService;

    @GET
    @Path("location/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public GNDLocation location(@PathParam("id") String gndId) {
        GNDLocation location = locationService.get(gndId);
        if (location == null) {
            MCRJerseyUtil.throwException(Response.Status.NOT_FOUND, "Unable to find " + gndId);
        }
        return location;
    }

}
