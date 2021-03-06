package fsu.jportal.urn;

import java.util.Optional;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.servlet.ServletContext;

import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.common.events.MCRStartupHandler;
import org.mycore.pi.MCRPIRegistrationInfo;
import org.mycore.pi.urn.rest.MCRDNBURNRestClient;
import org.mycore.pi.urn.rest.MCRDerivateURNUtils;
import org.mycore.pi.urn.rest.MCREpicurLite;
import org.mycore.pi.urn.rest.MCRURNGranularRESTRegistrationTask;

import fsu.jportal.backend.mcr.JPConfig;

/**
 * @author shermann
 *
 */
public class URNRESTRegistrationStarter
    implements MCRStartupHandler.AutoExecutable, MCRShutdownHandler.Closeable {

    private static final Logger LOGGER = LogManager.getLogger();

    private final long period;

    private final TimeUnit timeUnit;

    private ScheduledExecutorService scheduler;

    public URNRESTRegistrationStarter() {
        this(1, TimeUnit.MINUTES);
    }

    public URNRESTRegistrationStarter(long taskPeriod, TimeUnit timeUnit) {
        this.period = taskPeriod;
        this.timeUnit = timeUnit;
    }

    @Override
    public String getName() {
        return "URN Registration Service";
    }

    @Override
    public int getPriority() {
        return 0;
    }

    @Override
    public void startUp(ServletContext servletContext) {
        getUsernamePassword()
            .map(this::getEpicureProvider)
            .map(MCRDNBURNRestClient::new)
            .map(URNRESTRegistrationTask::new)
            .map(this::startTimerTask)
            .orElseGet(this::couldNotStartTask)
            .accept(LOGGER);
    }

    private Consumer<Logger> couldNotStartTask() {
        return logger -> logger
            .warn("Could not start Task " + MCRURNGranularRESTRegistrationTask.class.getSimpleName());
    }

    private ScheduledExecutorService getScheduler() {
        if (scheduler == null) {
            LOGGER.info("Starting executor service...");
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        return scheduler;
    }

    private Consumer<Logger> startTimerTask(TimerTask task) {
        getScheduler().scheduleAtFixedRate(task, 0, period, timeUnit);
        return logger -> logger
            .info("Started task " + task.getClass().getSimpleName() + ", refresh every " + period + timeUnit
                .toString());
    }

    public Function<MCRPIRegistrationInfo, MCREpicurLite> getEpicureProvider(UsernamePasswordCredentials credentials) {
        return urn -> MCREpicurLite.instance(urn, MCRDerivateURNUtils.getURL(urn))
            .setCredentials(credentials);
    }

    public Optional<UsernamePasswordCredentials> getUsernamePassword() {
        String username = JPConfig.getString("MCR.URN.DNB.Credentials.Login", null);
        String password = JPConfig.getString("MCR.URN.DNB.Credentials.Password", null);

        if (username == null || password == null || username.length() == 0 || password.length() == 0) {
            LOGGER.warn("Could not instantiate " + this.getClass().getName()
                + " as required credentials are unset");
            LOGGER.warn("Please set MCR.URN.DNB.Credentials.Login and MCR.URN.DNB.Credentials.Password");
            return Optional.empty();
        }

        return Optional.of(new UsernamePasswordCredentials(username, password));
    }

    @Override
    public void prepareClose() {
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    @Override
    public void close() {
        if (scheduler != null && !scheduler.isShutdown()) {
            try {
                scheduler.awaitTermination(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting.", e);
            }
            scheduler.shutdownNow();
        }
    }
}
