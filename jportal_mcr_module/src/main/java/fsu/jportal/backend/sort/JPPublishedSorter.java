package fsu.jportal.backend.sort;

import java.time.LocalDate;
import java.util.Comparator;

import fsu.jportal.backend.JPPeriodicalComponent;

/**
 * Orders the children by their published date.
 * 
 * @author Matthias Eichner
 */
public class JPPublishedSorter implements JPSorter {

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            LocalDate publishedDate1 = child1.getPublishedDate().orElse(null);
            LocalDate publishedDate2 = child2.getPublishedDate().orElse(null);
            if (publishedDate1 == null) {
                return Integer.MIN_VALUE;
            }
            if (publishedDate2 == null) {
                return Integer.MAX_VALUE;
            }
            return publishedDate1.compareTo(publishedDate2) * (order.equals(Order.ASCENDING) ? 1 : -1);
        };
    }

}
