package fsu.jportal.backend;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Journal abstraction. Be aware this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPJournal extends JPContainer {

    public static String TYPE = "jpjournal";

    public JPJournal() {
        super();
    }

    public JPJournal(String mcrId) {
        super(mcrId);
    }

    public JPJournal(MCRObjectID mcrId) {
        super(mcrId);
    }

    public JPJournal(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String getNameOfTemplate() {
        return getText("hidden_templates", null).orElse(null);
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

}
