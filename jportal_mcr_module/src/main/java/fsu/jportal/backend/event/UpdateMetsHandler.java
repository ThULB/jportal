package fsu.jportal.backend.event;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.stream.Collectors;

import org.mycore.common.events.MCREvent;
import org.mycore.common.events.MCREventHandlerBase;
import org.mycore.common.inject.MCRInjectorConfig;
import org.mycore.datamodel.common.MCRMarkManager;
import org.mycore.datamodel.metadata.*;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.tools.MCRMetsSave;

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
        handleObject(obj);
    }

    @Override
    protected void handleObjectRepaired(MCREvent evt, MCRObject obj) {
        handleObject(obj);
    }

    @Override
    protected void handleDerivateDeleted(MCREvent evt, MCRDerivate der) {
        metsAutoGenerator.remove(der.getId());
    }

    @Override
    protected void handleDerivateUpdated(MCREvent evt, MCRDerivate der) {
        handleDerivate(der);
    }

    @Override
    protected void handlePathDeleted(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePath(path);
    }

    @Override
    protected void handlePathCreated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePath(path);
    }

    @Override
    protected void handlePathRepaired(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePath(path);
    }

    @Override
    protected void handlePathUpdated(MCREvent evt, Path path, BasicFileAttributes attrs) {
        handlePath(path);
    }

    private void handleObject(MCRObject object) {
        MCRObjectID mcrId = object.getId();
        String type = mcrId.getTypeId();
        // apply only for journal, volume and article
        if (!(type.equals("jpjournal") || type.equals("jparticle") || type.equals("jpvolume"))) {
            return;
        }

        // fetch all derivates
        getDerivateLinks(object).stream()
            .map(MCRObjectID::getInstance)
            .filter(der -> !MCRMarkManager.instance().isMarkedForDeletion(der))
            .distinct()
            .forEach(metsAutoGenerator::add);
    }

    private void handleDerivate(MCRDerivate derivate) {
        MCRMetsSave.updateMetsOnUrnGenerate(derivate);
    }

    /**
     * Updates the mets.xml if a file change appear. If the mets.xml itself was
     * changed, then we remove the derivate from auto generation, cause we expect
     * the right mets.xml in the first place.
     *
     * @param path the path which is updated/repaired/removed
     */
    private void handlePath(Path path) {
        MCRPath mcrPath = MCRPath.toMCRPath(path);
        if (!MCRObjectID.isValid(mcrPath.getOwner())) {
            return;
        }
        MCRObjectID derivateId = MCRObjectID.getInstance(mcrPath.getOwner());
        Path fileNamePath = mcrPath.getFileName();
        if (fileNamePath == null) {
            return;
        }
        String fileName = fileNamePath.toString();
        if ("mets.xml".equals(fileName)) {
            metsAutoGenerator.remove(derivateId);
        } else {
            metsAutoGenerator.add(derivateId);
        }
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
        return ancestorsAndSelf.stream().flatMap(o -> o.getStructure().getDerivates().stream())
            .map(MCRMetaLinkID::getXLinkHref).distinct().collect(Collectors.toList());
    }

}
