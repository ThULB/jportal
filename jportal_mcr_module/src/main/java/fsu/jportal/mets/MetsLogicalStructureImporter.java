package fsu.jportal.mets;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import static fsu.jportal.util.MetsUtil.MONTH_NAMES;

public class MetsLogicalStructureImporter extends MetsImporter {

    private static Logger LOGGER = LogManager.getLogger(MetsLogicalStructureImporter.class);

    @Override
    public Map<LogicalDiv, JPComponent> importMets(Mets mets, MCRObjectID derivateId) throws MetsImportException {
        try {
            Map<LogicalDiv, JPComponent> divMap = new HashMap<>();

            // get corresponding mycore objects
            MCRDerivate derivate = MCRMetadataManager.retrieveMCRDerivate(derivateId);
            MCRObjectID ownerId = derivate.getOwnerID();
            Optional<JPContainer> optionalContainer = JPComponentUtil.getContainer(ownerId);
            if (!optionalContainer.isPresent()) {
                LOGGER.error("Unable to retrieve {}", ownerId);
                return divMap;
            }
            JPContainer container = optionalContainer.get();

            // run through mets
            LogicalStructMap structMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);

            LogicalDiv divContainer = structMap.getDivContainer();
            divContainer.getChildren().forEach(div -> {
                JPPeriodicalComponent child = handle(mets, derivate, div, container, divMap);
                if (child != null) {
                    container.addChild(child);
                }
            });

            // import to mycore system
            container.store();
            return divMap;
        } catch (Exception exc) {
            throw new MetsImportException("Unable to import component", exc);
        }
    }

    private JPPeriodicalComponent handle(Mets mets, MCRDerivate derivate, LogicalDiv div, JPContainer parent,
        Map<LogicalDiv, JPComponent> divMap) {
        String type = div.getType();
        switch (type.toLowerCase()) {
            case "volume":
                return createVolume(mets, derivate, div, parent, divMap);
            case "article":
                return createArticle(mets, derivate, div, parent, divMap);
            default:
                LOGGER
                    .warn("Unable to import {} because its type ({}) does not match volume or article.", div.getId(),
                        type);
                break;
        }
        return null;
    }

    private JPArticle createArticle(Mets mets, MCRDerivate derivate, LogicalDiv div, JPContainer parent,
        Map<LogicalDiv, JPComponent> divMap) {
        JPArticle article = new JPArticle();
        article.setTitle(div.getLabel());
        article.setSize(MetsImportUtils.getPageNumber(mets, div));
        handleDerivateLink(mets, derivate, div, article);
        return article;
    }

    private JPVolume createVolume(Mets mets, MCRDerivate derivate, LogicalDiv div, JPContainer parent,
        Map<LogicalDiv, JPComponent> divMap) {
        JPVolume volume = new JPVolume();
        String label = div.getLabel();
        volume.setTitle(label);
        // add published date if possible
        if (MONTH_NAMES.containsValue(label)) {
            Integer monthIndex = MONTH_NAMES.inverse().get(label);
            MetsImportUtils.setPublishedDate(monthIndex, volume, parent);
        }
        volume.setHiddenPosition(String.format("%04d", div.getPositionInParent().orElse(0)));
        divMap.put(div, volume);
        div.getChildren().forEach(childDiv -> {
            JPPeriodicalComponent childComponent = handle(mets, derivate, childDiv, volume, divMap);
            if (childComponent != null) {
                volume.addChild(childComponent);
            }
        });
        return volume;
    }

}
