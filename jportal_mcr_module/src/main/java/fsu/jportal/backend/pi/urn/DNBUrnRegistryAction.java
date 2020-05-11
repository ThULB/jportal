package fsu.jportal.backend.pi.urn;

import java.util.concurrent.ExecutionException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;

/**
 * Created by chi on 29.04.20
 *
 * @author Huu Chi Vu
 */
public class DNBUrnRegistryAction extends MCRJobAction {
    public DNBUrnRegistryAction() {
    }

    public DNBUrnRegistryAction(MCRJob job) {
        super(job);
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return URNJob.getActionName(this.job);
    }

    @Override
    public void execute() throws ExecutionException {
        URNJob.run(this.job);
    }

    @Override
    public void rollback() {

    }
}
