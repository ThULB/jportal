package fsu.jportal.mets;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.SmLink;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.util.MetsUtil;

/**
 * Base class for mets importing.
 * 
 * @author Matthias Eichner
 */
public abstract class MetsImporter {

    private static Logger LOGGER = LogManager.getLogger(LLZMetsImporter.class);

    private static final XPathExpression<Attribute> FILEID_EXPRESSION;

    static {
        Map<String, Object> vars = new HashMap<String, Object>();
        vars.put("id", null);
        FILEID_EXPRESSION = XPathFactory.instance().compile(
            "mets:div[@TYPE='physSequence']//mets:div[@ID=$id]/mets:fptr/@FILEID", Filters.attribute(), vars,
            MetsUtil.METS_NS_LIST);
    }

    /**
     * List of errors which are not important enough to break the import process but
     * should be handled by the editor.
     */
    private List<String> errorList;

    /**
     * Does the mets import.
     * 
     * @param mets METS Object
     * @param derivateId MCR derivate ID
     * @throws MetsImportException something went so wrong that the import process has to be stopped
     * @return a map where each logical div is assigned to its imported <code>JPComponent</code>
     */
    public abstract Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId)
        throws MetsImportException;

    /**
     * Adds a derivate link to the article.
     * 
     * @param mets
     * @param derivate
     * @param logicalDiv
     * @param article
     */
    public void handleDerivateLink(Mets mets, MCRDerivate derivate, LogicalDiv logicalDiv, JPArticle article) {
        String logicalId = logicalDiv.getId();
        MCRObjectID articleId = article.getObject().getId();
        List<SmLink> links = mets.getStructLink().getSmLinkByFrom(logicalId);
        for (SmLink link : links) {
            Element structPhys = mets.getStructMap("PHYSICAL").asElement();
            FILEID_EXPRESSION.setVariable("id", link.getTo());
            List<Attribute> physIDs = FILEID_EXPRESSION.evaluate(structPhys);

            physIDs.stream().map(Attribute::getValue).filter(physId -> physId.endsWith("MASTER")).map(physId -> {
                return mets.getFileSec().getFileGroup("MASTER").getFileById(physId).getFLocat().getHref();
            }).forEach(href -> {
                try {
                    MCRPath path = MCRPath.getPath(derivate.getId().toString(), href);
                    if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
                        article.setDerivateLink(derivate, href + "?div=" + article.getObject().getId());
                    } else {
                        String msg = articleId + ": There is no image " + href + " in this derivate.";
                        getErrorList().add(msg);
                        LOGGER.warn(msg);
                    }
                } catch (Exception exc) {
                    String msg = articleId + ": Unable to add derivate link " + href + " of logical div " + logicalId;
                    getErrorList().add(msg);
                    LOGGER.error(msg, exc);
                }
            });
        }
    }

    /**
     * Returns a list of errors which are not important enough to break the import process but
     * should be handled by the editor.
     */
    public List<String> getErrorList() {
        if (this.errorList == null) {
            this.errorList = new ArrayList<>();
        }
        return errorList;
    }

}
