package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.List;

import fsu.jportal.common.Pair;

/**
 * 
 * @author Matthias Eichner
 */
public class JPLevelSorting {

    protected List<Pair<String, JPSorter>> levelList;

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
    public void set(int level, String name, JPSorter sorter) {
        this.levelList.add(level, new Pair<>(name, sorter));
    }

    /**
     * Removes the sorter and name from the level.
     * 
     * @param level the level to be removed
     * @return the pair<name, sorter> previously at the specified level
     */
    public Pair<String, JPSorter> remove(int level) {
        return this.levelList.remove(level);
    }

    /**
     * Gets the name and sorter pair for the given level.
     * 
     * @param level the level of the pair to return 
     * @return the name sorter pair or null
     */
    public Pair<String, JPSorter> get(int level) {
        return this.levelList.get(level);
    }

    /**
     * Returns a live list of the levels.
     * 
     * @return the level list
     */
    public List<Pair<String, JPSorter>> getLevelList() {
        return levelList;
    }

}
