package fsu.jportal.backend.pi.urn;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.config.MCRConfigurationException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;

/**
 * Created by chi on 09.04.20
 *
 * @author Huu Chi Vu
 */
public class URNGranularJobService extends MCRPIService<MCRDNBURN> {
    private final Logger LOGGER = LogManager.getLogger();
    private final String JOBSERVICECLASS = "JOBSERVICECLASS";

    public URNGranularJobService(String registrationServiceID) {
        super(registrationServiceID, MCRDNBURN.TYPE);
    }

    @Override
    public synchronized MCRDNBURN register(MCRBase obj, String additional, boolean updateObject) throws MCRAccessException, MCRActiveLinkException, MCRPersistentIdentifierException, ExecutionException, InterruptedException {
        String serviceClass = getProperties().get(JOBSERVICECLASS);

        if(serviceClass == null || "".equals(serviceClass)){
            String errMsg = REGISTRATION_CONFIG_PREFIX + getServiceID() + "." + JOBSERVICECLASS + " not Set!";
            throw new MCRConfigurationException(errMsg);
        }

        DNBUrnLocalRegistryAction.addJob(getServiceID(),serviceClass, obj, additional);

        return new MCRDNBURN("inProgress","");
    }

    @Override
    protected void registerIdentifier(MCRBase obj, String additional, MCRDNBURN pi) throws MCRPersistentIdentifierException {

    }

    @Override
    protected void delete(MCRDNBURN identifier, MCRBase obj, String additional) throws MCRPersistentIdentifierException {

    }

    @Override
    protected void update(MCRDNBURN identifier, MCRBase obj, String additional) throws MCRPersistentIdentifierException {

    }
}
