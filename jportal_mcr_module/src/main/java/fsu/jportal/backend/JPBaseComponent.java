package fsu.jportal.backend;

import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.*;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
    public void store() throws MCRPersistenceException, MCRActiveLinkException, MCRAccessException {
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
        return Optional.ofNullable(object.getMetadata()).map(m -> m.getMetadataElement(metadataName));
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

    /**
     * Simple helper method to set a MCRMetaLang text. The 'form' attribute value is 'plain'.
     * The 'inherited' attribute value is '0'. To remove the text simply set the value to null.
     * Does nothing when the enclosingTag or the tag is null.
     * 
     * @param enclosingTag the enclosing tag e.g. def.placeOfBirth
     * @param tag the sub tag e.g. placeOfBirth
     * @param value the text value to set e.g. Jena
     * @param type the type value e.g. City
     * @param heritable set this flag to true if all child objects of this element can inherit this data
     * @param notInherit set this flag to true if this element should not inherit from his parent object
     */
    protected void setText(String enclosingTag, String tag, String value, String type, boolean heritable,
        boolean notInherit) {
        if (enclosingTag == null || tag == null) {
            return;
        }
        if (value == null) {
            object.getMetadata().removeMetadataElement(enclosingTag);
            return;
        }
        MCRMetaElement metaElement = new MCRMetaElement(MCRMetaLangText.class, enclosingTag, heritable, notInherit,
            null);
        metaElement.addMetaObject(new MCRMetaLangText(tag, null, null, 0, "plain", value));
        object.getMetadata().setMetadataElement(metaElement);
    }

    /**
     * Simple helper method to get the text of an not inherited MCRMetaLangText metadata element.
     * 
     * @param enclosingTag the enclosing tag e.g. def.placeOfBirth
     * @param type the type, can be null
     * @return an optional containing the text
     */
    protected Optional<String> getText(String enclosingTag, String type) {
        return metadataStreamNotInherited(enclosingTag, MCRMetaLangText.class).filter(typeFilter(type))
            .map(MCRMetaLangText::getText).findFirst();
    }

    /**
     * Simple helper method to add a MCRMetaLang text. The 'form' attribute value is 'plain'.
     * The 'inherited' attribute value is '0'. Does nothing when the enclosingTag, the tag or
     * the value is null.
     * 
     * @param enclosingTag the enclosing tag e.g. notes
     * @param tag the sub tag e.g. note
     * @param value the text value to set e.g. Hello World
     * @param type the type value e.g. message
     * @param heritable set this flag to true if all child objects of this element can inherit this data
     * @param notInherit set this flag to true if this element should not inherit from his parent object
     */
    protected void addText(String enclosingTag, String tag, String value, String type, boolean heritable,
        boolean notInherit) {
        if (enclosingTag == null || tag == null || value == null) {
            return;
        }
        MCRMetaElement metaElement = object.getMetadata().getMetadataElement(enclosingTag);
        if (metaElement == null) {
            metaElement = new MCRMetaElement(MCRMetaLangText.class, enclosingTag, heritable, notInherit, null);
            object.getMetadata().setMetadataElement(metaElement);
        }
        MCRMetaLangText metaText = new MCRMetaLangText(tag, null, type, 0, "plain", value);
        metaElement.addMetaObject(metaText);
    }

    /**
     * Simple helper method to list the text of an not inherited MCRMetaLangText metadata element.
     * 
     * @return list of strings. The list is empty if the enclosing tag does not exists or does not
     *          match the type.
     */
    protected List<String> listText(String enclosingTag, String type) {
        return metadataStreamNotInherited(enclosingTag, MCRMetaLangText.class).filter(typeFilter(type))
            .map(MCRMetaLangText::getText).collect(Collectors.toList());
    }

    /**
     * Builds an <code>MCRMetaISO8601Date</code> based on the date and a type.
     * 
     * @param subTag the tag e.g. date or dateOfBirth
     * @param dateString the date, should be in form of YYYY-MM-DD, YYYY-MM or YYYY.
     * @param type the type of the date (e.g. published)
     * @return an {@link MCRMetaISO8601Date}
     */
    protected MCRMetaISO8601Date buildISODate(String subTag, String dateString, String type) {
        MCRMetaISO8601Date isoDate = new MCRMetaISO8601Date();
        isoDate.setSubTag(subTag);
        isoDate.setType(type);
        isoDate.setDate(dateString);
        return isoDate;
    }

    /**
     * Predicate to compare MCRMetaLangText types. Returns true if the given type is null
     * and the MCRMetaLangText type is null, or if both are equal().
     * 
     * @param type the type to compare
     * @return true if they are equal
     */
    protected Predicate<MCRMetaLangText> typeFilter(String type) {
        return new Predicate<MCRMetaLangText>() {
            @Override
            public boolean test(MCRMetaLangText metaText) {
                return (type == null && metaText.getType() == null) || (metaText.getType().equals(type));
            }
        };
    }
}
