package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.JPLevelSortingUtil;

/**
 * 
 * @author Matthias Eichner
 */
public class JPLevelSorting {

    protected List<Level> levelList;

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
     * @param order
     */
    public void set(int level, String name, Class<? extends JPSorter> sorterClass, Order order) {
        this.levelList.add(level, new Level(name, sorterClass, order));
    }

    /**
     * Appends a new level add the end of the level sorting list. If the sorter is null,
     * manual sorting is assumed.
     * 
     * @param name
     * @param sorterClass
     * @param order
     */
    public void add(String name, Class<? extends JPSorter> sorterClass, Order order) {
        this.levelList.add(new Level(name, sorterClass, order));
    }

    /**
     * Removes the sorter and name from the level.
     * 
     * @param level the level to be removed
     * @return the level at the previously specified position
     */
    public Level remove(int level) {
        return this.levelList.remove(level);
    }

    /**
     * Gets the name and sorter pair for the given level.
     * 
     * @param level the level of the pair to return 
     * @return the level or null if there is nothing defined
     */
    public Level get(int level) {
        try {
            return this.levelList.get(level);
        } catch(Exception exc) {
            return null;
        }
    }

    /**
     * Returns a live list of the levels.
     * 
     * @return the level list
     */
    public List<Level> getLevelList() {
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
     *     {index: 1, name: 'Monat', sorter: 'fsu.jportal.sort.JPPublishedSorter', order: 'ascending'},
     *     ...
     *   ]
     * }
     * </pre>
     * 
     * @return the level sorting as json array
     */
    public JsonArray toJSON() {
        JsonArray array = new JsonArray();
        levelList.forEach(level -> {
            JsonObject levelobject = new JsonObject();
            levelobject.addProperty("index", levelList.indexOf(level));
            levelobject.addProperty("name", level.getName());
            Class<? extends JPSorter> sorterClass = level.getSorterClass();
            if (sorterClass != null) {
                levelobject.addProperty("sorter", sorterClass.getName());
            }
            if (level.getOrder() != null) {
                levelobject.addProperty("order", level.getOrder().name().toLowerCase());
            }
            array.add(levelobject);
        });
        return array;
    }

    /**
     * Creates a new level sorting object based on the give JSON array.
     * 
     * @see JPLevelSorting#toJSON()
     * @param array the array containing the whole level sorting information
     * @return an instance of <code>JPLevelSorting</code>
     * @throws ClassNotFoundException one of the sorters couldn't be found
     */
    public static JPLevelSorting fromJSON(JsonArray array) throws ClassNotFoundException {
        JPLevelSorting sorting = new JPLevelSorting();
        for (JsonElement element : array) {
            JsonObject object = element.getAsJsonObject();
            // name
            String name = object.getAsJsonPrimitive("name").getAsString();
            // sorter
            JsonPrimitive sorterPrimitve = object.getAsJsonPrimitive("sorter");
            String className = sorterPrimitve != null ? sorterPrimitve.getAsString() : null;
            Class<? extends JPSorter> sorterClass = className != null
                ? JPLevelSortingUtil.getSorterClassByName(className) : null;
            // order
            JsonPrimitive orderPrimitive = object.getAsJsonPrimitive("order");
            Order order = orderPrimitive != null ? Order.valueOf(orderPrimitive.getAsString().toUpperCase()) : null;
            // add new Level
            sorting.add(name, sorterClass, order);
        }
        return sorting;
    }

    public static class Level {
        private String name;

        private Class<? extends JPSorter> sorterClass;

        private Order order;

        public Level(String name, Class<? extends JPSorter> sorterClass, Order order) {
            this.name = name;
            this.sorterClass = sorterClass;
            this.order = order;
        }

        public String getName() {
            return name;
        }

        public Order getOrder() {
            return order;
        }

        public Class<? extends JPSorter> getSorterClass() {
            return sorterClass;
        }

    }

}
