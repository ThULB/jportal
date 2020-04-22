package fsu.jportal.backend.pi.urn;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRSystemUserInformation;
import org.mycore.common.MCRUserInformation;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;
import org.mycore.services.queuedjob.MCRJobQueue;

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
}
