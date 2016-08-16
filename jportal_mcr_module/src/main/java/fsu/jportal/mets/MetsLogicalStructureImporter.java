package fsu.jportal.mets;

import java.util.HashMap;
import java.util.List;
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
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.SmLink;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPComponent;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;

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
                LOGGER.error("Unable to retrieve " + ownerId);
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
        if (type.toLowerCase().equals("volume")) {
            return createVolume(mets, derivate, div, parent, divMap);
        } else if (type.toLowerCase().equals("article")) {
            return createArticle(mets, derivate, div, parent, divMap);
        } else {
            LOGGER.warn("Unable to import " + div.getId() + " because its type (" + type
                + ") does not match volume or article.");
        }
        return null;
    }

    private JPArticle createArticle(Mets mets, MCRDerivate derivate, LogicalDiv div, JPContainer parent,
        Map<LogicalDiv, JPComponent> divMap) {
        JPArticle article = new JPArticle();
        article.setTitle(div.getLabel());
        article.setSize(getPageNumber(mets, div));
        handleDerivateLink(mets, derivate, div, article);
        return article;
    }

    private JPVolume createVolume(Mets mets, MCRDerivate derivate, LogicalDiv div, JPContainer parent,
        Map<LogicalDiv, JPComponent> divMap) {
        JPVolume volume = new JPVolume();
        String label = div.getLabel();
        volume.setTitle(label);
        // add published date if possible
        if (MetsImportUtils.MONTH_NAMES.containsValue(label)) {
            Integer monthIndex = MetsImportUtils.MONTH_NAMES.inverse().get(label);
            MetsImportUtils.setPublishedDate(monthIndex, volume, parent);
        }
        volume.setHiddenPosition(String.format("%04d", Integer.valueOf(div.getPositionInParent().orElse(0))));
        divMap.put(div, volume);
        div.getChildren().forEach(childDiv -> {
            JPPeriodicalComponent childComponent = handle(mets, derivate, childDiv, volume, divMap);
            if (childComponent != null) {
                volume.addChild(childComponent);
            }
        });
        return volume;
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
