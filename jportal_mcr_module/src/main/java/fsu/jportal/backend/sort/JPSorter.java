package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.MetadataManager;
import fsu.jportal.util.JPComponentUtil;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectStructure;

/**
 * Base interface for sorting children of journals and volumes.
 *
 * @author Matthias Eichner
 */
public interface JPSorter {

    enum Order {
        ASCENDING, DESCENDING
    }

    /**
     * Does the children order for the given container component.
     *
     * @param component the component
     * @param order ascending | descending
     * @throws JPSortException component is unable to be sorted
     */
    default void sort(JPContainer component, Order order) throws JPSortException {
        MCRObject mcrObject = component.getObject();
        MCRObjectStructure structure = mcrObject.getStructure();
        List<MCRMetaLinkID> children = new ArrayList<>(structure.getChildren());
        if (children.isEmpty()) {
            return;
        }
        structure.clearChildren();
        try {
            children.stream()
                    .map(MCRMetaLinkID::getXLinkHrefID)
                    .map(MetadataManager::retrieveMCRObject)
                    .map(JPComponentUtil::getPeriodical)
                    .sorted(getSortComparator(order))
                    .map(JPPeriodicalComponent::getObject)
                    .map(MCRObject::getId)
                    .map(id -> new MCRMetaLinkID("child", id, null, null))
                    .forEachOrdered(structure::addChild);
        } catch (Exception exc) {
            children.forEach(structure::addChild);
            throw new JPSortException("Unable to sort " + component.getId(), exc);
        }
    }

    default int getOrder(Order order) {
        return Order.DESCENDING.equals(order) ? -1 : 1;
    }

    default boolean isOneNull(Object a, Object b) {
        return a == null || b == null;
    }

    default Integer handleNull(Object a, Object b) {
        if (a == null && b == null) {
            return 0;
        }
        if (a == null) {
            return Integer.MIN_VALUE;
        }
        if (b == null) {
            return Integer.MAX_VALUE;
        }
        return null;
    }

    /**
     * Returns a Comparator to sort {@link JPPeriodicalComponent}.
     *
     * @param order the order
     * @return a comparator to sort
     */
    Comparator<? super JPPeriodicalComponent> getSortComparator(Order order);

}
