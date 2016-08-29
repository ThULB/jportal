package fsu.jportal.backend.sort;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.mycore.common.MCRTestCase;

import fsu.jportal.backend.sort.JPSorter.Order;

public class JPArticleSizeSorterTest extends MCRTestCase {

    @Test
    public void compare() {
        JPArticleSizeSorter sorter = new JPArticleSizeSorter();

        // simple tests
        assertTrue(0 == sorter.compare(Order.ASCENDING, "1", "1"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "1", "2"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "2", "1"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "2", "11"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "0001", "0002"));
        assertTrue(0 < sorter.compare(Order.DESCENDING, "0001", "0002"));

        // map & plate
        assertTrue(0 > sorter.compare(Order.ASCENDING, "0010", "K 01"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "K 01", "K 02"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "0010", "T 01"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "T 02", "T 01"));

        // Roman numeral (with asterisk)
        assertTrue(0 < sorter.compare(Order.ASCENDING, "0010", "*001"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "*001", "0010"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "*002", "*001"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "*110", "*005"));

        // map & asterisk
        assertTrue(0 > sorter.compare(Order.ASCENDING, "*001", "K 01"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "K 01", "*001"));

        // Roman numeral
        assertTrue(0 > sorter.compare(Order.ASCENDING, "III", "IV"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "CCCI", "CMXCIX"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "0001", "IV"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "IV", "0100"));

        // not the first article on a page
        assertTrue(0 > sorter.compare(Order.ASCENDING, "025 [1]", "025 [2]"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "025 [2]", "025 [1]"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "024", "025 [1]"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "025 [1]", "024"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "024 [1]", "025"));
        assertTrue(0 < sorter.compare(Order.ASCENDING, "025", "024 [1]"));
        assertTrue(0 > sorter.compare(Order.ASCENDING, "025 [2]", "025 [11]"));
    }

}
