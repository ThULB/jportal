package fsu.jportal.backend;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaClassification;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.util.DerivateLinkUtil;
import org.mycore.solr.classification.MCRSolrClassificationUtil;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Simple java abstraction of a jportal article. This class is not complete at all.
 * 
 * @author Matthias Eichner
 */
public class JPArticle implements JPComponent, Cloneable {

    final static DecimalFormat EIGHT_DIGIT_FORMAT;

    static {
        EIGHT_DIGIT_FORMAT = new DecimalFormat("00000000");
    }

    private MCRObject article;

    public JPArticle() {
        article = new MCRObject();
        article.setId(MCRObjectID.getNextFreeId("jportal_jparticle"));
        article.setSchema("datamodel-jparticle.xsd");
        article.setImportMode(true);
    }

    public MCRObject getObject() {
        return article;
    }

    public void setTitle(String title) {
        if (title == null) {
            article.getMetadata().removeMetadataElement("maintitles");
        }
        MCRMetaElement maintitles = new MCRMetaElement(MCRMetaLangText.class, "maintitles", true, false, null);
        maintitles.addMetaObject(new MCRMetaLangText("maintitle", null, null, 0, null, title));
        article.getMetadata().setMetadataElement(maintitles);
    }

    public String getTitle() {
        MCRMetaElement maintitles = article.getMetadata().getMetadataElement("maintitles");
        if (maintitles == null) {
            return null;
        }
        MCRMetaLangText maintitle = (MCRMetaLangText) maintitles.getElementByName("maintitle");
        if (maintitle == null) {
            return null;
        }
        return maintitle.getText();
    }

    @Override
    public void importComponent() throws MCRPersistenceException, MCRActiveLinkException {
        MCRMetadataManager.update(article);
    }

    public void setSize(String size) {
        if (size == null) {
            article.getMetadata().removeMetadataElement("sizes");
            return;
        }
        MCRMetaElement sizes = new MCRMetaElement(MCRMetaLangText.class, "sizes", false, false, null);
        sizes.addMetaObject(new MCRMetaLangText("size", null, null, 0, "plain", size));
        article.getMetadata().setMetadataElement(sizes);
    }

    public void setSize(int size) {
        setSize(EIGHT_DIGIT_FORMAT.format(Long.valueOf(size)));
    }

    public String getSize() {
        MCRMetaElement sizes = article.getMetadata().getMetadataElement("sizes");
        if (sizes == null) {
            return null;
        }
        MCRMetaLangText size = (MCRMetaLangText) sizes.getElementByName("size");
        if (size == null) {
            return null;
        }
        return size.getText();
    }

    public void addParticipant(MCRObjectID id, String title, String type) {
        MCRMetaElement participants = article.getMetadata().getMetadataElement("participants");
        if (participants == null) {
            participants = new MCRMetaElement(MCRMetaLinkID.class, "participants", false, false, null);
            article.getMetadata().setMetadataElement(participants);
        }
        MCRMetaLinkID link = new MCRMetaLinkID("participant", id, null, title);
        link.setType(type);
    }

    public List<MCRMetaLinkID> getParticipants() {
        List<MCRMetaLinkID> participantList = new ArrayList<MCRMetaLinkID>();
        MCRMetaElement participants = article.getMetadata().getMetadataElement("participants");
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
     * Adds the heading to the rubric classification.
     * 
     * @param heading
     * @throws SolrServerException
     */
    public void addHeading(String heading) throws SolrServerException {
        HttpSolrServer server = MCRSolrClassificationUtil.getCore().getServer();
        SolrDocument doc = MCRSolrSearchUtils.first(server, "+classification:jportal_class_rubric_llz +label.de:\""
            + heading + "\"");
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
        MCRMetaElement rubrics = article.getMetadata().getMetadataElement("rubrics");
        if (rubrics == null) {
            rubrics = new MCRMetaElement(MCRMetaClassification.class, "rubrics", false, false, null);
            article.getMetadata().setMetadataElement(rubrics);
        }
        MCRMetaClassification metaClassification = new MCRMetaClassification("rubric", 0, null, categId);
        rubrics.addMetaObject(metaClassification);
    }

    /**
     * Creates a clone of the current article. Be aware that a new id is generated.
     */
    @Override
    public JPArticle clone() throws CloneNotSupportedException {
        JPArticle clone = new JPArticle();
        clone.setTitle(getTitle());
        clone.setSize(getSize());
        // TODO: rubrics (heading)
        for (MCRMetaLinkID id : getParticipants()) {
            clone.addParticipant(id.getXLinkHrefID(), id.getXLinkLabel(), id.getType());
        }
        return clone;
    }

    public void setDerivateLink(MCRDerivate derivate, String href) throws MCRActiveLinkException {
        String pathOfImage = derivate.getId().toString() + "/" + href;
        DerivateLinkUtil.setLink(article, pathOfImage);
    }

}
