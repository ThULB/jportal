package fsu.jportal.backend.pi.urn;

import java.util.Iterator;
import java.util.concurrent.ExecutionException;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobQueue;
import org.mycore.services.queuedjob.MCRJobStatus;
import org.mycore.user2.MCRUser;
import org.mycore.user2.MCRUserManager;

/**
 * Created by chi on 14.04.20
 *
 * @author Huu Chi Vu
 */
public class URNJobUtils {
    public static void addJob(MCRJob job) {
        MCRJobQueue mcrJobQueue = MCRJobQueue.getInstance(job.getAction());
        mcrJobQueue.offer(job);
    }

    public static void execAsJanistorUser(JobRunner runnable) throws ExecutionException {
        MCRSystemUserInformation janitorInstance = MCRSystemUserInformation.getJanitorInstance();
        execAsUser(janitorInstance, runnable);
        MCRSession currentSession = MCRSessionMgr.getCurrentSession();
        MCRUserInformation currentUserInformation = currentSession.getUserInformation();
        currentSession.setUserInformation(janitorInstance);
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

    public static void execAsUser(String userId, JobRunner runnable) throws ExecutionException {
        MCRUser user = MCRUserManager.getUser(userId);
        execAsUser(user, runnable);
    }

    public static void execAsUser(MCRUserInformation userInf, JobRunner runnable) throws ExecutionException {
        MCRSession currentSession = MCRSessionMgr.getCurrentSession();
        MCRUserInformation currentUserInformation = currentSession.getUserInformation();
        currentSession.setUserInformation(userInf);
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
        Class<DNBUrnRegistryAction> action = DNBUrnRegistryAction.class;
        Iterator<MCRJob> newJobs = MCRJobQueue.getInstance(action).iterator();
        while (newJobs.hasNext()){
            MCRJob job = newJobs.next();

            if(URNJob.isJobOf(job, derivateId, "")) {
                return true;
            }
        }

        Iterator<MCRJob> watingJobs = MCRJobQueue.getInstance(action)
                .iterator(MCRJobStatus.PROCESSING);

        while (watingJobs.hasNext()){
            MCRJob job = watingJobs.next();

            if(URNJob.isJobOf(job, derivateId, "")) {
                return true;
            }
        }
        return false;
    }

    public static boolean remoteRegistryInProgress(String urn){
        Class<DNBUrnRegistryAction> action = DNBUrnRegistryAction.class;
        Iterator<MCRJob> newJobs = MCRJobQueue.getInstance(action).iterator();
        while (newJobs.hasNext()){
            MCRJob job = newJobs.next();

            if(URNJob.hasUrn(job, urn)){
                return true;
            }
        }

        Iterator<MCRJob> watingJobs = MCRJobQueue.getInstance(action)
                .iterator(MCRJobStatus.PROCESSING);

        while (watingJobs.hasNext()){
            MCRJob job = watingJobs.next();

            if(URNJob.hasUrn(job, urn)){
                return true;
            }
        }
        return false;
    }
}
