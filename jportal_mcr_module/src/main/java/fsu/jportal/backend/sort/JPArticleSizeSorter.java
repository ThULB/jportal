package fsu.jportal.backend.sort;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPPeriodicalComponent;
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
            if (!JPComponentUtil.is(child1, JPArticle.TYPE) || !JPComponentUtil.is(child2, JPArticle.TYPE)) {
                return 0;
            }
            String size1 = ((JPArticle) child1).getSize();
            String size2 = ((JPArticle) child2).getSize();
            if (size1 == null || size2 == null) {
                return 0;
            }
            size1 = size1.replaceAll("[^0-9]", "");
            size2 = size2.replaceAll("[^0-9]", "");
            int intSize1 = getIntSize(child1, size1);
            int intSize2 = getIntSize(child2, size2);
            return Integer.compare(intSize1, intSize2) * (order.equals(Order.ASCENDING) ? 1 : -1);

        };
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
