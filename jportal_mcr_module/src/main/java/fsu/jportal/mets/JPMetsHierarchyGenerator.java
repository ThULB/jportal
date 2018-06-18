package fsu.jportal.mets;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;
import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LogicalDiv;

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
        return super.getChildren(parentObject);
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
     * If a physical div already has an @ORDERLABEL the original value is kept and will not be overwritten.
     *
     * @param mets the mets to enhance
     */
    private void enhanceOrderLabels(Mets mets) {
        // put the sizes for each available physical div
        this.sizeMap.forEach((logicalDivId, size) -> {
            try {
                MetsUtil.setOrderlabel(mets, logicalDivId, size, false);
            } catch (Exception exc) {
                LogManager.getLogger().warn("Unable to set ORDERLABEL=" + size + " to " + logicalDivId);
            }
        });
        // try to interpolate between the physical div's who has no ORDERLABEL's yet
        MetsUtil.interpolateOrderLabels(mets);
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }

}
