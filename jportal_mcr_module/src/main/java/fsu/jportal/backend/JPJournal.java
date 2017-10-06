package fsu.jportal.backend;

import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Journal abstraction. Be aware this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPJournal extends JPContainer {

    public static String TYPE = JPObjectType.jpjournal.name();

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
     * Returns the ISO 639-1 language code of this journal.
     *
     * @return the language code
     */
    @Override
    public String getLanguageCode() {
        return metadataStreamNotInherited("languages", MCRMetaClassification.class)
                .map(MCRMetaClassification::getCategId)
                .findAny()
                .orElse(null);
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
     * Returns a list of journal types this journal is labeled as.
     * 
     * @return list of journal types
     */
    public List<MCRCategoryID> getJournalTypes() {
        return metadataStream("journalTypes", MCRMetaClassification.class).map(mc -> {
            return new MCRCategoryID(mc.getClassId(), mc.getCategId());
        }).collect(Collectors.toList());
    }

    /**
     * Checks if this journal is type of the given classification and category.
     * For example: isJournalType("jportal_class_00000200", "calendars");
     * 
     * @param classId the classification to check
     * @param categId the category to check
     * @return true if this journal is one of the type
     */
    public boolean isJournalType(String classId, String categId) {
        return getJournalTypes().stream().anyMatch(id -> {
            MCRCategoryID cmp = new MCRCategoryID(classId, categId);
            return cmp.equals(id);
        });
    }

    /**
     * Gets the contentClassisX content.
     * 
     * @param number the number of the content classis.
     * @return list of categories
     */
    public List<MCRCategoryID> getContentClassis(int number) {
        return metadataStream("contentClassis" + number, MCRMetaClassification.class).map(mc -> {
            return new MCRCategoryID(mc.getClassId(), mc.getCategId());
        }).collect(Collectors.toList());
    }

}
