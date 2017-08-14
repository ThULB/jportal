package fsu.jportal.urn;

import java.io.Closeable;
import java.io.IOException;
import java.util.TimerTask;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.persistence.EntityTransaction;
import javax.persistence.RollbackException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.backend.jpa.MCREntityManagerProvider;
import org.mycore.pi.MCRPersistentIdentifierManager;
import org.mycore.pi.urn.MCRDNBURN;
import org.mycore.pi.urn.rest.MCRDNBURNRestClient;

/**
 * Created by chi on 26.01.17.
 * porting from org.mycore.urn.rest.URNRegistrationService
 *
 * @author shermann
 * @author Huu Chi Vu
 */
public final class URNRESTRegistrationTask extends TimerTask implements Closeable {

    protected static final Logger LOGGER = LogManager.getLogger();

    public static final int BATCH_SIZE = 20;

    private final MCRDNBURNRestClient dnburnClient;

    public URNRESTRegistrationTask(MCRDNBURNRestClient client) {
        this.dnburnClient = client;
    }

    @Override
    public void run() {
        UnaryOperator<Integer> register = b -> MCRPersistentIdentifierManager
            .getInstance()
            .setRegisteredDateForUnregisteredIdenifiers(MCRDNBURN.TYPE, dnburnClient::register, b);

        Integer numOfRegisteredObj = MCRTransactionExec.cute(register).apply(BATCH_SIZE);

        while (numOfRegisteredObj > 0) {
            if(numOfRegisteredObj % 3000 == 0){
                try {
                    LOGGER.info("URN registration sleep for 6 seconds.");
                    Thread.sleep(60000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            numOfRegisteredObj = MCRTransactionExec.cute(register).apply(BATCH_SIZE);
        }

    }

    @Override
    public void close() throws IOException {
        LOGGER.info("Stopping " + getClass().getSimpleName());
    }

    public static class MCRTransactionExec {
        public static <T, R> Function<T, R> cute(Function<T, R> function) {
            return t -> {
                EntityTransaction tx = beginTransaction();

                try {
                    return function.apply(t);
                } finally {
                    endTransaction(tx);
                }
            };
        }

        private static EntityTransaction beginTransaction() {
            EntityTransaction tx = MCREntityManagerProvider
                .getCurrentEntityManager()
                .getTransaction();

            tx.begin();
            return tx;
        }

        private static void endTransaction(EntityTransaction tx) {
            if (tx != null && tx.isActive()) {
                if (tx.getRollbackOnly()) {
                    tx.rollback();
                } else {
                    try {
                        tx.commit();
                    } catch (RollbackException e) {
                        if (tx.isActive()) {
                            tx.rollback();
                        }
                        throw e;
                    }
                }
            }
        }
    }
}
