package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fsu.jportal.common.Pair;
import fsu.jportal.util.JPLevelSortingUtil;

/**
 * 
 * @author Matthias Eichner
 */
public class JPLevelSorting {

    protected List<Pair<String, Class<? extends JPSorter>>> levelList;

    /**
     * Creates a new empty level sorting.
     */
    public JPLevelSorting() {
        this.levelList = new ArrayList<>();
    }

    /**
     * Sets the sorter for the given level. If the sorter is null, manual sorting is
     * assumed. To remove a level use the {@link #remove(int)} method.
     * 
     * @param level the level to set the sorter to
     * @param sorter the sorter
     */
    public void set(int level, String name, Class<? extends JPSorter> sorterClass) {
        this.levelList.add(level, new Pair<>(name, sorterClass));
    }

    /**
     * Appends a new level add the end of the level sorting list. If the sorter is null,
     * manual sorting is assumed.
     * 
     * @param name
     * @param sorterClass
     */
    public void add(String name, Class<? extends JPSorter> sorterClass) {
        this.levelList.add(new Pair<>(name, sorterClass));
    }

    /**
     * Removes the sorter and name from the level.
     * 
     * @param level the level to be removed
     * @return the pair<name, sorter> previously at the specified level
     */
    public Pair<String, Class<? extends JPSorter>> remove(int level) {
        return this.levelList.remove(level);
    }

    /**
     * Gets the name and sorter pair for the given level.
     * 
     * @param level the level of the pair to return 
     * @return the name sorter pair or null
     */
    public Pair<String, Class<? extends JPSorter>> get(int level) {
        return this.levelList.get(level);
    }

    /**
     * Returns a live list of the levels.
     * 
     * @return the level list
     */
    public List<Pair<String, Class<? extends JPSorter>>> getLevelList() {
        return levelList;
    }

    /**
     * Returns true if this level sorting is empty.
     * 
     * @return returns true if this level sorting is empty
     */
    public boolean isEmpty() {
        return levelList.isEmpty();
    }

    /**
     * Returns this level sorting as JSON. If the sorter parameter is empty,
     * manual sorting is assumed.
     * 
     * <pre>
     * {@code
     *   [
     *     {index: 0, name: 'Jahrgang', sorter: 'fsu.jportal.sort.JPMagicSorter'},
     *     {index: 1, name: 'Monat'},
     *     ...
     *   ]
     * }
     * </pre>
     * 
     * @return the level sorting as json array
     */
    public JsonArray toJSON() {
        JsonArray array = new JsonArray();
        levelList.forEach(pair -> {
            JsonObject levelobject = new JsonObject();
            levelobject.addProperty("index", levelList.indexOf(pair));
            levelobject.addProperty("name", pair.getKey());
            Class<? extends JPSorter> sorterClass = pair.getValue();
            if(sorterClass != null) {
                levelobject.addProperty("sorter", sorterClass.getName());
            }
            array.add(levelobject);
        });
        return array;
    }

    public static JPLevelSorting fromJSON(JsonArray array) throws ClassNotFoundException {
        JPLevelSorting sorting = new JPLevelSorting();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            String name = object.getAsJsonPrimitive("name").getAsString();
            JsonPrimitive sorterPrimitve = object.getAsJsonPrimitive("sorter");
            String className = sorterPrimitve != null ? sorterPrimitve.getAsString() : null;
            sorting.add(name, className != null ? JPLevelSortingUtil.getSorterClassByName(className) : null);
        }
        return sorting;
    }

}
