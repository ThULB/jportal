package fsu.jportal.mets;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPVolume;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.struct.*;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.*;

/**
 * LLZ METS importer class.
 *
 * @author Matthias Eichner
 */
public class LLZMetsImporter {

    private static Logger LOGGER = Logger.getLogger(LLZMetsImporter.class);

    private static final ArrayList<Namespace> NS_LIST;

    private static final XPathExpression<Attribute> FILEID_EXPRESSION;


    static {
        NS_LIST = new ArrayList<Namespace>();
        NS_LIST.add(MCRConstants.METS_NAMESPACE);
        NS_LIST.add(MCRConstants.MODS_NAMESPACE);
        NS_LIST.add(MCRConstants.XLINK_NAMESPACE);
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        FILEID_EXPRESSION = XPathFactory.instance().compile(
                "mets:div[@TYPE='physSequence']//mets:div[@ID=$id]/mets:fptr/@FILEID", Filters.attribute(), vars, NS_LIST);
    }

    private Mets mets;

    private MCRDerivate derivate;

    private String lastHeading;

    /**
     * List of errors which are not important enough to break the import process but
     * should be handled by the editor.
     */
    private List<String> errorList;

    /**
     * Imports the dmd section of the given mets document.
     *
     * @param mets METS Object
     * @param derivateId MCR derivate ID
     * @throws ConvertException something went so wrong that the import process has to be stopped
     */
    public void importMets(Mets mets, MCRObjectID derivateId) throws ConvertException {
        // reset values
        this.mets = mets;
        this.lastHeading = null;
        this.errorList = new ArrayList<String>();

        try {
            // get corresponding mycore objects
            derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID volumeId = derivate.getOwnerID();
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(volumeId);
            JPVolume volume = new JPVolume(mcrObject);

            // run through mets
            LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
            handleLogicalDivs(structMap.getDivContainer(), volume);

            // import to mycore system
            volume.importComponent();
        } catch (Exception exc) {
            throw new ConvertException("Unable to import component", exc);
        }
    }

    private void handleLogicalDivs(AbstractLogicalDiv parentDiv, JPVolume volume) throws ConvertException {
        List<LogicalSubDiv> children = parentDiv.getChildren();
        for (LogicalSubDiv div : children) {
            String type = div.getType();
            if (type.equals("issue")) {
                volume.addChild(buildVolume(div, div.getLabel()));
            } else if (type.equals("volumeparts")) {
                volume.addChild(buildVolume(div, "Volume Parts"));
            } else if (type.equals("article")) {
                volume.addChild(buildArticle(div));
            } else if (type.equals("title_page") || type.equals("preface") || type.equals("index")) {
                JPArticle article = new JPArticle();
                String title = type.equals("title_page") ? "Titelblatt" : type.equals("preface") ? "Vorwort" : "Register";
                article.setTitle(title);
                handleArticleOrder(div, article);
                handleDerivateLink(div, article);
                volume.addChild(article);
//            } else if (type.equals("heading")) {
//                lastHeading = div.getAttributeValue("LABEL");
            }
        }
    }

