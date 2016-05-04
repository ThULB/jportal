package fsu.jportal.mets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPObjectComponent;
import fsu.jportal.backend.JPVolume;

/**
 * LLZ METS importer class.
 *
 * @author Matthias Eichner
 */
public class LLZMetsImporter extends MetsImporter {

    private static Logger LOGGER = LogManager.getLogger(LLZMetsImporter.class);

    private Mets mets;

    private MCRDerivate derivate;

    private String lastHeading;

    /**
     * Imports the dmd section of the given mets document.
     *
     * @param mets METS Object
     * @param derivateId MCR derivate ID
     * @throws MetsImportException something went so wrong that the import process has to be stopped
     * 
     * @return a map where each logical div is assigned to its imported <code>JPComponent</code>
     */
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException {
        // reset values
        this.mets = mets;
        this.lastHeading = null;

        try {
            // get corresponding mycore objects
            derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID volumeId = derivate.getOwnerID();
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(volumeId);
            JPVolume volume = new JPVolume(mcrObject);

            // run through mets
            LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
            Map<LogicalDiv, JPComponent> divMap = handleLogicalDivs(structMap.getDivContainer(), volume);

            // import to mycore system
            volume.store();
            return divMap;
        } catch (Exception exc) {
            throw new MetsImportException("Unable to import component", exc);
        }
    }

    private Map<LogicalDiv, JPComponent> handleLogicalDivs(LogicalDiv parentDiv, JPVolume volume)
        throws ConvertException {
        Map<LogicalDiv, JPComponent> divMap = new HashMap<>();
        List<LogicalDiv> children = parentDiv.getChildren();
        for (LogicalDiv div : children) {
            String type = div.getType();
            JPObjectComponent jpComponent = null;
            if (type.equals("issue")) {
                jpComponent = new JPVolume();
                jpComponent.getObject().setImportMode(true);
                divMap.putAll(buildVolume(div, div.getLabel(), (JPVolume) jpComponent));
            } else if (type.equals("volumeparts")) {
                jpComponent = new JPVolume();
                jpComponent.getObject().setImportMode(true);
                divMap.putAll(buildVolume(div, "Volume Parts", (JPVolume) jpComponent));
            } else if (type.equals("article")) {
                jpComponent = buildArticle(div);
            } else if (type.equals("title_page") || type.equals("preface") || type.equals("index")) {
                JPArticle article = new JPArticle();
                article.getObject().setImportMode(true);
                String title = type.equals("title_page") ? "Titelblatt"
                    : type.equals("preface") ? "Vorwort" : "Register";
                article.setTitle(title);
                handleArticleOrder(div, article);
                handleDerivateLink(mets, derivate, div, article);
                jpComponent = article;
            }
            if (jpComponent != null) {
                volume.addChild(jpComponent);
                divMap.put(div, jpComponent);
            }
        }
        return divMap;
    }

    private Map<LogicalDiv, JPComponent> buildVolume(LogicalDiv logicalDiv, String defaultTitle, JPVolume volume)
        throws ConvertException {
        MCRObjectID volumeId = volume.getObject().getId();
        // title
        volume.setTitle(defaultTitle);
        // order
        int order = logicalDiv.getOrder();
        if (order != 0) {
            volume.setHiddenPosition(order);
        } else {
            String msg = volumeId + ": ORDER attribute of logical div " + logicalDiv.getId() + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
        // recursive calls for children
        return handleLogicalDivs(logicalDiv, volume);
    }

    //    private JPArticle buildArticle(LogicalDiv logicalDiv) throws ConvertException {
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
    protected JPArticle buildArticle(LogicalDiv logicalDiv) throws ConvertException {
        String logicalId = logicalDiv.getId();
        JPArticle article = new JPArticle();
        article.getObject().setImportMode(true);
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
        //                // don't store the queries
        //                if (id.startsWith("(gbv) ")) {
        //                    article.setIdenti(type, id.substring(6));
        //                }
        //            }
        //        }
        // derivate link
        handleDerivateLink(mets, derivate, logicalDiv, article);
        return article;
    }

    private void handleArticleOrder(LogicalDiv logicalDiv, JPArticle article) {
        int order = logicalDiv.getOrder();
        if (order != 0) {
            article.setSize(order);
        } else {
            String msg = article.getObject().getId() + ": ORDER attribute of logical div " + logicalDiv.getId()
                + " is not set!";
            LOGGER.warn(msg);
            getErrorList().add(msg);
        }
    }

}
