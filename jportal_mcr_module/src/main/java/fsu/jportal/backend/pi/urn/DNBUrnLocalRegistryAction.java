package fsu.jportal.backend.pi.urn;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRClassTools;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * Created by chi on 16.04.20
 *
 * @author Huu Chi Vu
 */
public class DNBUrnLocalRegistryAction extends MCRJobAction {
    private final Logger LOGGER = LogManager.getLogger();
    private static final String SERVICEID = "SERVICEID";
    private static final String SERVICECLASS = "SERVICECLASS";
    private static final String DERIVATEID = "DERIVATEID";
    private static final String ADDITIONAL = "ADDITIONAL";

    public static void addJob(String serviceID, String serviceClass, MCRBase obj, String additional) {
        MCRJob mcrJob = new MCRJob(DNBUrnLocalRegistryAction.class);
        mcrJob.setParameter(SERVICEID, serviceID);
        mcrJob.setParameter(SERVICECLASS, serviceClass);
        mcrJob.setParameter(DERIVATEID, obj.getId().toString());
        mcrJob.setParameter(ADDITIONAL, additional);

        URNJobUtils.addJob(mcrJob, DNBUrnLocalRegistryAction.class);
    }

    public DNBUrnLocalRegistryAction() {
    }

    public DNBUrnLocalRegistryAction(MCRJob job) {
        super(job);
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return "DNB URN system registry action";
    }

    @Override
    public void execute() throws ExecutionException {
        URNJobUtils.execAsJanistorUser(() -> runJob());
    }

    private void runJob(MCRDerivate derivate, String additional) throws ExecutionException {
        try {
            // check if derivate has to many scans -> use job or not
            getService().register(derivate, additional, true);
            createRemoteRegistryJobs(derivate);
        } catch (MCRAccessException | MCRActiveLinkException | MCRPersistentIdentifierException | InterruptedException e) {
            throw new ExecutionException(e);
        }
    }

    private void runJob() throws ExecutionException {
        String additional = this.job.getParameter(ADDITIONAL);
        //what if derivate has allready some existing urns?
        //check if additional is not empty string -> single Job
        try {
            MCRDerivate derivate = getDerivate();
            // check if derivate has to many scans -> use job or not
            getService().register(derivate, additional, true);
            createRemoteRegistryJobs(derivate);
        } catch (MCRAccessException | MCRActiveLinkException | MCRPersistentIdentifierException | InterruptedException e) {
            throw new ExecutionException(e);
        }
    }

    private void createRemoteRegistryJobs(MCRDerivate derivate) {
        String serviceId = this.job.getParameter(SERVICEID);
        String derivateId = derivate.getId().toString();
        Map<String, String> urnMap = derivate.getUrnMap();
        Set<Map.Entry<String, String>> urnMapEntries = urnMap.entrySet();

        String derivateURN = derivate.getDerivate().getURN();
        DNBUrnRemoteRegistryAction.addJob(serviceId, derivateId, "", derivateURN);

        for (Map.Entry<String, String> additionalUrnPair : urnMapEntries) {
            String additional = additionalUrnPair.getKey();
            String urn = additionalUrnPair.getValue();
            DNBUrnRemoteRegistryAction.addJob(serviceId, derivateId, additional, urn);
        }
    }

    @Override
    public void rollback() {

    }

    private MCRPIService<MCRDNBURN> getService() throws ExecutionException {
        String serviceClassName = this.job.getParameter(SERVICECLASS);
        String serviceId = this.job.getParameter(SERVICEID);

        return getService(serviceClassName, serviceId);
    }
    private MCRPIService<MCRDNBURN> getService(String serviceClassName, String serviceId) throws ExecutionException {
        try {
            Object service = MCRClassTools.forName(serviceClassName)
                    .getConstructor(String.class)
                    .newInstance(serviceId);
            if (service instanceof MCRPIService){
                return (MCRPIService<MCRDNBURN>) service;
            }
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new ExecutionException(e);
        }

        return null;
    }

    private MCRDerivate getDerivate(){
        String objIdStr = this.job.getParameter(DERIVATEID);
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(objIdStr);
        return MCRMetadataManager.retrieveMCRDerivate(mcrObjectID);
    }

    public boolean isJobOf(String derivateId, String additional){
        String jobDerivId = this.job.getParameter(DERIVATEID);
        String jobAdditional = this.job.getParameter(ADDITIONAL);

        return jobDerivId.equals(derivateId) && jobAdditional.equals(additional);
    }
}
