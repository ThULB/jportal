package fsu.jportal.backend.sort;

import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;

/**
 * Default implementation to sort jpvolume's by their hidden_position.
 * 
 * @author Matthias Eichner
 */
public class JPHiddenPositionSorter implements JPSorter {

    static Logger LOGGER = LogManager.getLogger(JPHiddenPositionSorter.class);

    @Override
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order) {
        return (child1, child2) -> {
            if (!JPComponentUtil.is(child1, JPVolume.TYPE) || !JPComponentUtil.is(child2, JPVolume.TYPE)) {
                return 0;
            }
            Integer pos1 = 0;
            Integer pos2 = 0;
            try {
                pos1 = ((JPVolume) child1).getHiddenPosition();
            } catch (Exception exc) {
                LOGGER.warn("Unable to get hidden position of " + child1.getId(), exc);
            }
            try {
                pos2 = ((JPVolume) child2).getHiddenPosition();
            } catch (Exception exc) {
                LOGGER.warn("Unable to get hidden position of " + child2.getId(), exc);
            }
            return Integer.compare(pos1, pos2) * (order.equals(Order.ASCENDING) ? 1 : -1);
        };
    }

}
