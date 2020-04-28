package fsu.jportal.backend.pi.urn;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.pi.MCRPIManager;
import org.mycore.pi.backend.MCRPI;
import org.mycore.services.queuedjob.MCRJob;
import org.mycore.services.queuedjob.MCRJobAction;

import fsu.thulb.connections.urn.DNBURNRestClient;
import fsu.thulb.model.EpicurLite;

/**
 * Created by chi on 16.04.20
 *
 * @author Huu Chi Vu
 */
public class DNBUrnRemoteRegistryAction extends MCRJobAction {
    private final Logger LOGGER = LogManager.getLogger();
    private static final String SERVICEID = "SERVICEID";
    private static final String DERIVATEID = "DERIVATEID";
    private static final String ADDITIONAL = "ADDITIONAL";
    private static final String URN = "URN";

    public static void addJob(String serviceID, String derivatId, String additional, String urn) {
        MCRJob mcrJob = new MCRJob(DNBUrnRemoteRegistryAction.class);
        mcrJob.setParameter(SERVICEID, serviceID);
        mcrJob.setParameter(DERIVATEID, derivatId);
        mcrJob.setParameter(ADDITIONAL, additional);
        mcrJob.setParameter(URN, urn);

        URNJobUtils.addJob(mcrJob, DNBUrnRemoteRegistryAction.class);
    }

    public DNBUrnRemoteRegistryAction() {
    }

    public DNBUrnRemoteRegistryAction(MCRJob job) {
        super(job);
    }

    @Override
    public boolean isActivated() {
        return true;
    }

    @Override
    public String name() {
        return "URN DNB Registry";
    }

    @Override
    public void execute() throws ExecutionException {
        URNJobUtils.execAsJanistorUser(() -> runJob());
    }

    private void runJob() throws ExecutionException {
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
            String derivateId = this.job.getParameter(DERIVATEID);
            String additional = this.job.getParameter(ADDITIONAL);
            String urn = this.job.getParameter(URN);

            URL url = DerivateURNUtils.getURL(derivateId, additional, urn);
            EpicurLite epicurLite = EpicurLite.instance(credentials, urn, url);

            updateRegistrationStarted(new Date());
            Optional<Date> registeredDate = DNBURNRestClient.register(epicurLite, dnbURL);

            if(registeredDate.isPresent()){
                updateRegistrationDate(registeredDate.get());
            } else {
                Exception e = new Exception("Could not register URN " + urn);
                throw new ExecutionException(e);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw new ExecutionException("Plese check MCR.URN.DNB.URL for errors.", e);
        }
    }

    private void updateRegistrationStarted(Date date) {
        getMCRPI().setRegistrationStarted(date);
    }

    private void updateRegistrationDate(Date date) {
        getMCRPI().setRegistered(date);
    }

    private MCRPI getMCRPI(){
        String serviceId = this.job.getParameter(SERVICEID);
        String derivateId = this.job.getParameter(DERIVATEID);
        String additional = this.job.getParameter(ADDITIONAL);
        return MCRPIManager.getInstance()
                .get(serviceId, derivateId, additional);
    }



    @Override
    public void rollback() {

    }

    public boolean hasUrn(String derivateId, String additional) {
        String jobDerivId = this.job.getParameter(DERIVATEID);
        String jobAdditional = this.job.getParameter(ADDITIONAL);

        return jobDerivId.equals(derivateId) && jobAdditional.equals(additional);
    }

    public boolean hasUrn(String urn) {
        String jobUrn = this.job.getParameter(URN);

        return jobUrn.equals(urn);
    }
}
