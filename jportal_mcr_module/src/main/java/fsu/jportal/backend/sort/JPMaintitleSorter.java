package fsu.jportal.backend.sort;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

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
            if(isOneNull(title1, title2)) {
                return handleNull(title1, title2);
            }
            return compare(order, title1, title2);
        };
    }

    int compare(Order order, String title1, String title2) {
        Collator collator = Collator.getInstance(Locale.GERMAN);
        collator.setStrength(Collator.SECONDARY); // a == A, a < Ä
        return collator.compare(title1, title2) * getOrder(order);
    }

}
