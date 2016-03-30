package fsu.jportal.backend;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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

}
