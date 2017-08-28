package fsu.jportal.backend.sort;

import fsu.jportal.backend.JPPeriodicalComponent;

import java.time.LocalDate;
import java.util.Comparator;

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
            if (isOneNull(publishedDate1, publishedDate2)) {
                return handleNull(publishedDate1, publishedDate2);
            }
            return publishedDate1.compareTo(publishedDate2) * getOrder(order);
        };
    }

}
