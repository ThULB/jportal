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
     * Returns an optional of the metadata.
     * 
     * @param metadataName name of the metadata element
     * @return an optional of the metadata element
     */
    protected Optional<MCRMetaElement> metadata(String metadataName) {
        MCRMetaElement metaElement = object.getMetadata().getMetadataElement(metadataName);
        if (metaElement == null) {
            return Optional.empty();
        }
        return Optional.of(metaElement);
    }

    /**
     * Streams the content of a metadata element.
     * 
     * @param metadataName name of the metadata element
     * @return a stream of metadata children
     */
    protected Stream<MCRMetaInterface> stream(String metadataName) {
        Optional<MCRMetaElement> metadata = metadata(metadataName);
        if (!metadata.isPresent()) {
            return Stream.empty();
        }
        return StreamSupport.stream(metadata.get().spliterator(), false);
    }

    /**
     * Streams the content of a metadata element. Only children which are
     * inherited == 0 are streamed, the rest is filtered.
     * 
     * @param metadataName name of the metadata element
     * @return a stream of metadata children
     */
    protected Stream<MCRMetaInterface> streamNotInherited(String metadataName) {
        return stream(metadataName).filter(m -> m.getInherited() == 0);
    }

}
