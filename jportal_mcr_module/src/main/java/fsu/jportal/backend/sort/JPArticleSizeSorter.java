package fsu.jportal.backend.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

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
            if(size1 == null) {
                return Integer.MIN_VALUE;
            }
            if(size2 == null) {
                return Integer.MAX_VALUE;
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
        int specialCharCompareResult = compareSpecialChar(size1, size2);
        if (specialCharCompareResult != 0) {
            return specialCharCompareResult * getOrderSign(order);
        }

        // Roman numerals: IV
        Integer result = compareRomanNumerals(size1, size2);
        if (result != null) {
            return result * getOrderSign(order);
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
        return Integer.compare(intSize1, intSize2) * getOrderSign(order);
    }

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

    private int getOrderSign(Order order) {
        return order.equals(Order.ASCENDING) ? 1 : -1;
    }

    public int compareSpecialChar(String size1, String size2) {
        List<String> pre = Arrays.asList("*");
        List<String> post = Arrays.asList("[", "K", "T");
        return Stream.concat(pre.stream(), post.stream()).mapToInt(c -> {
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
