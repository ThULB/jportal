package fsu.jportal.backend.sort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectStructure;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.util.JPComponentUtil;

/**
 * Base interface for sorting children of journals and volumes.
 * 
 * @author Matthias Eichner
 */
public interface JPSorter {

    public static enum Order {
        ASCENDING, DESCENDING
    }

    /**
     * Does the children order for the given container component.
     * 
     * @param component the component
     * @param order ascending | descending
     */
    default public void sort(JPContainer component, Order order) {
        MCRObject mcrObject = component.getObject();
        MCRObjectStructure structure = mcrObject.getStructure();
        List<MCRMetaLinkID> children = new ArrayList<>(structure.getChildren());
        structure.clearChildren();
        children.stream()
                .map(MCRMetaLinkID::getXLinkHrefID)
                .map(MCRMetadataManager::retrieveMCRObject)
                .map(JPComponentUtil::getPeriodical)
                .sorted(getSortComparator(order))
                .map(JPPeriodicalComponent::getObject)
                .map(MCRObject::getId)
                .map(id -> {
                    return new MCRMetaLinkID("child", id, null, null);
                })
                .forEachOrdered(structure::addChild);
    }

    /**
     * Returns a Comparator to sort {@link JPPeriodicalComponent}.
     * 
     * @param order the order
     * @return a comparator to sort
     */
    public Comparator<? super JPPeriodicalComponent> getSortComparator(Order order);

}
