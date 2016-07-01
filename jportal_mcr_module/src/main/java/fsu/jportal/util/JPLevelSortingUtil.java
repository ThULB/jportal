package fsu.jportal.util;

import java.io.IOException;
import java.text.DateFormatSymbols;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPArticle;
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
import fsu.jportal.common.Unthrow;

/**
 * Helper class to support level sorting on journals.
 * 
 * @author Matthias Eichner
 */
public abstract class JPLevelSortingUtil {

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
            Integer level = Integer.valueOf(key.substring(6, key.indexOf(".", 6)));
            return level;
        })).entrySet().stream().forEach(entry -> {
            // level
            Integer level = entry.getKey();
            // name
            String name = entry.getValue().stream().filter(e -> {
                return e.getKey().endsWith(".name");
            }).map(Entry::getValue).findAny().orElse("unknown");
            // sorter
            String sorter = entry.getValue().stream().filter(e -> {
                return e.getKey().endsWith(".sorter");
            }).map(Entry::getValue).findAny().orElse(null);
            // order
            Order order = entry.getValue().stream().filter(e -> {
                return e.getKey().endsWith(".order");
            }).map(Entry::getValue).map(Order::valueOf).findAny().orElse(null);
            Unthrow.wrapProc(() -> {
                Class<? extends JPSorter> sorterClass = getSorterClassByName(sorter);
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
        for (Level level : levelSorting.getLevelList()) {
            int index = levelSorting.getLevelList().indexOf(level);
            String baseKey = "level." + index;
            config.set(baseKey + ".name", level.getName());
            if (level.getSorterClass() != null) {
                config.set(baseKey + ".sorter", level.getSorterClass().getName());
            }
            if (level.getOrder() != null) {
                config.set(baseKey + ".order", level.getOrder().name());
            }
        }
        config.store();
    }

    /**
     * Runs through all descendants of the object and applies the sorters defined
     * in the given level sorting object.
     * 
     * @param objectID the object to apply the level sorting at
     * @param levelSorting the level sorting to apply
     */
    public static void apply(MCRObjectID objectID, JPLevelSorting levelSorting) {
    }

    public static void applySorter(List<MCRObjectID> objects, Class<? extends JPSorter> sorter) {
        objects.stream()
               .map(JPComponentUtil::getContainer)
               .filter(Optional::isPresent)
               .map(Optional::get)
               .forEach(container -> {

               });
    }

    /**
     * Analyzes the journal and returns a good starting point for the
     * level sorting structure.
     * 
     * @param objectID the journal id
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
                if (child.getType().equals(JPVolume.TYPE)) {
                    JPVolume volume = (JPVolume) child;
                    analyzeVolume(volume, levelSorting);
                    analyzeNext(volume, levelSorting);
                } else if (child.getType().equals(JPArticle.TYPE)) {
                    levelSorting.add("Artikel", JPMagicSorter.class, null);
                }
            });
        }
    }

    private static void analyzeVolume(JPVolume volume, JPLevelSorting levelSorting) {
        Optional<TemporalAccessor> publishedAccessor = volume.getPublishedTemporalAccessor();
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
        if (Arrays.asList(DateFormatSymbols.getInstance(Locale.GERMAN).getMonths()).stream().filter(month -> {
            return !month.isEmpty() && title.toLowerCase().contains(month.toLowerCase());
        }).findAny().isPresent()) {
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
