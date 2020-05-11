package fsu.jportal.backend.pi.urn;

import java.util.concurrent.ExecutionException;

import org.mycore.access.MCRAccessException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.services.queuedjob.MCRJob;

/**
 * Created by chi on 05.05.20
 *
 * @author Huu Chi Vu
 */
public class LocalRegistryAction {
    public static void run(MCRJob job) throws ExecutionException {
        String additional = URNJob.getAdditional(job);
        //what if derivate has allready some existing urns?
        //check if additional is not empty string -> single Job
        try {
            String derivateId = URNJob.getDerivateId(job);
            MCRDerivate derivate = URNJob.getDerivate(derivateId);
            // check if derivate has to many scans -> use job or not
            URNJob.getService(job).register(derivate, additional, true);
            String serviceId = URNJob.getServiceId(job);
            RemoteRegistryJob.createRemoteRegistryJobs(serviceId, derivate);
        } catch (MCRAccessException | MCRActiveLinkException | MCRPersistentIdentifierException | InterruptedException e) {
            throw new ExecutionException(e);
        }
    }

}
