package fsu.jportal.mets;

import static fsu.jportal.frontend.SolrToc.buildQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Uses the mycore mets hierarchy generator and adds the support for ALTO files. This includes
 * adding alto files to the fileSec and physical struct map.
 * <p>
 * An additional feature is coping ALTO block references from an "old"
 * mets to the newly generated one.
 * </p>
 *
 * @author Matthias Eichner
 */
public class JPMetsHierarchyGenerator extends MCRMETSHierarchyGenerator {

    protected Map<String, String> sizeMap;

    @Override
    protected void setup(String derivateId) {
        super.setup(derivateId);
        this.sizeMap = new HashMap<>();
    }

    @Override
    public synchronized Mets generate() throws MCRException {
        Mets mets = super.generate();
        enhanceOrderLabels(mets);
        return mets;
    }

    protected String getType(MCRObject obj) {
        return obj.getId().getTypeId().substring(2);
    }

    protected String getLabel(MCRObject obj) {
        Optional<MCRMetaLangText> maintitle = obj.getMetadata().findFirst("maintitles");
        return maintitle.map(MCRMetaLangText::getText).orElse("no title for " + obj.getId());
    }

    protected List<MCRObject> getChildren(MCRObject parentObject) {
        if (parentObject.getId().getTypeId().equals("jparticle")) {
            return Collections.emptyList();
        }
        List<MCRObject> children = new ArrayList<>();
        getChildren(parentObject, "jpvolume").stream()
                                             .map(MCRMetadataManager::retrieveMCRObject)
                                             .forEach(children::add);
        getChildren(parentObject, "jparticle").stream()
                                              .map(MCRMetadataManager::retrieveMCRObject)
                                              .forEach(children::add);
        return children;
    }

    protected List<MCRObjectID> getChildren(MCRObject parentObject, String objectType) {
        String parentID = parentObject.getId().toString();
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, "order asc");
        solrParams.set("fl", "id objectType");
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        return MCRSolrSearchUtils.stream(solrClient, solrParams).map(doc -> {
            String id = (String) doc.getFieldValue("id");
            return MCRObjectID.getInstance(id);
        }).collect(Collectors.toList());
    }

    @Override
    protected void newLogicalStructMap(MCRObject parentObject, LogicalDiv parentLogicalDiv) {
        super.newLogicalStructMap(parentObject, parentLogicalDiv);
        buildSizeMap(parentObject, parentLogicalDiv);
    }

    /**
     * In jportal we use the "sizes" element in an article to set up order. We can use this information to enhance
     * the mets.xml's phyisical structMap. We respect existing @ORDERLABEL's and do not overwrite them.
     *
     * <p>This method is responsible to gather the size information while building the logical struct map. We cannot add
     * the ORDERLABEL yet, cause the link between the physical structure and the logical structure is not build. But at
     * this point we have access to the mcrObject, which will be lost later. So we build an id/size map here for later
     * usage.</p>
     *
     * @param mcrObject the object to get the size information
     * @param logicalDiv the logical div
     */
    private void buildSizeMap(MCRObject mcrObject, LogicalDiv logicalDiv) {
        if (!JPComponentUtil.is(mcrObject.getId(), JPObjectType.jparticle)) {
            return;
        }
        JPComponentUtil.get(mcrObject.getId(), JPArticle.class)
                       .flatMap(JPArticle::getSize)
                       .ifPresent(size -> this.sizeMap.put(logicalDiv.getId(), size));
    }

    /**
     * Uses the sizeMap to enhance the @ORDERLABEL's of the physical struct map. See {@link #buildSizeMap}.
     *
     * @param mets the mets to enhance
     */
    private void enhanceOrderLabels(Mets mets) {
        // put the sizes for each available physical div
        this.sizeMap.forEach((logicalDivId, size) -> {
            List<SmLink> linkList = mets.getStructLink().getSmLinkByFrom(logicalDivId);
            if (linkList.isEmpty()) {
                return;
            }
            SmLink firstLink = linkList.get(0);
            String physicalDivId = firstLink.getTo();
            PhysicalSubDiv physicalDiv = mets.getPhysicalStructMap().getDivContainer().get(physicalDivId);
            if (physicalDiv.getOrderLabel() != null && !"".equals(physicalDiv.getOrderLabel().trim())) {
                return;
            }
            try {
                size = size.split("-")[0].trim();
                physicalDiv.setOrderLabel(size);
            } catch (Exception exc) {
                LogManager.getLogger().warn("Unable to set ORDERLABEL=" + size + " to " + physicalDivId);
            }
        });
        // try to interpolate between the physical div's who has no ORDERLABEL's yet
        interpolateOrderLabels(mets);
    }

    protected void interpolateOrderLabels(Mets mets) {
        PhysicalStructMap physicalStructMap = mets.getPhysicalStructMap();
        PhysicalDiv divContainer = physicalStructMap.getDivContainer();
        String lastOrderLabel = null;
        int count = 0;
        for(PhysicalSubDiv div : divContainer.getChildren()) {
            // respect existing orderlabel's
            if(div.getOrderLabel() != null && !"".equals(div.getOrderLabel())) {
                lastOrderLabel = div.getOrderLabel();
                count = 0;
                continue;
            }
            if(lastOrderLabel == null) {
                continue;
            }
            count++;
            // order label is null or unset -> try to interpolate from last one
            String newOrderLabel = interpolateOrderLabel(lastOrderLabel, count);
            if(newOrderLabel != null) {
                div.setOrderLabel(newOrderLabel);
            }
        }
    }

    protected String interpolateOrderLabel(String baseOrderLabel, int count) {
        try {
            // normal numbers
            Integer baseInteger = Integer.valueOf(baseOrderLabel);
            return String.valueOf(baseInteger + count);
        } catch(Exception exc) {
            try {
                // recto verso
                if (baseOrderLabel.contains("v")) {
                    String base = baseOrderLabel.replace("v", "");
                    Integer number = Integer.valueOf(base);
                    return count % 2 == 0 ? number + (count / 2) + "v" : number + ((count - 1) / 2) + "r";
                } else if (baseOrderLabel.contains("r")) {
                    String base = baseOrderLabel.replace("r", "");
                    Integer number = Integer.valueOf(base);
                    return count % 2 == 0 ? (number + count / 2) + "r" : number + (count / 2 + 1) + "v";
                }
            } catch(Exception exc2) {
                // do not handle -> just return null
            }
        }
        return null;
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }

}
