package fsu.jportal.backend.sort;

import java.time.LocalDateTime;
import java.util.Comparator;

import fsu.jportal.backend.JPPeriodicalComponent;

/**
 * Sort objects by their &lt;servedate type="createdate" /&gt;.
 * 
 * @author Matthias Eichner
 */
public class JPCreateDateSorter implements JPSorter {

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            LocalDateTime date1 = child1.getCreateDate();
            LocalDateTime date2 = child2.getCreateDate();
            if(isOneNull(date1, date2)) {
                return handleNull(date1, date2);
            }
            return date1.compareTo(date2) * (order.equals(Order.ASCENDING) ? 1 : -1);
        };
    }

}
