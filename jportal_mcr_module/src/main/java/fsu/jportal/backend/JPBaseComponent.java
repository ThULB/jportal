package fsu.jportal.backend;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Base component for person, jpinst, jparticle, jpvolume and jpjournal.
 * 
 * @author Matthias Eichner
 */
public abstract class JPBaseComponent implements JPComponent {

    protected MCRObject object;

    /**
     * Creates a new <code>MCRObject</code> based on the {@link #getType()} method.
     */
    public JPBaseComponent() {
        object = new MCRObject();
        object.setId(MCRObjectID.getNextFreeId("jportal_" + getType()));
        object.setSchema("datamodel-" + getType() + ".xsd");
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPBaseComponent(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPBaseComponent(MCRObjectID mcrId) {
        this(MCRMetadataManager.retrieveMCRObject(mcrId));
    }

    /**
     * Creates a new JPComponent container for the mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPBaseComponent(MCRObject mcrObject) {
        if (!mcrObject.getId().getTypeId().equals(getType())) {
            throw new IllegalArgumentException("Object " + mcrObject.getId() + " is not a " + getType());
        }
        this.object = mcrObject;
    }

    @Override
    public MCRObject getObject() {
        return object;
    }

    @Override
    public void store() throws MCRPersistenceException, MCRActiveLinkException {
        MCRMetadataManager.update(object);
    }

    /**
     * Returns the type of the component. One of person, jpinst, jpjournal, jpvolume or jparticle should be returned.
     * 
     * @return the tpye of the component
     */
    public abstract String getType();

}
