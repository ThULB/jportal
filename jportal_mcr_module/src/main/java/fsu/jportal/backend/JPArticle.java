package fsu.jportal.backend;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mycore.datamodel.common.MCRISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Simple java abstraction of a jportal article. This class is not complete at all.
 * 
 * @author Matthias Eichner
 */
public class JPArticle extends JPPeriodicalComponent implements Cloneable {

    public static String TYPE = "jparticle";

    public static enum RecensionDateType {
        published_Original, published_Original_From, published_Original_Till
    }

    /**
     * Creates a new jportal article.
     */
    public JPArticle() {
        super();
    }

    /**
     * Creates a new JPArticle container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPArticle(String mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPArticle container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPArticle(MCRObjectID mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPArticle container for the mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPArticle(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Articles can have recension dates. Use this method when you want to set a
     * recension date instead of a default published date. Be aware that articles
     * can have only one date, either recension or published.
     * 
     * @param from from recension
     * @param until until recension
     */
    public void setRecensionDate(String from, String until) {
        if (from == null) {
            object.getMetadata().removeMetadataElement("dates");
            return;
        }
        MCRMetaElement dates = new MCRMetaElement(MCRMetaISO8601Date.class, "dates", true, false, null);
        String fromType = until == null ? RecensionDateType.published_Original.name()
            : RecensionDateType.published_Original_From.name();
        dates.addMetaObject(buildISODate("date", from, fromType));
        if (until != null) {
            dates.addMetaObject(buildISODate("date", until, RecensionDateType.published_Original_Till.name()));
        }
        object.getMetadata().setMetadataElement(dates);
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

    public void setSize(String size) {
        setText("sizes", "size", size, null, false, false);
    }

    public void setSize(int size) {
        setSize(EIGHT_DIGIT_FORMAT.format(Integer.valueOf(size)));
    }

    public String getSize() {
        MCRMetaElement sizes = object.getMetadata().getMetadataElement("sizes");
        if (sizes == null) {
            return null;
        }
        MCRMetaLangText size = (MCRMetaLangText) sizes.getElementByName("size");
        if (size == null) {
            return null;
        }
        return size.getText();
    }

    public void setIdenti(String type, String id) {
        setText("identis", "identi", id, type, false, true);
    }

    /**
     * Adds a note to the article.
     * 
     * @param note the note to add
     * @param publicNote if its public or not
     */
    public void addNote(String note, boolean publicNote) {
        addText("notes", "note", note, (publicNote ? "annotation" : "internalNote"), false, true);
    }

    /**
     * Adds a keyword to the article.
     * 
     * @param keyword the keyword to add
     */
    public void addKeyword(String keyword) {
        addText("keywords", "keyword", keyword, null, false, true);
    }

    /**
     * Returns a list of keywords. The list is empty if there are no keywords.
     * 
     * @return list of keywords
     */
    public List<String> getKeywords() {
        return metadataStream("keywords", MCRMetaLangText.class).map(MCRMetaLangText::getText)
                                                                .collect(Collectors.toList());
    }

    @Override
    public Optional<LocalDate> getPublishedDate() {
        return getDate(DateType.published.name()).map(MCRMetaISO8601Date::getMCRISO8601Date)
                                                 .map(MCRISO8601Date::getDt)
                                                 .map(LocalDate::from);
    }

    /**
     * Creates a clone of the current article. Be aware that a new id is generated.
     */
    @Override
    public JPArticle clone() throws CloneNotSupportedException {
        JPArticle clone = (JPArticle) super.clone();
        clone.setTitle(getTitle());
        clone.setSize(getSize());
        String derivateLink = getDerivateLink();
        if (derivateLink != null) {
            try {
                clone.setDerivateLink(derivateLink);
            } catch (Exception exc) {
                throw new RuntimeException(
                    "Unable to set derivate link " + derivateLink + " to object " + clone.getObject().getId(), exc);
            }
        }
        // TODO: rubrics (heading)
        for (MCRMetaLinkID id : getParticipants()) {
            clone.addParticipant(id.getXLinkHrefID(), id.getXLinkLabel(), id.getType());
        }
        return clone;
    }
}
