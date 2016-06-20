package fsu.jportal.util;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPObjectConfiguration;
import fsu.jportal.backend.sort.JPLevelSorting;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.common.Pair;
import fsu.jportal.common.Triple;
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
            Integer level = Integer.valueOf(key.substring(5, key.indexOf(".", 6)));
            return level;
        })).entrySet().stream().map(entry -> {
            Integer level = entry.getKey();
            String name = entry.getValue().stream().filter(e -> {
                return e.getKey().endsWith(".name");
            }).map(Entry::getValue).findAny().orElse("unknown");
            String sorter = entry.getValue().stream().filter(e -> {
                return e.getKey().endsWith(".sorter");
            }).map(Entry::getValue).findAny().orElse(null);
            return new Triple<Integer, String, String>(level, name, sorter);
        }).forEach(triple -> {
            Unthrow.wrapProc(() -> {
                String sorterAsString = triple.getRight();
                Class<? extends JPSorter> sorterClass = null;
                if (sorterAsString != null && sorterAsString.length() > 0) {
                    sorterClass = Class.forName(sorterAsString).asSubclass(JPSorter.class);
                }
                levelSorting.set(triple.getLeft(), triple.getMiddle(), sorterClass);
            });
        });
        return levelSorting;
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
        for (Pair<String, Class<? extends JPSorter>> pair : levelSorting.getLevelList()) {
            int level = levelSorting.getLevelList().indexOf(pair);
            String baseKey = "level." + level;
            config.set(baseKey + ".name", pair.getKey());
            config.set(baseKey + ".sorter", pair.getValue().getName());
        }
        config.store();
    }

    private static JPObjectConfiguration getConfig(MCRObjectID objectID) throws IOException {
        return new JPObjectConfiguration(objectID.toString(), LEVEL_SORTING);
    }
}
