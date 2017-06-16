package fsu.jportal.mets;


import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPVolume;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Does the import for a Perthes mets.xml file. Creates articles for the given issue.
 *
 * @author Matthias Eichner
 */
public class PerthesMetsImporter extends MetsImporter {

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
            handleArticles(mets, derivate, structMap.getDivContainer(), volume, divMap);

            // import to mycore system
            volume.store();
            return divMap;
        } catch (Exception exc) {
            throw new MetsImportException("Unable to import component", exc);
        }
    }

    private void handleArticles(Mets mets, MCRDerivate derivate, LogicalDiv logicalVolume, JPVolume volume,
                              Map<LogicalDiv, JPComponent> divMap) {
        logicalVolume.getChildren().forEach(logicalArticle -> {
            JPArticle article = new JPArticle();
            article.setTitle(logicalArticle.getLabel());
            article.setSize(MetsImportUtils.getPageNumber(mets, logicalArticle) + 1);
            handleDerivateLink(mets, derivate, logicalArticle, article);
            volume.addChild(article);
            divMap.put(logicalArticle, article);
        });
    }

}
