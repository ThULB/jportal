package fsu.jportal.backend.sort;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.common.RomanNumeral;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Default implementation to sort jparticle's by their size.
 * 
 * @author Matthias Eichner
 */
public class JPArticleSizeSorter implements JPSorter {

    private static Logger LOGGER = LogManager.getLogger(JPArticleSizeSorter.class);

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            if (child1 == null || !JPComponentUtil.is(child1, JPArticle.TYPE)) {
                return Integer.MIN_VALUE;
            }
            if (child2 == null || !JPComponentUtil.is(child2, JPArticle.TYPE)) {
                return Integer.MAX_VALUE;
            }
            String size1 = ((JPArticle) child1).getSize().orElse(null);
            String size2 = ((JPArticle) child2).getSize().orElse(null);
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

    int compare(Order order, String size1, String size2) {
        String firstPart1 = getFirstNumIfRange(size1).trim();
        String firstPart2 = getFirstNumIfRange(size2).trim();

        // equal
        if (firstPart1.equals(firstPart2)) {
            firstPart1 = getSecondNumIfRange(size1);
            firstPart2 = getSecondNumIfRange(size2);
            if(firstPart1.equals(firstPart2)) {
                return 0;
            }
        }

        // column
        List<String> columns = Arrays.asList("Sp.", "S.", "S", "sp.", "s.", "s");
        for (String column : columns) {
            if (firstPart1.startsWith(column)) {
                firstPart1 = firstPart1.replace(column, "").trim();
            }
            if (firstPart2.startsWith(column)) {
                firstPart2 = firstPart2.replace(column, "").trim();
            }
        }

        // special characters: *, [, K, T
        Integer result = compareSpecialChar(firstPart1, firstPart2);
        if (result != null && result == 0) {
            return simpleCompare(order, firstPart1, firstPart2);
        } else if (result != null) {
            return result * getOrder(order);
        }

        // Roman numerals: IV
        result = compareRomanNumerals(firstPart1, firstPart2);
        if (result != null) {
            return result * getOrder(order);
        }

        // compare
        return compareMulti(order, firstPart1, firstPart2);
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
        size1 = size1.replaceAll("[^IVXLCDM]", "");
        size2 = size2.replaceAll("[^IVXLCDM]", "");
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

    private String getSecondNumIfRange(String range) {
        String[] split = range.split("-");
        return split.length > 1 ? split[1] : split[0];
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
    private Integer compareSpecialChar(String size1, String size2) {
        List<String> pre = Collections.singletonList("*");
        List<String> post = Arrays.asList("[", "K", "T");
        List<String> concat = new ArrayList<>(pre);
        concat.addAll(post);
        if (concat.stream().noneMatch(c -> {
            return size1.startsWith(c) || size2.startsWith(c);
        })) {
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
