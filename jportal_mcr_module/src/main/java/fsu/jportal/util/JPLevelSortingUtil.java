package fsu.jportal.util;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent.StoreOption;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.sort.JPLevelSorting;
import fsu.jportal.backend.sort.JPLevelSorting.Level;
import fsu.jportal.backend.sort.JPMagicSorter;
import fsu.jportal.backend.sort.JPPublishedSorter;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.common.MCRMarkManager.Operation;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.index.MCRSolrIndexer;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Helper class to support level sorting on journals.
 *
 * @author Matthias Eichner
 */
public abstract class JPLevelSortingUtil {

    static Logger LOGGER = LogManager.getLogger(JPLevelSortingUtil.class);

    public static final String LEVEL_SORTING = "jportal_level_sorting";

    /**
     * Loads the level sorting of the given journal id.
     *
     * @param objectID the journal identifier
     * @return a level sorting object
     * @throws IOException journal configuration couldn't be loaded
     */
    public static JPLevelSorting load(MCRObjectID objectID) throws IOException {
        JPObjectConfiguration config = getConfig(objectID);
        JPLevelSorting levelSorting = new JPLevelSorting();
        Map<String, String> levels = config.keyFilter("level.");
        levels.entrySet().stream().collect(Collectors.groupingBy(entry -> {
            String key = entry.getKey();
            return Integer.valueOf(key.substring(6, key.indexOf(".", 6)));
        })).forEach((level, value) -> {
            // level
            // name
            String name = value.stream()
                               .filter(e -> e.getKey().endsWith(".name"))
                               .map(Entry::getValue)
                               .findAny()
                               .orElse("unknown");
            // sorter
            String sorter = value.stream()
                                 .filter(e -> e.getKey().endsWith(".sorter"))
                                 .map(Entry::getValue)
                                 .findAny()
                                 .orElse(null);
            // order
            Order order = value.stream()
                               .filter(e -> e.getKey().endsWith(".order"))
                               .map(Entry::getValue)
                               .map(Order::valueOf)
                               .findAny()
                               .orElse(null);
            Unthrow.wrapProc(() -> {
                Class<? extends JPSorter> sorterClass = sorter != null ? getSorterClassByName(sorter) : null;
                levelSorting.set(level, name, sorterClass, order);
            });
        });
        return levelSorting;
    }

    /**
     * Returns the sorter class by its fully qualified class name.
     *
     * @param className the fully qualified class name
     * @return the class
     * @throws ClassNotFoundException if the class could not be found
     */
    public static Class<? extends JPSorter> getSorterClassByName(String className) throws ClassNotFoundException {
        Class<? extends JPSorter> sorterClass = null;
        if (className != null && className.length() > 0) {
            sorterClass = Class.forName(className).asSubclass(JPSorter.class);
        }
        return sorterClass;
    }

    /**
     * Saves the level sorting for the given journal on the file store.
     *
     * @param objectID the journal id
     * @param levelSorting the level sorting object to store
     *
     * @throws IOException loading or storing the journal configuration failed
     */
    public static void store(MCRObjectID objectID, JPLevelSorting levelSorting) throws IOException {
        JPObjectConfiguration config = getConfig(objectID);
        // remove all level stuff
        config.removeByKeyFilter("level.");
        // add from the level sorting object
        levelSorting.getLevels().forEach((index, level) -> {
            String baseKey = "level." + index;
            config.set(baseKey + ".name", level.getName());
            if (level.getSorterClass() != null) {
                config.set(baseKey + ".sorter", level.getSorterClass().getName());
            }
            if (level.getOrder() != null) {
                config.set(baseKey + ".order", level.getOrder().name());
            }
        });
        config.store();
    }

    /**
     * Returns the level of the object. The journal is level zero.
     *
     * @param object the object
     * @return level as number
     */
    public static int getLevelOfObject(MCRObject object) {
        return MCRObjectUtils.getAncestors(object).size();
    }

    /**
     * Reapply the level sorting for the given object. You can call this method if something
     * on your sorting implementation has changed.
     *
     * @param objectID the object to reapply the level sorting
     * @throws IOException journal configuration couldn't be loaded
     */
    public static void reapply(MCRObjectID objectID) throws IOException {
        JPLevelSorting levelSorting = JPLevelSortingUtil.load(objectID);
        JPLevelSortingUtil.apply(objectID, levelSorting);
    }

