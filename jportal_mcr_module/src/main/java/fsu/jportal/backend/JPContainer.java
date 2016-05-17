package fsu.jportal.backend;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.solr.common.util.Pair;
import org.jdom2.Element;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetaXML;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.event.AutoSortHandler;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;

/**
 * Component that can contain other components as children.
 * 
 * @author Matthias Eichner
 */
public abstract class JPContainer extends JPPeriodicalComponent {

    protected Map<MCRObjectID, JPObjectComponent> childrenMap;

    public JPContainer() {
        super();
        childrenMap = new HashMap<MCRObjectID, JPObjectComponent>();
    }

    public JPContainer(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    public JPContainer(MCRObjectID mcrId) {
        this(MCRMetadataManager.retrieveMCRObject(mcrId));
    }

    public JPContainer(MCRObject mcrObject) {
        super(mcrObject);
        childrenMap = new HashMap<MCRObjectID, JPObjectComponent>();
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
    public void removeChild(MCRObjectID id) {
        JPObjectComponent component = childrenMap.remove(id);
        if (component != null) {
            component.getObject().getStructure().setParent((MCRMetaLinkID) null);
        }
    }

    /**
     * Returns a unmodifiable collection of all children.
     * 
     * @return collection of children
     */
    public Collection<JPComponent> getChildren() {
        return Collections.unmodifiableCollection(childrenMap.values());
    }

    @Override
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException, IOException {
        super.store();
        for (JPComponent component : childrenMap.values()) {
            component.store();
        }
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
        Stream<MCRMetaXML> stream = metadataStream("sorters", MCRMetaXML.class);
        return stream.flatMap(metaXML -> metaXML.getContent().stream())
                     .filter(c -> c instanceof Element)
                     .map(Element.class::cast)
                     .map(sorterElement -> {
                         try {
                             Order order = Order.valueOf(sorterElement.getAttributeValue("order"));
                             Class<?> forName = Class.forName(sorterElement.getText());
                             if (JPSorter.class.isAssignableFrom(forName)) {
                                 return new Pair<JPSorter, Order>((JPSorter) forName.newInstance(), order);
                             } else {
                                 throw new MCRException(
                                     "The sorter class " + forName.getName() + " is not a subclass of JPSorter.");
                             }

                         } catch (Exception exc) {
                             throw new MCRException("Unable to retrieve sorter for object " + getObject().getId(), exc);
                         }
                     })
                     .findFirst();
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