    private JPVolume buildVolume(LogicalSubDiv logicalDiv, String defaultTitle) throws ConvertException {
        JPVolume volume = new JPVolume();
        MCRObjectID volumeId = volume.getObject().getId();
        // title
        volume.setTitle(defaultTitle);
        // order
        int order = logicalDiv.getOrder();
        if (order != 0) {
            volume.setHiddenPosition(order);
        } else {
            String msg = volumeId + ": ORDER attribute of logical div " + logicalDiv.getId()
                    + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // recursive calls for children
        handleLogicalDivs(logicalDiv, volume);
        return volume;
    }

//    private JPArticle buildArticle(LogicalSubDiv logicalDiv) throws ConvertException {
//        String dmdId = dmdIDs.get(logicalDiv.getId());
//
//        if (dmdId != null) {
//            lastDmdID = dmdId;
//        } else if(lastDmdID != null){
//            dmdId = lastDmdID;
//        } else {
//            throw new ConvertException("Cannot create article cause of missing DMDID. ID="
//                    + logicalDiv.getId());
//        }
//
//
//        Element mods = mets.getDmdSecById(dmdId).asElement();
//        if (mods == null) {
//            throw new ConvertException("Could not find referenced dmd entry " + dmdId + " in dmd section.");
//        }
//
//        return buildArticle(mods, logicalDiv, dmdId);
//    }

    /**
     * Creates a new jparticle based on the given mods element.
     *
     * @param logicalDiv the referenced logical div (mets:structMap[@TYPE='logical']/mets:div)
     * @return JPArticle object
     * @throws ConvertException
     */
    protected JPArticle buildArticle(LogicalSubDiv logicalDiv) throws ConvertException {
        String logicalId = logicalDiv.getId();
        JPArticle article = new JPArticle();
        MCRObjectID articleId = article.getObject().getId();
        // title
        String title = logicalDiv.getLabel();
        if (title != null && !title.equals("")) {
            article.setTitle(title);
        } else {
            article.setTitle("undefined");
            String msg = articleId + ": Missing mods:recordInfo/mods:recordOrigin of logical div " + logicalId;
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // participants - we only create participants which have a gnd id
//        for (Element name : mods.getChildren("name", MCRConstants.MODS_NAMESPACE)) {
//            try {
//                // get or create person
//                MCRObjectID participantId = LLZMetsUtils.getOrCreatePerson(name);
//                if (participantId == null) {
//                    continue;
//                }
//                // link with article
//                MCRContent participantContent = MCRXMLMetadataManager.instance().retrieveContent(participantId);
//                MCRContentTransformer transformer = MCRLayoutTransformerFactory.getTransformer("mycoreobject-solr");
//                MCRContent solrContent = transformer.transform(participantContent);
//                Text participantTitle = HEADING_EXPRESSION.evaluateFirst(solrContent.asXML());
//                article.addParticipant(participantId, participantTitle.getText(), "author");
//            } catch (Exception exc) {
//                String msg = articleId + ": Unable to import participant";
//                LOGGER.error(msg, exc);
//                getErrorList().add(msg);
//            }
//        }
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
//        List<Element> identifiers = mods.getChildren("identifier", MCRConstants.MODS_NAMESPACE);
//        for (Element identifier : identifiers) {
//            String attr = identifier.getAttributeValue("type");
//            if (attr != null) {
//                String type = attr.toLowerCase();
//                type = type.equals("gbv") ? "ppn" : type;
//                String id = identifier.getTextNormalize();
//                if (id.startsWith("(")) {
//                    // don't store the queries
//                    article.setIdenti(type, id.substring(6));
//                }
//            }
//        }
        // derivate link
        handleDerivateLink(logicalDiv, article);
        return article;
    }

    private void handleArticleOrder(LogicalSubDiv logicalDiv, JPArticle article) {
        int order = logicalDiv.getOrder();
        if (order != 0) {
            article.setSize(order);
        } else {
            String msg = article.getObject().getId() + ": ORDER attribute of logical div "
                    + logicalDiv.getId() + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
    }

    public void handleDerivateLink(LogicalSubDiv logicalDiv, JPArticle article) {
        String logicalId = logicalDiv.getId();
        MCRObjectID articleId = article.getObject().getId();
        List<SmLink> links = mets.getStructLink().getSmLinkByFrom(logicalId);
        for (SmLink link : links) {
            Element structPhys = mets.getStructMap("PHYSICAL").asElement();
            FILEID_EXPRESSION.setVariable("id", link.getTo());
            List<Attribute> physIDs = FILEID_EXPRESSION.evaluate(structPhys);
            for (Attribute physID : physIDs) {
                if (physID.getValue().endsWith("MASTER")){
                    File file = mets.getFileSec().getFileGroup("MASTER").getFileById(physID.getValue());
                    String newHref = file.getFLocat().getHref();
                    try {
                        MCRPath path = MCRPath.getPath(derivate.getId().toString(), file.getFLocat().getHref());
                        if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                            article.setDerivateLink(derivate, newHref);
                        } else {
                            String msg = articleId + ": There is no image " + newHref + " in this derivate.";
                            getErrorList().add(msg);
                            LOGGER.warn(msg);
                        }
                    } catch (Exception exc) {
                        String msg = articleId + ": Unable to add derivate link " + newHref + " of logical div " + logicalId;
                        getErrorList().add(msg);
                        LOGGER.error(msg, exc);
                    }
                }
            }
        }
    }

    public List<String> getErrorList() {
        return errorList;
    }

}