    /**
     * Runs through all descendants of the object and applies the sorters defined
     * in the given level sorting object.
     *
     * @param objectID the object to apply the level sorting at
     * @param levelSorting the level sorting to apply
     */
    public static void apply(MCRObjectID objectID, JPLevelSorting levelSorting) {
        LOGGER.info("apply level sorting for journal " + objectID + "...");
        // mark all descendants as IMPORTED
        SolrClient solrClient = MCRSolrClientFactory.getMainSolrClient();
        List<String> descendantAndSelfIds = MCRSolrSearchUtils.listIDs(solrClient, "journalID:" + objectID.toString());
        descendantAndSelfIds.forEach(
                childId -> MCRMarkManager.instance().mark(MCRObjectID.getInstance(childId), Operation.IMPORT));
        try {
            apply(objectID, levelSorting, 0);
        } finally {
            // unmark all descendants
            descendantAndSelfIds.forEach(childId -> MCRMarkManager.instance().remove(MCRObjectID.getInstance(childId)));
            MCRSolrIndexer.rebuildMetadataIndex(descendantAndSelfIds, solrClient);
        }
    }

    protected static void apply(MCRObjectID objectID, JPLevelSorting levelSorting, int levelPosition) {
        try {
            Level level = levelSorting.get(levelPosition);
            if (level != null) {
                applySorter(objectID, level.getSorterClass(), level.getOrder());
            }
            MCRObject object = MCRMetadataManager.retrieveMCRObject(objectID);
            List<MCRMetaLinkID> children = object.getStructure().getChildren();
            if (children.isEmpty()) {
                return;
            }
            children.stream()
                    .map(MCRMetaLinkID::getXLinkHref)
                    .map(MCRObjectID::getInstance)
                    .forEach(id -> apply(id, levelSorting, levelPosition + 1));
        } catch (Exception exc) {
            throw new MCRException("Unable to apply level sorting for " + objectID.toString(), exc);
        }
    }

    /**
     * Helper method to apply a sorter and an order to the given object.
     *
     * @param objectID the object
     * @param sorter the sorter
     * @param order the order
     */
    protected static void applySorter(MCRObjectID objectID, Class<? extends JPSorter> sorter, Order order) {
        JPComponentUtil.getContainer(objectID).ifPresent(container -> {
            container.setSortBy(sorter, order);
            try {
                container.store(StoreOption.metadata);
            } catch (Exception exc) {
                LOGGER.error("Unable to store " + container.getObject().getId() + " while applying sorter "
                        + sorter.getName(), exc);
            }
        });
    }

    /**
     * Analyzes the journal and returns a good starting point for the
     * level sorting structure.
     *
     * @param journalID the journal id
     */
    public static JPLevelSorting analyze(MCRObjectID journalID) {
        JPJournal journal = new JPJournal(journalID);
        JPLevelSorting levelSorting = new JPLevelSorting();
        analyzeNext(journal, levelSorting);
        return levelSorting;
    }

    private static void analyzeNext(JPContainer container, JPLevelSorting levelSorting) {
        List<MCRObjectID> children = container.getChildren();
        if (!children.isEmpty()) {
            MCRObjectID childID = children.get(0);
            Optional<JPPeriodicalComponent> optionalChild = JPComponentUtil.getPeriodical(childID);
            optionalChild.ifPresent(child -> {
                try {
                    if (child.getType().equals(JPVolume.TYPE)) {
                        JPVolume volume = (JPVolume) child;
                        analyzeVolume(volume, levelSorting);
                        analyzeNext(volume, levelSorting);
                    } else if (child.getType().equals(JPArticle.TYPE)) {
                        levelSorting.add("Artikel", JPMagicSorter.class, null);
                    }
                } catch (Exception exc) {
                    throw new MCRException("Unable to analyze " + child.getId().toString(), exc);
                }
            });
        }
    }

    private static void analyzeVolume(JPVolume volume, JPLevelSorting levelSorting) {
        Optional<Temporal> publishedAccessor = volume.getPublishedTemporal();
        if (publishedAccessor.isPresent()) {
            try {
                publishedAccessor.get().get(ChronoField.MONTH_OF_YEAR);
                levelSorting.add("Jahrgang", JPPublishedSorter.class, Order.ASCENDING);
                return;
            } catch (Exception exc) {
                // the temporal accessor does not contain a month value
                // -> continue
            }
        }
        String title = volume.getTitle();
        if (title.matches("[0-9]{4}")) {
            levelSorting.add("Jahrgang", JPPublishedSorter.class, Order.ASCENDING);
            return;
        }
        if (Arrays.stream(DateFormatSymbols.getInstance(Locale.GERMAN).getMonths())
                  .anyMatch(month -> !month.isEmpty() && title.toLowerCase().contains(month.toLowerCase()))) {
            levelSorting.add("Monat", JPPublishedSorter.class, Order.ASCENDING);
            return;
        }
        if (title.contains("Nr.")) {
            levelSorting.add("Nr.", JPMagicSorter.class, null);
            return;
        }
        levelSorting.add("Unknown", JPMagicSorter.class, null);
    }

    private static JPObjectConfiguration getConfig(MCRObjectID objectID) throws IOException {
        return new JPObjectConfiguration(objectID.toString(), LEVEL_SORTING);
    }

}
