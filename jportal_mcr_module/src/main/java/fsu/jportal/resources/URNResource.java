package fsu.jportal.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.urn.URNTools;

/**
 * Created by chi on 18.09.17.
 */
@Path("urn")
public class URNResource {

    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    @Path("update/{derivID}")
    public Response update(@PathParam("derivID") String derivID) {
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(derivID);
        if (!MCRAccessManager.checkPermission(mcrObjectID, "update-derivate")) {
            return Response.status(401).build();
        }

        return URNTools.registerURNs(mcrObjectID)
            .filter(pi -> pi.getRegistered() == null)
            .findAny()
            .map(failedURNReg -> Response.serverError().build())
            .orElse(Response.ok().build());
    }
}
