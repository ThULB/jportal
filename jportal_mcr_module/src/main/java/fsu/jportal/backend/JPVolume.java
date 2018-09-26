package fsu.jportal.backend;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mycore.common.inject.MCRInjectorConfig;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.gnd.GNDAreaCodesService;

/**
 * Volume abstraction. Be aware that this class is not complete.
 * 
 * @author Matthias Eichner
 */
public class JPVolume extends JPContainer {

    public static String TYPE = JPObjectType.jpvolume.name();

    public JPVolume() {
        super();
    }

    public JPVolume(String mcrId) {
        super(mcrId);
    }

    public JPVolume(MCRObjectID mcrId) {
        super(mcrId);
    }

    public JPVolume(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * A volume in jportal is used for many different types of objects. This returns a more specific type. E.g.
     * issue, year, month or calendar.
     *
     * @return types of the volume or an empty list if its not specified, unknown or cannot be detected
     */
    public List<String> getVolumeTypes() {
        JPVolumeTypeDetector typeDetector = MCRInjectorConfig.injector().getInstance(JPVolumeTypeDetector.class);
        return typeDetector.detect(this);
    }

    /**
     * Checks if this volume has the given type.
     * 
     * @param type the type to check
     * @return true if this volume is of the same type
     */
    public boolean isVolumeType(String type) {
        return getVolumeTypes().contains(type);
    }

    public void setHiddenPosition(String position) {
        setText("hidden_positions", "hidden_position", position, null, false, false);
    }

    public void setHiddenPosition(int position) {
        setHiddenPosition(String.valueOf(position));
    }

    public void addSubTitle(String title, String type) {
        addText("subtitles", "subtitle", title, type, false, true);
    }

    /**
     * Sets the parent of this component.
     * 
     * @param parentId a mycore object id
     */
    public void setParent(String parentId) {
        setParent(MCRObjectID.getInstance(parentId));
    }

    /**
     * Sets the parent of this component.
     * 
     * @param parentId a mycore object id
     */
    public void setParent(MCRObjectID parentId) {
        MCRMetaLinkID link = new MCRMetaLinkID("parent", 0);
        link.setReference(parentId, null, null);
        object.getStructure().setParent(link);
    }

    /**
     * Sets the note for this volume.
     * 
     * @param note note text
     */
    public void setNote(String note) {
        setText("notes", "note", note, null, false, true);
    }

    /**
     * Returns the note.
     * 
     * @return optional note
     */
    public Optional<String> getNote() {
        return getText("notes", null);
    }

    /**
     * Returns the publisher of this volume.
     *
     * @return publisher of this volume
     */
    @Override
    public Optional<JPLegalEntity> getCreator() {
        Optional<JPLegalEntity> mainPublisher = getParticipant(JPObjectType.jpinst, "mainPublisher");
        Optional<JPLegalEntity> publisher = getParticipant(JPObjectType.jpinst, "publisher");
        return mainPublisher.map(Optional::of).orElse(publisher);
    }

    /**
     * Returns the hidden position of this volume.
     * 
     * @return position in parent
     */
    public Integer getHiddenPosition() {
        String hiddenPosition = getText("hidden_positions", null).orElse("0");
        try {
            String replaced = hiddenPosition.replaceAll("[^0-9]", "");
            return Integer.valueOf(replaced);
        } catch (Exception exc) {
            LOGGER.warn("Unable to parse the hidden position ({}) of {} to an integer value.", hiddenPosition,
                getObject().getId());
            return 0;
        }
    }

    /**
     * Gets the volContentClassisX content as stream.
     *
     * @param number the number of the content classis.
     * @return stream of categories
     */
    public Stream<MCRCategoryID> streamVolContentClassis(int number) {
        return metadataStream("volContentClassis" + number, MCRMetaClassification.class)
            .map(mc -> new MCRCategoryID(mc.getClassId(), mc.getCategId()));
    }

    /**
     * Checks if there is volContentClassis{number} with the given classId and categId.
     *
     * @param number the number of the content classis.
     * @param classId the classification id
     * @param categId the category id
     * @return true if the is a class and category
     */
    public boolean isVolContentClassis(int number, String classId, String categId) {
        return streamVolContentClassis(number).anyMatch(id -> {
            MCRCategoryID cmp = new MCRCategoryID(classId, categId);
            return cmp.equals(id);
        });
    }

}
