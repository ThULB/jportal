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
    private final String REGISTRYSERVICECLASS = "REGISTRYSERVICECLASS";
    private final String MAXNUMFILES = "MAXNUMFILES";

    public URNGranularJobService(String registrationServiceID) {
        super(registrationServiceID, MCRDNBURN.TYPE);
    }

    @Override
    public synchronized MCRDNBURN register(MCRBase obj, String additional, boolean updateObject) throws MCRAccessException, MCRActiveLinkException, MCRPersistentIdentifierException, ExecutionException, InterruptedException {
        String serviceClass = getProperties().get(REGISTRYSERVICECLASS);
        String maxNumFilesProp = getProperties().get(MAXNUMFILES);

        if(serviceClass == null || "".equals(serviceClass)){
            String errMsg = REGISTRATION_CONFIG_PREFIX + getServiceID() + "." + REGISTRYSERVICECLASS + " not Set!";
            throw new MCRConfigurationException(errMsg);
        }

        String maxNumFilesPropName = REGISTRATION_CONFIG_PREFIX + getServiceID() + "." + MAXNUMFILES;
        if(maxNumFilesProp == null || "".equals(maxNumFilesProp)){
            String errMsg = maxNumFilesPropName + " not Set!";
            throw new MCRConfigurationException(errMsg);
        }

        long maxFiles = 10;
        try {
            maxFiles = Long.parseLong(maxNumFilesProp);
        }catch (NumberFormatException e){
            throw new MCRConfigurationException("Wrong number format in " + maxNumFilesPropName + ": " + maxNumFilesProp);
        }

        URNJob.register(getServiceID(),serviceClass, obj, additional, maxFiles);
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
