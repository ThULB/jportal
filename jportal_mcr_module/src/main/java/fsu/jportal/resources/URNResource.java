package fsu.jportal.resources;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.MCRPersistentIdentifierManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.rest.MCRDNBURNRestClient;
import org.mycore.pi.urn.rest.MCRDerivateURNUtils;
import org.mycore.pi.urn.rest.MCREpicurLite;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

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

        Function<MCRPIRegistrationInfo, MCREpicurLite> func;
        Supplier<? extends Throwable> exception;

        MCRDNBURNRestClient urnRestClient = getUsernamePassword()
            .map(this::getEpicureProvider)
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

     public Optional<UsernamePasswordCredentials> getUsernamePassword() {
        String username = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Login", null);
        String password = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Password", null);

        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            LOGGER.warn("Could not instantiate " + this.getClass().getName()
                + " as required credentials are unset");
            LOGGER.warn("Please set MCR.URN.DNB.Credentials.Login and MCR.URN.DNB.Credentials.Password");
            return Optional.empty();
        }

        return Optional.of(new UsernamePasswordCredentials(username, password));
    }

    public Function<MCRPIRegistrationInfo, MCREpicurLite> getEpicureProvider(UsernamePasswordCredentials credentials) {
        return urn -> MCREpicurLite.instance(urn, MCRDerivateURNUtils.getURL(urn))
            .setCredentials(credentials);
    }
}
