package fsu.jportal.backend;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mycore.datamodel.metadata.MCRMetaElement;
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

    public static String TYPE = JPObjectType.jparticle.name();

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
        setSize(String.valueOf(size));
    }

    public Optional<String> getSize() {
        MCRMetaElement sizes = object.getMetadata().getMetadataElement("sizes");
        if (sizes == null) {
            return Optional.empty();
        }
        MCRMetaLangText size = (MCRMetaLangText) sizes.getElementByName("size");
        if (size == null) {
            return Optional.empty();
        }
        return Optional.of(size.getText());
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
     * Returns the author of this article.
     *
     * @return author of this article
     */
    @Override
    public Optional<JPLegalEntity> getCreator() {
        Optional<JPLegalEntity> mainAuthor = getParticipant(JPObjectType.person, "mainAuthor");
        Optional<JPLegalEntity> author = getParticipant(JPObjectType.person, "author");
        return mainAuthor.map(Optional::of).orElse(author);
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

    /**
     * Creates a clone of the current article. Be aware that a new id is generated.
     */
    @Override
    public JPArticle clone() throws CloneNotSupportedException {
        JPArticle clone = (JPArticle) super.clone();
        clone.setTitle(getTitle());
        getSize().ifPresent(clone::setSize);
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
