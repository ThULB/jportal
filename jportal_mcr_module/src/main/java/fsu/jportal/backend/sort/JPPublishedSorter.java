package fsu.jportal.backend.sort;

import java.util.Comparator;

import fsu.jportal.backend.JPPeriodicalComponent;
import org.mycore.datamodel.metadata.JPMetaDate;

/**
 * Orders the children by their published date.
 * 
 * @author Matthias Eichner
 */
public class JPPublishedSorter implements JPSorter {

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            JPMetaDate publishedDate1 = child1.getDate(JPPeriodicalComponent.DateType.published).orElse(null);
            JPMetaDate publishedDate2 = child2.getDate(JPPeriodicalComponent.DateType.published).orElse(null);
            if (isOneNull(publishedDate1, publishedDate2)) {
                return handleNull(publishedDate1, publishedDate2);
            }
            return publishedDate1.compareTo(publishedDate2) * getOrder(order);
        };
    }

}
