package fsu.jportal.mets;

import java.util.HashMap;
import java.util.Map;

import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.mcr.MetadataManager;

/**
 * Default importer for the MCRPROFILE mets.
 *
 * @author Matthias Eichner
 */
public class MCRMetsImporter extends MetsImporter {

    @Override
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException {
        try {
            // get corresponding mycore objects
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID volumeId = derivate.getOwnerID();
            MCRObject mcrObject = MetadataManager.retrieveMCRObject(volumeId);
            JPVolume volume = new JPVolume(mcrObject);

            // run through mets
            Map<LogicalDiv, JPComponent> divMap = new HashMap<>();
            LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);
            handle(mets, derivate, structMap.getDivContainer(), volume, divMap);

            // import to mycore system
            volume.store();
            return divMap;
        } catch (Exception exc) {
            throw new MetsImportException("Unable to import component", exc);
        }
    }

    protected void handle(Mets mets, MCRDerivate derivate, LogicalDiv div, JPVolume volume,
        Map<LogicalDiv, JPComponent> divMap) {
        div.getChildren().forEach(childDiv -> {
            boolean isArticle = childDiv.getChildren().isEmpty();
            if (isArticle) {
                handleArticle(mets, derivate, volume, divMap, childDiv);
            } else {
                handleIssue(mets, derivate, volume, divMap, childDiv);
            }
        });
    }

    protected void handleIssue(Mets mets, MCRDerivate derivate, JPVolume volume, Map<LogicalDiv, JPComponent> divMap,
        LogicalDiv logicalIssue) {
        JPVolume issue = new JPVolume();
        issue.setTitle(logicalIssue.getLabel());
        issue.setHiddenPosition(logicalIssue.getPositionInParent().orElse(0));
        volume.addChild(issue);
        divMap.put(logicalIssue, issue);
        handle(mets, derivate, logicalIssue, issue, divMap);
    }

    protected void handleArticle(Mets mets, MCRDerivate derivate, JPVolume issue, Map<LogicalDiv, JPComponent> divMap,
        LogicalDiv logicalArticle) {
        JPArticle article = new JPArticle();
        article.setTitle(logicalArticle.getLabel());
        article.setSize(MetsImportUtils.getPageNumber(mets, logicalArticle) + 1);
        handleDerivateLink(mets, derivate, logicalArticle, article);
        issue.addChild(article);
        divMap.put(logicalArticle, article);
    }

}
