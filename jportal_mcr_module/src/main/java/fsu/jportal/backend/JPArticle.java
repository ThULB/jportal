package fsu.jportal.backend;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.common.SolrDocument;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.solr.classification.MCRSolrClassificationUtil;
import org.mycore.solr.search.MCRSolrSearchUtils;

import fsu.jportal.backend.JPPeriodicalComponent.DateType;

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
        dates.addMetaObject(buildISODate(from, fromType));
        if (until != null) {
            dates.addMetaObject(buildISODate(until, RecensionDateType.published_Original_Till.name()));
        }
        object.getMetadata().setMetadataElement(dates);
    }

    public void setParent(String parentId) {
        MCRMetaLinkID parent = object.getStructure().getParent();
        if (parent == null) {
            parent = new MCRMetaLinkID("parent", 0);
            object.getStructure().setParent(parent);
        }
        parent.setReference(parentId, null, null);
    }

    public void setSize(String size) {
        if (size == null) {
            object.getMetadata().removeMetadataElement("sizes");
            return;
        }
        MCRMetaElement sizes = new MCRMetaElement(MCRMetaLangText.class, "sizes", false, false, null);
        sizes.addMetaObject(new MCRMetaLangText("size", null, null, 0, "plain", size));
        object.getMetadata().setMetadataElement(sizes);
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
        MCRMetaElement identis = object.getMetadata().getMetadataElement("identis");
        if (identis == null) {
            identis = new MCRMetaElement(MCRMetaLangText.class, "identis", false, true, null);
            object.getMetadata().setMetadataElement(identis);
        }
        identis.addMetaObject(new MCRMetaLangText("identi", null, type, 0, "plain", id));
    }

    public void addParticipant(MCRObjectID id, String title, String type) {
        MCRMetaElement participants = object.getMetadata().getMetadataElement("participants");
        if (participants == null) {
            participants = new MCRMetaElement(MCRMetaLinkID.class, "participants", false, false, null);
            object.getMetadata().setMetadataElement(participants);
        }
        MCRMetaLinkID link = new MCRMetaLinkID("participant", id, null, title);
        link.setType(type);
        participants.addMetaObject(link);
    }

    public List<MCRMetaLinkID> getParticipants() {
        List<MCRMetaLinkID> participantList = new ArrayList<MCRMetaLinkID>();
        MCRMetaElement participants = object.getMetadata().getMetadataElement("participants");
        if (participants == null) {
            return participantList;
        }
        Iterator<MCRMetaInterface> i = participants.iterator();
        while (i.hasNext()) {
            participantList.add((MCRMetaLinkID) i.next());
        }
        return participantList;
    }

    /**
     * Adds a note to the article.
     * 
     * @param note the note to add
     * @param publicNote if its public or not
     */
    public void addNote(String note, boolean publicNote) {
        MCRMetaElement notes = object.getMetadata().getMetadataElement("notes");
        if (notes == null) {
            notes = new MCRMetaElement(MCRMetaLangText.class, "notes", false, true, null);
            object.getMetadata().setMetadataElement(notes);
        }
        MCRMetaLangText mcrNote = new MCRMetaLangText("note", null, (publicNote ? "annotation" : "internalNote"), 0,
            "plain", note);
        notes.addMetaObject(mcrNote);
    }

    /**
     * Adds the heading to the rubric classification.
     * 
     * @param heading
     * @throws SolrServerException
     */
    public void addHeading(String heading) throws SolrServerException, IOException {
        HttpSolrClient client = MCRSolrClassificationUtil.getCore().getClient();
        SolrDocument doc = MCRSolrSearchUtils.first(client,
            "+classification:jportal_class_rubric_llz +label.de:\"" + heading + "\"");
        MCRCategoryID categId;
        String classId = "jportal_class_rubric_llz";
        if (doc == null) {
            // hate this api... its not even an api...
            MCRCategoryImpl category = new MCRCategoryImpl();
            categId = new MCRCategoryID(classId, UUID.randomUUID().toString());
            category.setId(categId);
            category.getLabels().add(new MCRLabel("de", heading, null));
            MCRCategoryDAOFactory.getInstance().addCategory(MCRCategoryID.rootID(classId), category);
        } else {
            categId = new MCRCategoryID(classId, doc.getFieldValue("category").toString());
        }
        MCRMetaElement rubrics = object.getMetadata().getMetadataElement("rubrics");
        if (rubrics == null) {
            rubrics = new MCRMetaElement(MCRMetaClassification.class, "rubrics", false, false, null);
            object.getMetadata().setMetadataElement(rubrics);
        }
        MCRMetaClassification metaClassification = new MCRMetaClassification("rubric", 0, null, categId);
        rubrics.addMetaObject(metaClassification);
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
