package fsu.jportal.resources;

import fsu.jportal.urn.URNTools;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.MCRPersistentIdentifierManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.rest.MCRDNBURNRestClient;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

/**
 * Created by chi on 18.09.17.
 */
@Path("urn")
public class URNResource {

    private static final Logger LOGGER = LogManager.getLogger();

    @POST
    @Path("update/{derivID}")
    public Response update(@PathParam("derivID") String derivID){
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(derivID);
        if(!MCRAccessManager.checkPermission(mcrObjectID, "update-derivate")){
            return Response.status(401).build();
        }

        MCRDNBURNRestClient urnRestClient = URNTools.getUsernamePassword()
            .map(URNTools::getEpicureProvider)
            .map(MCRDNBURNRestClient::new)
            .orElseThrow(() -> new MCRException("Could not create URN Rest client."));

        List<MCRPIRegistrationInfo> regURN = MCRPersistentIdentifierManager.getInstance()
            .getCreatedIdentifiers(mcrObjectID, MCRDNBURN.TYPE, "DNBURNGranular");

        for (MCRPIRegistrationInfo urn : regURN) {
            MCRPI pi = (MCRPI) urn;
            Date registerDate = urnRestClient.register(urn)
                .orElse(null);

            if(registerDate == null){
                return Response.serverError().build();
            }

            pi.setRegistered(registerDate);
        }

        return Response.ok().build();
    }

}
