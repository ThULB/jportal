package fsu.jportal.backend.sort;

import java.util.Comparator;

import fsu.jportal.backend.JPPeriodicalComponent;

/**
 * Orders the children by their maintitle.
 * 
 * @author Matthias Eichner
 */
public class JPMaintitleSorter implements JPSorter {

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            String title1 = child1.getTitle();
            String title2 = child2.getTitle();
            if (title1 == null) {
                return Integer.MIN_VALUE;
            }
            if (title2 == null) {
                return Integer.MAX_VALUE;
            }
            return title1.compareTo(title2) * (order.equals(Order.ASCENDING) ? 1 : -1);
        };
    }

}
