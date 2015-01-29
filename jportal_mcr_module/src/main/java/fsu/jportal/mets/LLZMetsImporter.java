package fsu.jportal.mets;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jdom2.Attribute;
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
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.IMetsElement;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.mets.LLZMetsUtils.TiffHrefStrategy;

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

    private static final XPathExpression<Attribute> FILEID_EXPRESSION;

    private static final XPathExpression<Attribute> FILE_EXPRESSION;

    static {
        NS_LIST = new ArrayList<>();
        NS_LIST.add(MCRConstants.METS_NAMESPACE);
        NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        NS_LIST.add(MCRConstants.XLINK_NAMESPACE);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        LOGICAL_STRUCTMAP_EXPRESSION = XPathFactory.instance().compile(
            "mets:structMap[@TYPE='logical_structmap']/mets:div", Filters.element(), null, NS_LIST);
        MODS_EXPRESSION = XPathFactory.instance().compile("mets:dmdSec[@ID=$id]/mets:mdWrap/mets:xmlData/mods:mods",
            Filters.element(), vars, NS_LIST);
        TITLE_EXPRESSION = XPathFactory.instance().compile("mods:recordInfo/mods:recordOrigin/text()", Filters.text(),
            null, NS_LIST);
        HEADING_EXPRESSION = XPathFactory.instance().compile("/add/doc/field[@name='heading_base']/text()",
            Filters.text());
        FILEID_EXPRESSION = XPathFactory.instance().compile(
            "mets:div/mets:fptr/mets:area/@FILEID", Filters.attribute(), null, NS_LIST);
        FILE_EXPRESSION = XPathFactory.instance().compile(
            "mets:fileSec/mets:fileGrp/mets:fileGrp[@ID='OCRMasterFiles']/mets:file[@ID=$id]/mets:FLocat/@xlink:href",
            Filters.attribute(), vars, NS_LIST);
    }

    private Document mets;

    private MCRDerivate derivate;

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
            derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
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
                volume.addChild(buildVolume(div, div.getAttributeValue("LABEL")));
            } else if (type.equals("volumeparts")) {
                volume.addChild(buildVolume(div, "Volume Parts"));
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
                handleArticleOrder(div, article);
                handleDerivateLink(div, article);
                volume.addChild(article);
            } else if (type.equals("heading")) {
                lastHeading = div.getAttributeValue("LABEL");
            }
        }
    }

    private JPVolume buildVolume(Element logicalDiv, String defaultTitle) throws ConvertException {
        JPVolume volume = new JPVolume();
        MCRObjectID volumeId = volume.getObject().getId();
        // title
        volume.setTitle(defaultTitle);
        // order
        String order = logicalDiv.getAttributeValue("ORDER");
        if (order != null) {
            volume.setHiddenPosition(Integer.valueOf(order));
        } else {
            String msg = volumeId + ": ORDER attribute of logical div " + logicalDiv.getAttributeValue("ID")
                + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // recursive calls for children
        handleLogicalDivs(logicalDiv, volume);
        return volume;
    }

    private JPArticle buildArticle(Element logicalDiv) throws ConvertException {
        String dmdId = LLZMetsUtils.getDmDId(logicalDiv);
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
            String msg = articleId + ": Missing mods:recordInfo/mods:recordOrigin of logical div " + logicalId;
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // participants - we only create participants which have a gnd id
        for (Element name : mods.getChildren("name", MCRConstants.MODS_NAMESPACE)) {
            try {
                // get or create person
                MCRObjectID participantId = LLZMetsUtils.getOrCreatePerson(name);
                if (participantId == null) {
                    continue;
                }
                // link with article
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
        handleArticleOrder(logicalDiv, article);
        // heading
        if (lastHeading != null) {
            try {
                /* we disable heading cause the ocr text is so bad that we would
                   create a bunch of unnecessary categories */
                // article.addHeading(lastHeading);
            } catch (Exception exc) {
                String msg = articleId + ": Unable to set heading of logical div " + logicalId;
                LOGGER.error(msg, exc);
                getErrorList().add(msg);
            }
        }
        // id's
        List<Element> identifiers = mods.getChildren("identifier", MCRConstants.MODS_NAMESPACE);
        for (Element identifier : identifiers) {
            String type = identifier.getAttributeValue("type").toLowerCase();
            type = type.equals("gbv") ? "ppn" : type;
            String id = identifier.getTextNormalize();
            if(id.startsWith("(")) {
                // don't store the queries
                article.setIdenti(type, id.substring(6));
            }
        }
        // derivate link
        handleDerivateLink(logicalDiv, article);
        return article;
    }

    private void handleArticleOrder(Element logicalDiv, JPArticle article) {
        String order = logicalDiv.getAttributeValue("ORDER");
        if (order != null) {
            article.setSize(Integer.valueOf(order));
        } else {
            String msg = article.getObject().getId() + ": ORDER attribute of logical div "
                + logicalDiv.getAttributeValue("ID") + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
    }

    private void handleDerivateLink(Element logicalDiv, JPArticle article) {
        String logicalId = logicalDiv.getAttributeValue("ID");
        MCRObjectID articleId = article.getObject().getId();
        Attribute fileIdAttr = FILEID_EXPRESSION.evaluateFirst(logicalDiv);
        if (fileIdAttr != null) {
            String fileId = fileIdAttr.getValue().replaceAll("-INDEXALTO", "-OCRMASTER");
            FILE_EXPRESSION.setVariable("id", fileId);
            Attribute hrefAttr = FILE_EXPRESSION.evaluateFirst(mets.getRootElement());
            if (hrefAttr != null) {
                String href = hrefAttr.getValue();
                try {
                    String newHref = new TiffHrefStrategy().get(href);
                    MCRPath path = MCRPath.getPath(derivate.getId().toString(), newHref);
                    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                        article.setDerivateLink(derivate, newHref);
                    } else {
                        String msg = articleId + ": There is no image " + newHref + " in this derivate.";
                        getErrorList().add(msg);
                        LOGGER.warn(msg);
                    }
                } catch (Exception exc) {
                    String msg = articleId + ": Unable to add derivate link " + href + " of logical div " + logicalId;
                    getErrorList().add(msg);
                    LOGGER.error(msg, exc);
                }
            }
        }
    }

    public List<String> getErrorList() {
        return errorList;
    }

}
