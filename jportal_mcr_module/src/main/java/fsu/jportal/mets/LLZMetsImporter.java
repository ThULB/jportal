package fsu.jportal.mets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.common.content.MCRContent;
import org.mycore.common.content.transformer.MCRContentTransformer;
import org.mycore.common.xml.MCRLayoutTransformerFactory;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.IMetsElement;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPVolume;

/**
 * LLZ METS importer class.
 * 
 * @author Matthias Eichner
 */
public class LLZMetsImporter {

    private static Logger LOGGER = Logger.getLogger(LLZMetsImporter.class);

    private static final List<Namespace> NS_LIST;

    private static final XPathExpression<Element> LOGICAL_STRUCTMAP_EXPRESSION;

    private static final XPathExpression<Element> MODS_EXPRESSION;

    private static final XPathExpression<Text> TITLE_EXPRESSION;

    private static final XPathExpression<Text> HEADING_EXPRESSION;

    static {
        NS_LIST = new ArrayList<>();
        NS_LIST.add(MCRConstants.METS_NAMESPACE);
        NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        LOGICAL_STRUCTMAP_EXPRESSION = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, NS_LIST);
        MODS_EXPRESSION = XPathFactory.instance().compile("mets:dmdSec[@ID=$id]/mets:mdWrap/mets:xmlData/mods:mods",
            Filters.element(), vars, NS_LIST);
        TITLE_EXPRESSION = XPathFactory.instance().compile("mods:recordInfo/mods:recordOrigin/text()", Filters.text(), null,
            NS_LIST);
        HEADING_EXPRESSION = XPathFactory.instance().compile("/add/doc/field[@name='heading_base']/text()",
            Filters.text());
    }

    private Document mets;

    private String lastHeading;

    private JPArticle lastArticle;

    /**
     * List of errors which are not important enough to break the import process but
     * should be handled by the editor.
     */
    private List<String> errorList;

    /**
     * Imports the dmd section of the given mets document.
     * 
     * @param metsDocument
     * @param derivateId
     * @throws ConvertException something went so wrong that the import process has to be stopped
     */
    public void importMets(Document metsDocument, MCRObjectID derivateId) throws ConvertException {
        // reset values
        this.mets = metsDocument;
        this.lastHeading = null;
        this.lastArticle = null;
        this.errorList = new ArrayList<String>();

        try {
            // get corresponding mycore objects
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID volumeId = derivate.getOwnerID();
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(volumeId);
            JPVolume volume = new JPVolume(mcrObject);

            // run through mets
            Element rootDiv = LOGICAL_STRUCTMAP_EXPRESSION.evaluateFirst(metsDocument.getRootElement());
            handleLogicalDivs(rootDiv, volume);

            // import to mycore system
            volume.importComponent();
        } catch (Exception exc) {
            throw new ConvertException("Unable to import component", exc);
        }
    }

    private void handleLogicalDivs(Element parentDiv, JPVolume volume) throws ConvertException {
        List<Element> children = parentDiv.getChildren("div", IMetsElement.METS);
        for (Element div : children) {
            String type = div.getAttributeValue("TYPE").toLowerCase();
            if (type.equals("issue")) {
                JPVolume issue = new JPVolume();
                issue.setTitle(div.getAttributeValue("LABEL"));
                handleLogicalDivs(div, issue);
                volume.addChild(issue);
            } else if (type.equals("volumeparts")) {
                handleLogicalDivs(div, volume);
            } else if (type.equals("rezension")) {
                JPArticle article = buildArticle(div);
                if (article != null) {
                    lastArticle = article;
                } else if (lastArticle != null) {
                    try {
                        article = lastArticle.clone();
                    } catch (Exception exc) {
                        throw new ConvertException("Unable to clone last article", exc);
                    }
                } else {
                    throw new ConvertException("Cannot create article cause of missing DMDID. ID="
                        + div.getAttributeValue("ID"));
                }
                volume.addChild(article);
            } else if (type.equals("tp") || type.equals("preface") || type.equals("toc")) {
                JPArticle article = new JPArticle();
                String title = type.equals("tp") ? "Titelblatt" : type.equals("preface") ? "Vorwort" : "Register";
                article.setTitle(title);
                volume.addChild(article);
            } else if (type.equals("heading")) {
                lastHeading = div.getAttributeValue("LABEL");
            }
        }
    }

    private JPArticle buildArticle(Element logicalDiv) throws ConvertException {
        String dmdId = LLZMetsUtils.getDmDId(logicalDiv);
        // handle dmd id is null!
        if (dmdId == null) {
            return null;
        }
        MODS_EXPRESSION.setVariable("id", dmdId);
        Element mods = MODS_EXPRESSION.evaluateFirst(mets.getRootElement());
        if (mods == null) {
            throw new ConvertException("Could not find referenced dmd entry " + dmdId + " in dmd section.");
        }
        JPArticle article = buildArticle(mods, logicalDiv, dmdId);
        return article;
    }

    /**
     * Creates a new jparticle based on the given mods element. 
     * 
     * @param mods the mods element containing the article
     * @param logicalDiv the referenced logical div (mets:structMap[@TYPE='logical']/mets:div)
     * @param dmdId the dmdId
     * @return
     * @throws ConvertException
     */
    protected JPArticle buildArticle(Element mods, Element logicalDiv, String dmdId) throws ConvertException {
        String logicalId = logicalDiv.getAttributeValue("ID");
        JPArticle article = new JPArticle();
        MCRObjectID articleId = article.getObject().getId();
        // title
        Text title = TITLE_EXPRESSION.evaluateFirst(mods);
        if (title != null && title.getTextNormalize().length() > 0) {
            article.setTitle(title.getTextNormalize());
        } else {
            article.setTitle("undefined");
            String msg = articleId + ": Missing mods:titleInfo/mods:title of logical div " + logicalId;
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // participants - we just create participants which have a gnd id
        for (Element name : mods.getChildren("name", MCRConstants.MODS_NAMESPACE)) {
            try {
                MCRObjectID participantId = LLZMetsUtils.getOrCreatePerson(name);
                if (participantId == null) {
                    continue;
                }
                MCRContent participantContent = MCRXMLMetadataManager.instance().retrieveContent(participantId);
                MCRContentTransformer transformer = MCRLayoutTransformerFactory.getTransformer("mycoreobject-solr");
                MCRContent solrContent = transformer.transform(participantContent);
                Text participantTitle = HEADING_EXPRESSION.evaluateFirst(solrContent.asXML());
                article.addParticipant(participantId, participantTitle.getText(), "author");
            } catch (Exception exc) {
                String msg = articleId + ": Unable to import participant";
                LOGGER.error(msg, exc);
                getErrorList().add(msg);
            }
        }
        // order
        String order = logicalDiv.getAttributeValue("ORDER");
        if (order != null) {
            article.setSize(Integer.valueOf(order));
        } else {
            String msg = articleId + ": ORDER attribute of logical div " + logicalId + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // heading
        if (lastHeading != null) {
            try {
                article.addHeading(lastHeading);
            } catch (Exception exc) {
                LOGGER.error("Unable to set heading to logical div " + logicalDiv, exc);
                getErrorList().add(articleId + ": Unable to set heading to logical div " + logicalId);
            }
        }
        // TODO: derivate link

        return article;
    }

    public List<String> getErrorList() {
        return errorList;
    }

}
