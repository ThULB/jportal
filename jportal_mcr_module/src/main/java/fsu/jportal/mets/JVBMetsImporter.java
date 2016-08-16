package fsu.jportal.mets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.SmLink;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPVolume;

/**
 * Does the import for a JVB mets.xml file. Creates articles for the given issue.
 * 
 * @author Matthias Eichner
 */
public class JVBMetsImporter extends MetsImporter {

    @Override
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException {
        try {
            // get corresponding mycore objects
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID volumeId = derivate.getOwnerID();
            MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(volumeId);
            JPVolume volume = new JPVolume(mcrObject);

            // run through mets
            Map<LogicalDiv, JPComponent> divMap = new HashMap<>();
            LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
            handleIssues(mets, derivate, structMap.getDivContainer(), volume, divMap);

            // import to mycore system
            volume.store();
            return divMap;
        } catch (Exception exc) {
            throw new MetsImportException("Unable to import component", exc);
        }
    }

    private void handleIssues(Mets mets, MCRDerivate derivate, LogicalDiv logicalVolume, JPVolume volume,
        Map<LogicalDiv, JPComponent> divMap) {
        logicalVolume.getChildren().forEach(logicalIssue -> {
            JPVolume issue = new JPVolume();
            issue.setTitle(logicalIssue.getLabel());
            issue.setHiddenPosition(logicalIssue.getPositionInParent().orElse(0));
            volume.addChild(issue);
            divMap.put(logicalIssue, issue);
            handleArticles(mets, derivate, logicalIssue, issue, divMap);
        });
    }

    private void handleArticles(Mets mets, MCRDerivate derivate, LogicalDiv logicalIssue, JPVolume issue,
        Map<LogicalDiv, JPComponent> divMap) {
        logicalIssue.getChildren().forEach(logicalArticle -> {
            JPArticle article = new JPArticle();
            article.setTitle(logicalArticle.getLabel());
            article.setSize(getPageNumber(mets, logicalArticle));
            issue.addChild(article);
            handleDerivateLink(mets, derivate, logicalArticle, article);
            divMap.put(logicalArticle, article);
        });
    }

    /**
     * Returns the page number of the given logical div.
     * 
     * @param mets
     * @param div
     * @return
     */
    private int getPageNumber(Mets mets, LogicalDiv div) {
        // get physical container
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        PhysicalDiv divContainer = physicalStructMap.getDivContainer();
        // get logical id
        String logicalId = div.getId();
        List<SmLink> links = mets.getStructLink().getSmLinkByFrom(logicalId);
        // map phyisical div's and get the lowest by order
        return links.stream()
                    .map(link -> divContainer.get(link.getTo()).getPositionInParent().orElse(0))
                    .min(Integer::compareTo)
                    .orElse(0);
    }

}
