package fsu.jportal.mets;

import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectUtils;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.IStructMap;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;

import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This implementation is used for all issues in newspapers.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddNewspaperIssueMetsGenerator implements MCRMETSGenerator {

    protected List<MCRMETSHierarchyGenerator.FileRef> files;

    protected List<FileGrp> fileGroups;

    protected JPVolume volume;

    protected JPDerivateComponent derivate;

    public ZvddNewspaperIssueMetsGenerator(JPVolume volume) {
        this.volume = volume;
    }

    @Override
    public Mets generate() throws MCRException {
        // get derivate
        this.derivate = getDerivate(this.volume);
        if (derivate == null) {
            throw new MCRException("Unable to locate a derivate in the object '" + this.volume.getId() + "' or its"
                + " ancestors. The creating of the issue is not possible.");
        }
        // get files and build file groups
        this.files = getFiles();
        Collection<String> groupIds = DfgViewerFileRef.GROUP_TO_ZOOM_LEVEL_MAP.keySet();
        this.fileGroups = groupIds.stream().map(FileGrp::new).collect(Collectors.toList());

        // create mets
        Mets mets = new Mets();
        mets.addDmdSec(ZvddMetsTools.createDmdSec(volume, "issue"));
        mets.setFileSec(createFileSec());
        mets.addStructMap(ZvddMetsTools.createPhysicalStructMap(files, fileGroups, null));
        mets.addStructMap(createLogicalStructMap());
        return mets;
    }

    protected FileSec createFileSec() {
        FileSec fileSec = new FileSec();
        this.fileGroups.forEach(fileSec::addFileGrp);
        ZvddMetsTools.addFilesToGroups(this.files, this.fileGroups);
        return fileSec;
    }

    protected List<MCRMETSHierarchyGenerator.FileRef> getFiles() {
        try {
            Mets mets = MetsUtil.getMets(derivate.getId().toString());
            List<JPPeriodicalComponent> descendantsAndSelf = MCRObjectUtils
                .getDescendantsAndSelf(volume.getObject())
                .stream()
                .map(JPComponentUtil::getPeriodical)
                .collect(Collectors.toList());

            List<PhysicalSubDiv> physicalIds = getPhysicalIds(descendantsAndSelf, mets);
            return physicalIds.stream().map(div -> toFileRef(div, mets, derivate)).collect(Collectors.toList());
        } catch (Exception exc) {
            throw new MCRException("Unable to get files of " + this.volume, exc);
        }
    }

    /**
     * Tries to get the first derivate with a mets.xml of the component itself or its ancestors.
     * 
     * @param component the component to look for the derivate
     * @return the derivate component or null
     */
    protected JPDerivateComponent getDerivate(JPPeriodicalComponent component) {
        if (component == null) {
            return null;
        }
        List<JPDerivateComponent> derivates = component.getDerivates();
        if (!derivates.isEmpty()) {
            for (JPDerivateComponent derivate : derivates) {
                if (Files.exists(derivate.getPath().resolve("mets.xml"))) {
                    return derivate;
                }
            }
        }
        return getDerivate(component.getParent().orElse(null));
    }

    /**
     * Returns all physical sub divs linked with the given components.
     * 
     * @param components list of mycore objects
     * @param mets the mets to find the data
     * @return list of physical sub divs
     */
    protected List<PhysicalSubDiv> getPhysicalIds(List<JPPeriodicalComponent> components, Mets mets) {
        // fill physical sub div ids
        Set<String> physicalSubDivIds = new LinkedHashSet<>();
        for (JPPeriodicalComponent component : components) {
            List<SmLink> links = mets.getStructLink().getSmLinkByFrom(component.getId().toString());
            for (SmLink link : links) {
                physicalSubDivIds.add(link.getTo());
            }
        }
        // get physical sub divs
        PhysicalDiv divContainer = mets.getPhysicalStructMap().getDivContainer();
        return physicalSubDivIds.stream().map(divContainer::get).collect(Collectors.toList());
    }

    protected LogicalStructMap createLogicalStructMap() {
        LogicalStructMap struct = new LogicalStructMap();


        return struct;
    }

    /**
     * Converts the given subDiv to FileRef object.
     * 
     * @param subDiv the subdiv to handle
     * @param mets the mets
     * @param derivate the derivate
     * @return a new file ref object or null
     */
    protected MCRMETSHierarchyGenerator.FileRef toFileRef(PhysicalSubDiv subDiv, Mets mets,
        JPDerivateComponent derivate) {
        FileGrp masterGroup = mets.getFileSec().getFileGroup(FileGrp.USE_MASTER);
        List<Fptr> fptrCanidates = subDiv.getChildren();
        for (Fptr fptr : fptrCanidates) {
            File file = masterGroup.getFileById(fptr.getFileId());
            if (file == null) {
                continue;
            }
            MCRPath path = MCRPath.toMCRPath(derivate.getPath().resolve(file.getFLocat().getHref()));
            String contentType = file.getMimeType();
            return toFileref(path, contentType);
        }
        return null;
    }

    protected MCRMETSHierarchyGenerator.FileRef toFileref(MCRPath path, String contentType) {
        return new DfgViewerFileRef(path, contentType);
    }

}
