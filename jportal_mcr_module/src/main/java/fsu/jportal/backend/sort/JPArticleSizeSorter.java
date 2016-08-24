package fsu.jportal.backend.sort;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Comparator;

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
            if (!JPComponentUtil.is(child1, JPArticle.TYPE) || !JPComponentUtil.is(child2, JPArticle.TYPE)) {
                return 0;
            }
            String size1 = ((JPArticle) child1).getSize();
            String size2 = ((JPArticle) child2).getSize();
            if (size1 == null || size2 == null) {
                return 0;
            }

            int specialCharCompareResult = compareSpecialChar(size1, size2);
            if (specialCharCompareResult != 0) {
                return specialCharCompareResult * getOrderSign(order);
            }

            size1 = size1.replaceAll("[^0-9]", "");
            size2 = size2.replaceAll("[^0-9]", "");
            int intSize1 = getIntSize(child1, size1);
            int intSize2 = getIntSize(child2, size2);
            return Integer.compare(intSize1, intSize2) * getOrderSign(order);

        };
    }

    private int getOrderSign(Order order) {
        return order.equals(Order.ASCENDING) ? 1 : -1;
    }

    private int compareSpecialChar(String size1, String size2) {
        return Arrays.stream(new String[] { "*", "[" })
                        .mapToInt(c -> getSign(c) * Boolean.compare(size1.startsWith(c), size2.startsWith(c))).sum();
    }

    private int getSign(String c) {
        return "*".equals(c)? -1 : 1;
    }

    private int getIntSize(JPPeriodicalComponent child, String size) {
        try {
            return Integer.valueOf(size);
        } catch (NumberFormatException nfe) {
            LOGGER.warn("Unable to format size of " + child.getId().toString(), nfe);
            return Integer.MAX_VALUE;
        }
    }

}
