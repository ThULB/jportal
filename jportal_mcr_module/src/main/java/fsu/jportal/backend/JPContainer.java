package fsu.jportal.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaElementXML;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.event.AutoSortHandler;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.common.Pair;

/**
 * Component that can contain other components as children.
 * 
 * @author Matthias Eichner
 */
public abstract class JPContainer extends JPPeriodicalComponent {

    protected LinkedHashMap<MCRObjectID, JPObjectComponent> childrenMap;

    public JPContainer() {
        super();
        childrenMap = new LinkedHashMap<MCRObjectID, JPObjectComponent>();
    }

    public JPContainer(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    public JPContainer(MCRObjectID mcrId) {
        this(MCRMetadataManager.retrieveMCRObject(mcrId));
    }

    public JPContainer(MCRObject mcrObject) {
        super(mcrObject);
        childrenMap = new LinkedHashMap<MCRObjectID, JPObjectComponent>();
    }

    /**
     * Adds a new child.
     * 
     * @param child the child to add
     */
    public void addChild(JPObjectComponent child) {
        MCRMetaLinkID link = new MCRMetaLinkID("parent", getObject().getId(), null, getTitle());
        child.getObject().getStructure().setParent(link);
        childrenMap.put(child.getObject().getId(), child);
    }

    /**
     * Removes the child by the given id. If there is no such child
     * nothing happens.
     * 
     * @param id
     */
    public void removeAddedChild(MCRObjectID id) {
        JPObjectComponent component = childrenMap.remove(id);
        if (component != null) {
            component.getObject().getStructure().setParent((MCRMetaLinkID) null);
        }
    }

    /**
     * Returns a unmodifiable collection of all children which are added
     * (due the {@link #addChild(JPObjectComponent)} method) and most
     * likely (not 100% safe) not stored to the system yet.
     * 
     * @return collection of added children
     */
    public Collection<JPComponent> getAddedChildren() {
        return Collections.unmodifiableCollection(childrenMap.values());
    }

    /**
     * Returns a list of the children of this container. Changes to this
     * list are not reflected to the mycore object.
     * 
     * @return list of mycore object identifiers
     */
    public List<MCRObjectID> getChildren() {
        return getObject().getStructure()
                          .getChildren()
                          .stream()
                          .map(MCRMetaLinkID::getXLinkHrefID)
                          .collect(Collectors.toList());
    }

    @Override
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException, IOException {
        super.store();
        for (JPComponent component : childrenMap.values()) {
            component.store();
        }
        childrenMap.clear();
    }

    /**
     * Returns the {@link JPSorter} and its {@link Order}. This is used for automatic
     * sorting of the components children.
     * 
     * @see #sort()
     * @see AutoSortHandler
     * 
     * @return a pair of JPSorter and Order
     */
    public Optional<Pair<JPSorter, Order>> getSortBy() {
        Stream<MCRMetaElementXML> stream = metadataStream("autosort", MCRMetaElementXML.class);
        return stream.map(metaXML -> metaXML.createXML()).map(sortByElement -> {
            try {
                Order order = Order.valueOf(sortByElement.getAttributeValue("order").toUpperCase());
                Class<?> forName = Class.forName(sortByElement.getText());
                if (JPSorter.class.isAssignableFrom(forName)) {
                    return new Pair<JPSorter, Order>((JPSorter) forName.newInstance(), order);
                } else {
                    throw new MCRException("The sorter class " + forName.getName() + " is not a subclass of JPSorter.");
                }

            } catch (Exception exc) {
                throw new MCRException("Unable to retrieve sorter for object " + getObject().getId(), exc);
            }
        }).findFirst();
    }

    /**
     * Sets the autosort for this container. If the sorter or the order is null,
     * the autosort is removed. 
     * 
     * @param sorter the JPSorter
     * @param order ascending or descending
     */
    public void setSortBy(Class<? extends JPSorter> sorterClass, Order order) {
        object.getMetadata().removeMetadataElement("autosort");
        if (sorterClass == null || order == null) {
            return;
        }
        MCRMetaElement autosort = new MCRMetaElement(MCRMetaElementXML.class, "autosort", false, true, null);
        object.getMetadata().setMetadataElement(autosort);
        MCRMetaElementXML metaXML = new MCRMetaElementXML();
        Element sortby = new Element("sortby");
        sortby.setAttribute("order", order.name().toLowerCase());
        sortby.setText(sorterClass.getName());
        metaXML.setFromDOM(sortby);
        autosort.addMetaObject(metaXML);
    }

    /**
     * Sorts the children of this container by its {@link JPSorter} and {@link Order}.
     * 
     * @see AutoSortHandler
     */
    public void sort() {
        JPContainer component = this;
        getSortBy().ifPresent(pair -> {
            pair.getKey().sort(component, pair.getValue());
        });
    }

}
