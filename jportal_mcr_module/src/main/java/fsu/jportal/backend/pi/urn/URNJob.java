package fsu.jportal.backend.pi.urn;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRClassTools;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRBase;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.pi.MCRPIService;
import org.mycore.pi.exceptions.MCRPersistentIdentifierException;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.user2.MCRUserManager;

/**
 * Created by chi on 29.04.20
 *
 * @author Huu Chi Vu
 */
public class URNJob {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final String SERVICEID = "SERVICEID";
    private static final String NAME = "NAME";
    private static final String SERVICECLASS = "SERVICECLASS";
    private static final String DERIVATEID = "DERIVATEID";
    private static final String ADDITIONAL = "ADDITIONAL";
    private static final String URN = "URN";
    private static final String USERID = "USERID";

    private static final String LOCALREGISTRY = "LOCALREGISTRY";
    private static final String REMOTEREGISTRY = "REMOTEREGISTRY";

    private static MCRJob newJob(String name){
        MCRJob mcrJob = new MCRJob(DNBUrnRegistryAction.class);
        mcrJob.setParameter(NAME, name);

        return mcrJob;
    }

    public static void addLocalRegistryJob(String serviceID, String serviceClass, MCRBase obj, String additional) {
        MCRJob mcrJob = newJob(LOCALREGISTRY);
        mcrJob.setParameter(SERVICEID, serviceID);
        mcrJob.setParameter(SERVICECLASS, serviceClass);
        mcrJob.setParameter(DERIVATEID, obj.getId().toString());
        mcrJob.setParameter(ADDITIONAL, additional);
        mcrJob.setParameter(USERID, getCurrentUserID());

        URNJobUtils.addJob(mcrJob);
    }

    private static String getCurrentUserID() {
        return MCRUserManager.getCurrentUser().getUserID();
    }

    public static void addRemoteRegistryJob(String serviceID, String derivatId, String additional, String urn) {
        MCRJob mcrJob = newJob(REMOTEREGISTRY);
        mcrJob.setParameter(SERVICEID, serviceID);
        mcrJob.setParameter(DERIVATEID, derivatId);
        mcrJob.setParameter(ADDITIONAL, additional);
        mcrJob.setParameter(URN, urn);
        mcrJob.setParameter(USERID, getCurrentUserID());

        URNJobUtils.addJob(mcrJob);
    }

    static MCRPIService<MCRDNBURN> getService(MCRJob job) throws ExecutionException {
        String serviceId = getServiceId(job);
        String serviceClassName = getServiceClassName(job);
        return getService(serviceId, serviceClassName);
    }

    private static String getServiceClassName(MCRJob job) {
        return job.getParameter(SERVICECLASS);
    }

    static String getServiceId(MCRJob job) {
        return job.getParameter(SERVICEID);
    }

    static String getDerivateId(MCRJob job) {
        return job.getParameter(DERIVATEID);
    }

    static String getAdditional(MCRJob job) {
        return job.getParameter(ADDITIONAL);
    }

    static String getUrn(MCRJob job) {
        return job.getParameter(URN);
    }

    private static String getName(MCRJob job) {
        return job.getParameter(NAME);
    }

    private static String getUserId(MCRJob job) {
        return job.getParameter(USERID);
    }

    static MCRDerivate getDerivate(String objIdStr){
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(objIdStr);
        return MCRMetadataManager.retrieveMCRDerivate(mcrObjectID);
    }

    public static String getActionName(MCRJob job) {
        return getServiceId(job) + "." + getName(job);
    }

    public static void register(String serviceID, String serviceClass, MCRBase obj, String additional, long maxFiles)
            throws ExecutionException, MCRAccessException, MCRActiveLinkException, MCRPersistentIdentifierException,
            InterruptedException {
        MCRPath derivateRoot = MCRPath.getPath(obj.getId().toString(), "/");
        if(!(obj instanceof MCRDerivate)){
            Exception e = new Exception("Object is not of type MCRDerivate " + obj.getId());
            throw new ExecutionException(e);
        }

        MCRDerivate derivate = (MCRDerivate) obj;

        try(Stream<Path> files = Files.walk(derivateRoot)){
            long numOfFiles = files.filter(f -> !f.getRoot().equals(f) && Files.isRegularFile(f))
                    .count();

            if(numOfFiles > maxFiles){
                addLocalRegistryJob(serviceID, serviceClass, derivate, additional);
            } else {
                getService(serviceID, serviceClass).register(derivate, additional, true);
                RemoteRegistryJob.createRemoteRegistryJobs(serviceID, derivate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasUrn(MCRJob job, String urn) {
        String jobUrn = getUrn(job);
        String name = getName(job);

        return name.equals(REMOTEREGISTRY) && jobUrn.equals(urn);
    }

    public static boolean isJobOf(MCRJob job, String derivateId, String additional){
        String jobDerivId = getDerivateId(job);
        String jobAdditional = getAdditional(job);
        String name = getName(job);

        return name.equals(LOCALREGISTRY) && jobDerivId.equals(derivateId) && jobAdditional.equals(additional);
    }

    public static void run(MCRJob job) throws ExecutionException {
        String name = getName(job);
        String userId = getUserId(job);
        switch (name) {
            case LOCALREGISTRY: URNJobUtils.execAsUser(userId, () -> LocalRegistryAction.run(job));
            break;
            case REMOTEREGISTRY: URNJobUtils.execAsUser(userId,() -> RemoteRegistryJob.runRemoteRegistryJob(job));
            break;
            default: LOGGER.warn("Not supported action class!");
        }
    }

    private static MCRPIService<MCRDNBURN> getService(String serviceId, String serviceClassName)
            throws ExecutionException {

        try {
            Object service = MCRClassTools.forName(serviceClassName)
                    .getConstructor(String.class)
                    .newInstance(serviceId);
            if (service instanceof MCRPIService) {
                return (MCRPIService<MCRDNBURN>) service;
            } else {
                throw new ExecutionException(new ClassCastException(serviceClassName + " is not a " + MCRPIService.class));
            }
        } catch (InstantiationException | IllegalAccessException |
                InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            throw new ExecutionException(e);
        }
    }
}
