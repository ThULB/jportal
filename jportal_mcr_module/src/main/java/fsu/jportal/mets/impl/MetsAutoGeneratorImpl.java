package fsu.jportal.mets.impl;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.events.MCRShutdownHandler;
import org.mycore.common.processing.MCRProcessableDefaultCollection;
import org.mycore.common.processing.MCRProcessableRegistry;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.util.concurrent.MCRTransactionableRunnable;
import org.mycore.util.concurrent.processing.MCRProcessableExecutor;
import org.mycore.util.concurrent.processing.MCRProcessableFactory;
import org.mycore.util.concurrent.processing.MCRProcessableSupplier;

import fsu.jportal.mets.MetsAutoGenerator;
import fsu.jportal.util.MetsUtil;

/**
 * <p>
 * This class spawns {@link MetsGenerationTask}s to update the mets.xml
 * of a derivate. The generation will be started fifteen minutes (or more)
 * after the {@link #add(MCRObjectID)} method was called.
 * </p>
 * 
 * <p>
 * In this period you can add the same derivate multiple times but the
 * generation is done just once.
 * </p>
 * 
 * @author Matthias Eichner
 */
@Singleton
public class MetsAutoGeneratorImpl implements MetsAutoGenerator {

    protected final static Logger LOGGER = LogManager.getLogger(MetsAutoGeneratorImpl.class);

    private static final long DELAY = 1;

    private MCRProcessableExecutor executor;

    private MCRProcessableDefaultCollection collection;

    private Map<MCRObjectID, Long> map;

    @Inject
    public MetsAutoGeneratorImpl(MCRProcessableRegistry registry) {
        this.map = new ConcurrentHashMap<>();

        this.collection = new MCRProcessableDefaultCollection("JP METS Generator");
        this.collection.setProperty("delay (minutes)", DELAY);
        registry.register(collection);
        this.executor = MCRProcessableFactory.newPool(Executors.newSingleThreadExecutor(), collection);
        final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleWithFixedDelay(new MetsGenerationHandler(map, executor, collection), DELAY, DELAY,
            TimeUnit.MINUTES);

        MCRShutdownHandler.getInstance().addCloseable(new MCRShutdownHandler.Closeable() {

            @Override
            public void prepareClose() {
                scheduler.shutdown();
                try {
                    scheduler.awaitTermination(30, TimeUnit.SECONDS);
                } catch (InterruptedException ie) {
                    LOGGER.debug("Could not wait 30 seconds for mets generation scheduler...", ie);
                }
                executor.getExecutor().shutdown();
                try {
                    executor.getExecutor().awaitTermination(60, TimeUnit.SECONDS);
                } catch (InterruptedException ie) {
                    LOGGER.debug("Could not wait 60 seconds for mets generation executor...", ie);
                }
            }

            @Override
            public void close() {
                scheduler.shutdownNow();
                executor.getExecutor().shutdownNow();
            }

        });

    }

    @Override
    public void add(MCRObjectID derivateId) {
        Long insertTime = map.get(derivateId);
        if (insertTime != null && insertTime == -1L) {
            // thread to create is already spawned
            return;
        }
        map.put(derivateId, System.currentTimeMillis());
        collection.setProperty("derivates", derivates(this.map));
    }

    @Override
    public void remove(MCRObjectID derivateId) {
        map.remove(derivateId);
        collection.setProperty("derivates", derivates(this.map));
    }

    private static List<String> derivates(Map<MCRObjectID, Long> map) {
        return map.keySet().stream().map(MCRObjectID::toString).collect(Collectors.toList());
    }

    private static class MetsGenerationHandler implements Runnable {

        private MCRProcessableExecutor executor;

        private MCRProcessableDefaultCollection collection;

        private Map<MCRObjectID, Long> map;

        public MetsGenerationHandler(Map<MCRObjectID, Long> map, MCRProcessableExecutor executor,
            MCRProcessableDefaultCollection collection) {
            this.map = map;
            this.executor = executor;
            this.collection = collection;
        }

        @Override
        public void run() {
            synchronized (this.map) {
                this.map.entrySet().stream().filter(filterEntry()).forEach(handleEntry());
                this.collection.setProperty("derivates", derivates(this.map));
            }
        }

        private Predicate<? super Entry<MCRObjectID, Long>> filterEntry() {
            return e -> {
                long currentTimeMillis = System.currentTimeMillis();
                if (e.getValue() == -1) {
                    return false;
                }
                return e.getValue() + DELAY < currentTimeMillis;
            };
        }

        private Consumer<? super Entry<MCRObjectID, Long>> handleEntry() {
            return e -> {
                MCRObjectID id = e.getKey();
                this.map.put(id, -1L);
                MetsGenerationTask metsGenerationTask = new MetsGenerationTask(id);
                MCRTransactionableRunnable transactionableRunnable = new MCRTransactionableRunnable(metsGenerationTask);
                MCRProcessableSupplier<?> supplier = this.executor.submit(transactionableRunnable);

                supplier.getFuture().whenComplete((result, throwable) -> {
                    this.map.remove(e.getKey());
                    if (throwable != null) {
                        LOGGER.warn("Unable to generate mets xml for " + id, throwable);
                    }
                });
            };
        }

    }

    private static class MetsGenerationTask implements Runnable {

        private MCRObjectID derivateId;

        public MetsGenerationTask(MCRObjectID derivateId) {
            this.derivateId = derivateId;
        }

        public void run() {
            try {
                if (!MetsUtil.isGeneratable(this.derivateId)) {
                    return;
                }
                MetsUtil.generateAndReplace(this.derivateId);
            } catch (Exception exc) {
                throw new RuntimeException("Unable to complete MetsGenerationTask", exc);
            }
        }

        @Override
        public String toString() {
            return "MetsGenerationTask of " + derivateId;
        }

    }

}
