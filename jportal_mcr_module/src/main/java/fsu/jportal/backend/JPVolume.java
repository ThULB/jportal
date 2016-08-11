package fsu.jportal.backend;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Volume abstraction. Be aware that this class is not complete.
 * 
 * @author Matthias Eichner
 */
public class JPVolume extends JPContainer {

    public static String TYPE = "jpvolume";

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
            LOGGER.warn("Unable to parse the hidden position (" + hiddenPosition + ") of " + getObject().getId()
                + " to an integer value.");
            return 0;
        }
    }

}
