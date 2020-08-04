package fsu.jportal.backend;

import java.util.Arrays;
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
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaElementXML;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.event.AutoSortHandler;
import fsu.jportal.backend.mcr.MetadataManager;
import fsu.jportal.backend.sort.JPSorter;
import fsu.jportal.backend.sort.JPSorter.Order;
import fsu.jportal.util.Pair;

/**
 * Component that can contain other components as children.
 *
 * @author Matthias Eichner
 */
public abstract class JPContainer extends JPPeriodicalComponent {

    protected LinkedHashMap<MCRObjectID, JPObjectComponent> childrenMap;

    public JPContainer() {
        super();
        childrenMap = new LinkedHashMap<>();
    }

    public JPContainer(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    public JPContainer(MCRObjectID mcrId) {
        this(MetadataManager.retrieveMCRObject(mcrId));
    }

    public JPContainer(MCRObject mcrObject) {
        super(mcrObject);
        childrenMap = new LinkedHashMap<>();
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
     * @param id the child to remove
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

    /**
     * Returns a list of the children of the given type of this container. Changes to this
     * list are not reflected to the mycore object.
     *
     * @param type the object type to return e.g. volume or article
     * @return list of mycore object identifiers
     */
    public List<MCRObjectID> getChildren(JPObjectType type) {
        return getObject().getStructure()
            .getChildren()
            .stream()
            .map(MCRMetaLinkID::getXLinkHrefID)
            .filter(linkId -> linkId.getTypeId().equals(type.name()))
            .collect(Collectors.toList());
    }

    /**
     * Returns a stream of children volumes.
     *
     * @return stream of volumes
     */
    public Stream<JPVolume> streamVolumes() {
        return getChildren(JPObjectType.jpvolume).stream().map(JPVolume::new);
    }

    /**
     * Returns a stream of children articles.
     *
     * @return stream of articles
     */
    public Stream<JPArticle> streamArticles() {
        return getChildren(JPObjectType.jparticle).stream().map(JPArticle::new);
    }

    @Override
    public void store(StoreOption... options) throws MCRPersistenceException, MCRAccessException {
        super.store(options);
        if (Arrays.asList(options).contains(StoreOption.children)) {
            for (JPComponent component : childrenMap.values()) {
                component.store();
            }
            childrenMap.clear();
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
        Stream<MCRMetaElementXML> stream = metadataStream("autosort", MCRMetaElementXML.class);
        return stream.map(MCRMetaElementXML::createXML).map(sortByElement -> {
            try {
                Class<?> forName = Class.forName(sortByElement.getText());
                String orderAttribute = sortByElement.getAttributeValue("order");
                Order order = orderAttribute != null ? Order.valueOf(orderAttribute.toUpperCase()) : null;
                if (JPSorter.class.isAssignableFrom(forName)) {
                    return new Pair<>((JPSorter) forName.getDeclaredConstructor().newInstance(), order);
                } else {
                    throw new MCRException("The sorter class " + forName.getName() + " is not a subclass of JPSorter.");
                }
            } catch (Exception exc) {
                throw new MCRException("Unable to retrieve sorter for object " + getObject().getId(), exc);
            }
        }).findFirst();
    }

    /**
     * Sets the autosort for this container. If the sorter is null,
     * the autosort is removed.
     *
     * @param sorterClass class of the JPSorter
     * @param order ascending or descending
     */
    public void setSortBy(Class<? extends JPSorter> sorterClass, Order order) {
        object.getMetadata().removeMetadataElement("autosort");
        if (sorterClass == null) {
            return;
        }
        MCRMetaElement autosort = new MCRMetaElement(MCRMetaElementXML.class, "autosort", false, true, null);
        object.getMetadata().setMetadataElement(autosort);
        MCRMetaElementXML metaXML = new MCRMetaElementXML();
        Element sortby = new Element("sortby");
        if (order != null) {
            sortby.setAttribute("order", order.name().toLowerCase());
        }
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
        getSortBy().ifPresent(pair -> pair.getKey().sort(component, pair.getValue()));
    }

}
