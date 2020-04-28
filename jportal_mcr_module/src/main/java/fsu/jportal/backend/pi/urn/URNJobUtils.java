package fsu.jportal.backend.pi.urn;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.services.queuedjob.MCRJobQueue;
import org.mycore.services.queuedjob.MCRJobStatus;

/**
 * Created by chi on 14.04.20
 *
 * @author Huu Chi Vu
 */
public class URNJobUtils {
    public static void addJob(MCRJob job, Class<? extends MCRJobAction> actionClass) {
        MCRJobQueue mcrJobQueue = MCRJobQueue.getInstance(actionClass);
        mcrJobQueue.offer(job);
    }

    public static void execAsJanistorUser(JobRunner runnable) throws ExecutionException {
        MCRSession currentSession = MCRSessionMgr.getCurrentSession();
        MCRUserInformation currentUserInformation = currentSession.getUserInformation();
        currentSession.setUserInformation(MCRSystemUserInformation.getJanitorInstance());
        try {
            if(!currentSession.isTransactionActive()){
                currentSession.beginTransaction();
            }
            runnable.run();
        }finally {
            if(currentSession.isTransactionActive()){
                currentSession.commitTransaction();
            }

            currentSession.setUserInformation(currentUserInformation);
        }
    }

    interface JobRunner {
        void run() throws ExecutionException;
    }

    public static boolean localRegistryInProgress(String derivateId){
        Iterator<MCRJob> newJobs = MCRJobQueue.getInstance(DNBUrnLocalRegistryAction.class).iterator();
        while (newJobs.hasNext()){
            MCRJob job = newJobs.next();

            DNBUrnLocalRegistryAction urnLocalRegistryAction = new DNBUrnLocalRegistryAction(job);
            return urnLocalRegistryAction.isJobOf(derivateId, "");
        }

        Iterator<MCRJob> watingJobs = MCRJobQueue.getInstance(DNBUrnLocalRegistryAction.class)
                .iterator(MCRJobStatus.PROCESSING);

        while (watingJobs.hasNext()){
            MCRJob job = watingJobs.next();

            DNBUrnLocalRegistryAction urnLocalRegistryAction = new DNBUrnLocalRegistryAction(job);
            return urnLocalRegistryAction.isJobOf(derivateId, "");
        }
        return false;
    }

    public static boolean remoteRegistryInProgress(String urn){
        Iterator<MCRJob> newJobs = MCRJobQueue.getInstance(DNBUrnRemoteRegistryAction.class).iterator();
        while (newJobs.hasNext()){
            MCRJob job = newJobs.next();

            DNBUrnRemoteRegistryAction remoteRegistryAction = new DNBUrnRemoteRegistryAction(job);
            return remoteRegistryAction.hasUrn(urn);
        }

        Iterator<MCRJob> watingJobs = MCRJobQueue.getInstance(DNBUrnLocalRegistryAction.class)
                .iterator(MCRJobStatus.PROCESSING);

        while (watingJobs.hasNext()){
            MCRJob job = watingJobs.next();

            DNBUrnRemoteRegistryAction urnRemoteRegistryAction = new DNBUrnRemoteRegistryAction(job);
            return urnRemoteRegistryAction.hasUrn(urn);
        }
        return false;
    }
}
