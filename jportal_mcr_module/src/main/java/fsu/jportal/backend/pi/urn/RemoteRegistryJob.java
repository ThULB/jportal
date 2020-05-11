package fsu.jportal.backend.pi.urn;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.services.queuedjob.MCRJob;

import fsu.thulb.connections.urn.DNBURNRestClient;
import fsu.thulb.model.EpicurLite;

/**
 * Created by chi on 05.05.20
 *
 * @author Huu Chi Vu
 */
public class RemoteRegistryJob {
    public static void runRemoteRegistryJob(MCRJob job) throws ExecutionException {
        String username = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Login", null);
        String password = MCRConfiguration.instance().getString("MCR.URN.DNB.Credentials.Password", null);

        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            Exception e = new Exception("Please set MCR.URN.DNB.Credentials.Login and MCR.URN.DNB.Credentials.Password");
            throw new ExecutionException(e);
        }

        String dnbURLStr = MCRConfiguration.instance().getString("MCR.URN.DNB.URL", null);
        try {
            URI dnbURL = new URI(dnbURLStr);
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(username, password);
            String serviceId = URNJob.getServiceId(job);
            String derivateId = URNJob.getDerivateId(job);
            String additional = URNJob.getAdditional(job);
            String urn = URNJob.getUrn(job);

            URL url = DerivateURNUtils.getURL(derivateId, additional, urn);
            EpicurLite epicurLite = EpicurLite.instance(credentials, urn, url);

            MCRPI mcrpi = MCRPIManager.getInstance()
                    .get(serviceId, derivateId, additional);

            mcrpi.setRegistrationStarted(new Date());
            Optional<Date> registeredDate = DNBURNRestClient.register(epicurLite, dnbURL);

            if(registeredDate.isPresent()){
                mcrpi.setRegistered(registeredDate.get());
            } else {
                Exception e = new Exception("Could not register URN " + urn);
                throw new ExecutionException(e);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new ExecutionException("Plese check MCR.URN.DNB.URL for errors.", e);
        }
    }

    public static void createRemoteRegistryJobs(String serviceId, MCRDerivate derivate) {
        String derivateId = derivate.getId().toString();
        Map<String, String> urnMap = derivate.getUrnMap();
        Set<Map.Entry<String, String>> urnMapEntries = urnMap.entrySet();

        String derivateURN = derivate.getDerivate().getURN();
        URNJob.addRemoteRegistryJob(serviceId, derivateId, "", derivateURN);

        for (Map.Entry<String, String> additionalUrnPair : urnMapEntries) {
            String additional = additionalUrnPair.getKey();
            String urn = additionalUrnPair.getValue();
            URNJob.addRemoteRegistryJob(serviceId, derivateId, additional, urn);
        }
    }
}
