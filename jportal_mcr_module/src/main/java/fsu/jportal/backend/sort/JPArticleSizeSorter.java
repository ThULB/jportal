package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.common.RomanNumeral;
import fsu.jportal.util.JPComponentUtil;

/**
 * Default implementation to sort jparticle's by their size.
 * 
 * @author Matthias Eichner
 */
public class JPArticleSizeSorter implements JPSorter {

    static Logger LOGGER = LogManager.getLogger(JPArticleSizeSorter.class);

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            if (child1 == null || !JPComponentUtil.is(child1, JPArticle.TYPE)) {
                return Integer.MIN_VALUE;
            }
            if (child2 == null || !JPComponentUtil.is(child2, JPArticle.TYPE)) {
                return Integer.MAX_VALUE;
            }
            String size1 = ((JPArticle) child1).getSize();
            String size2 = ((JPArticle) child2).getSize();
            if (isOneNull(size1, size2)) {
                return handleNull(size1, size2);
            }
            try {
                return compare(order, size1, size2);
            } catch (Exception exc) {
                LOGGER.warn("Unable to compare " + child1.getId() + " with " + child2.getId(), exc);
                return Integer.MAX_VALUE;
            }
        };
    }

    public int compare(Order order, String size1, String size2) {
        size1 = getFirstNumIfRange(size1);
        size2 = getFirstNumIfRange(size2);

        // special characters: *, [, K, T
        Integer result = compareSpecialChar(size1, size2);
        if (result != null && result == 0) {
            return simpleCompare(order, size1, size2);
        } else if (result != null) {
            return result * getOrder(order);
        }

        // Roman numerals: IV
        result = compareRomanNumerals(size1, size2);
        if (result != null) {
            return result * getOrder(order);
        }

        // compare
        return compareMulti(order, size1, size2);
    }

    private Integer compareMulti(Order order, String size1, String size2) {
        if (!size1.contains("[") && !size2.contains("[")) {
            return simpleCompare(order, size1, size2);
        }
        String page1 = size1.split("\\[")[0];
        String page2 = size2.split("\\[")[0];
        Integer result = simpleCompare(order, page1, page2);
        if (result != 0) {
            return result;
        }
        String posOnPage1 = size1.contains("[") ? size1.split("\\[")[1] : "0";
        String posOnPage2 = size2.contains("[") ? size2.split("\\[")[1] : "0";
        return simpleCompare(order, posOnPage1, posOnPage2);
    }

    private int simpleCompare(Order order, String size1, String size2) {
        size1 = size1.replaceAll("[^0-9]", "");
        size2 = size2.replaceAll("[^0-9]", "");
        int intSize1 = getIntSize(size1);
        int intSize2 = getIntSize(size2);
        return Integer.compare(intSize1, intSize2) * getOrder(order);
    }

    /**
     * Compares two Roman numerals like III and IV. The comparison
     * range is between 1 and 3999.
     * 
     * @param size1 the first size
     * @param size2 the second size
     * @return null if no Roman numeral is found
     */
    private Integer compareRomanNumerals(String size1, String size2) {
        RomanNumeral roman1;
        RomanNumeral roman2;
        try {
            roman1 = new RomanNumeral(size1);
        } catch (NumberFormatException nfe) {
            roman1 = null;
        }
        try {
            roman2 = new RomanNumeral(size2);
        } catch (NumberFormatException nfe) {
            roman2 = null;
        }
        if (roman1 == null && roman2 == null) {
            return null;
        }
        return roman1 == null ? 1 : (roman2 == null ? -1 : Integer.compare(roman1.toInt(), roman2.toInt()));
    }

    private String getFirstNumIfRange(String range) {
        return range.split("-")[0];
    }

    /**
     * Compares the special characters "*, [, K and T" at the start
     * of the size. E.g.:
     * <li>[Anfang] 50</li>
     * <li>K 01</li>
     * <li>T 01</li>
     * <li>*30</li>
     * <p>
     * All sizes starting with "*" should appear at the start, all
     * others at the end.
     * </p>
     * If null is returned, none of the special characters are occur
     * either on size1 or size2. If zero is returned the special
     * characters occurred, but this method does not know how to
     * further process it. 
     * 
     * @param size1 the first size
     * @param size2 the second size
     * @return null if no special character is found
     */
    public Integer compareSpecialChar(String size1, String size2) {
        List<String> pre = Arrays.asList("*");
        List<String> post = Arrays.asList("[", "K", "T");
        List<String> concat = new ArrayList<>(pre);
        concat.addAll(post);
        if (!concat.stream().filter(c -> {
            return size1.startsWith(c) || size2.startsWith(c);
        }).findAny().isPresent()) {
            return null;
        }
        return concat.stream().mapToInt(c -> {
            Integer order = pre.contains(c) ? -1 : 1;
            return order * Boolean.compare(size1.startsWith(c), size2.startsWith(c));
        }).sum();
    }

    private int getIntSize(String size) {
        try {
            return Integer.valueOf(size);
        } catch (NumberFormatException nfe) {
            throw new NumberFormatException("Unable to format size " + size);
        }
    }

}
