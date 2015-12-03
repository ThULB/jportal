package fsu.jportal.backend;

import java.util.Optional;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectMetadata;

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
     * Returns the type of the component. One of person, jpinst, jpjournal, jpvolume or jparticle is returned here.
     * 
     * @return the tpye of the component
     */
    public abstract String getType();

    /**
     * Returns an optional of an metadata element.
     * 
     * @param metadataName name of the metadata element
     * @return an optional of the metadata element
     */
    protected Optional<MCRMetaElement> metadataElement(String metadataName) {
        MCRObjectMetadata metadata = object.getMetadata();
        if(metadata == null) {
            return Optional.empty();
        }
        MCRMetaElement metaElement = metadata.getMetadataElement(metadataName);
        return metaElement != null ? Optional.of(metaElement) : Optional.empty();
    }

    /**
     * Returns a mapped stream of all children of an element.
     * 
     * @param metadataName name of the metadata element
     * @param type Extended class of <code>MCRMetaInterface</code>
     * @return a stream of metadata children
     */
    protected <T extends MCRMetaInterface> Stream<T> metadataStream(String metadataName, Class<T> type) {
        Optional<MCRMetaElement> metadata = metadataElement(metadataName);
        // waiting for https://bugs.openjdk.java.net/browse/JDK-8050820
        if (!metadata.isPresent()) {
            return Stream.empty();
        }
        return StreamSupport.stream(metadata.get().spliterator(), false).map(m -> type.cast(m));
    }

    /**
     * Returns a mapped stream of elements where only children are returned which
     * are not inherited by an parent object.
     * 
     * @param metadataName name of the metadata element
     * @return a stream of metadata children
     */
    protected <T extends MCRMetaInterface> Stream<T> metadataStreamNotInherited(String metadataName, Class<T> type) {
        return metadataStream(metadataName, type).filter(m -> m.getInherited() == 0);
    }

}
