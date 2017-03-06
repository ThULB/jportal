package fsu.jportal.backend.event;

import java.util.List;
import java.util.stream.Collectors;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.common.inject.MCRInjectorConfig;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

import fsu.jportal.mets.MetsAutoGenerator;
import fsu.jportal.util.DerivateLinkUtil;

/**
 * Checks if it is required to rebuild the corresponding mets.xml. Uses
 * the {@link MetsAutoGenerator} to do so.
 * 
 * @author Matthias Eichner
 */
public class UpdateMetsHandler extends MCREventHandlerBase {

    private MetsAutoGenerator metsAutoGenerator;

    public UpdateMetsHandler() {
        this.metsAutoGenerator = MCRInjectorConfig.injector().getInstance(MetsAutoGenerator.class);
    }

    @Override
    protected void handleObjectUpdated(MCREvent evt, MCRObject obj) {
        MCRObjectID mcrId = obj.getId();
        String type = mcrId.getTypeId();
        // apply only for journal, volume and article
        if (!(type.equals("jpjournal") || type.equals("jparticle") || type.equals("jpvolume"))) {
            return;
        }

        // fetch all derivates
        List<String> derivateLinks = getDerivateLinks(obj);

        // run through derivates
        for (String derivateID : derivateLinks) {
            metsAutoGenerator.add(MCRObjectID.getInstance(derivateID));
        }
    }

    @Override
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        metsAutoGenerator.remove(der.getId());
    }

    /**
     * Returns a list of derivates which are in relation to the given object. This includes
     * all derivates of the structure part and all derivate links.
     * 
     * @param obj mycore object
     * @return list of derivate id's
     */
    private List<String> getDerivateLinks(MCRObject obj) {
        // first derivate links
        List<String> links = DerivateLinkUtil.getLinkedDerivates(obj);
        if (!links.isEmpty()) {
            return links;
        }
        // run through the derivates of ancestors and self
        List<MCRObject> ancestorsAndSelf = MCRObjectUtils.getAncestorsAndSelf(obj);
        // return list of all derivate Links
        return ancestorsAndSelf.stream()
                               .flatMap(o -> o.getStructure().getDerivates().stream())
                               .map(MCRMetaLinkID::getXLinkHref)
                               .distinct()
                               .collect(Collectors.toList());
    }

}
