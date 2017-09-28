package fsu.jportal.backend.sort;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class JPMaintitleSorterTest {

    @Test
    public void compare() {
        JPMaintitleSorter sorter = new JPMaintitleSorter();

        // default
        assertTrue(0 == sorter.compare(JPSorter.Order.ASCENDING,"Jena", "Jena"));
        assertTrue(0 > sorter.compare(JPSorter.Order.ASCENDING,"Aachen", "Berlin"));
        assertTrue(0 > sorter.compare(JPSorter.Order.ASCENDING,"Aachen", "Augsburg"));
        assertTrue(0 < sorter.compare(JPSorter.Order.ASCENDING,"Augsburg", "Aachen"));

        // umlaute
        assertTrue(0 > sorter.compare(JPSorter.Order.ASCENDING,"Deutschland", "Österreich"));
        assertTrue(0 < sorter.compare(JPSorter.Order.ASCENDING,"Zypern", "Österreich"));

        // lowercase
        assertTrue(0 == sorter.compare(JPSorter.Order.ASCENDING,"jena", "Jena"));
    }

}
