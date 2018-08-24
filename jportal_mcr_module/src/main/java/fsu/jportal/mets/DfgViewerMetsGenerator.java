package fsu.jportal.mets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.Mptr;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.tools.MCRMetsSave;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.MetsUtil;

/**
 * DFG Viewer implementation of a mets generator using this
 * <a href="https://dfg-viewer.de/fileadmin/groups/dfgviewer/METS-Anwendungsprofil_2.3.pdf">profile</a>.
 *
 * @author Matthias Eichner
 */
public class DfgViewerMetsGenerator extends JPMetsHierarchyGenerator {

    protected static Map<String, String> GROUP_TO_ZOOM_LEVEL_MAP;

    static {
        GROUP_TO_ZOOM_LEVEL_MAP = new HashMap<>();
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_MIN, "MIN");
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_DEFAULT, "MID");
    }

    protected JPPeriodicalComponent rootPeriodical;

    protected JPDerivateComponent derivate;

    protected List<FileGrp> fileGroups;

    protected HashMap<MCRObjectID, JPPeriodicalComponent> periodicalMap;

    @Override
    public void setup(String derivateId) {
        super.setup(derivateId);
        this.rootPeriodical = JPComponentUtil.getPeriodical(this.rootObj);
        this.derivate = new JPDerivateComponent(this.mcrDer);
        this.periodicalMap = new LinkedHashMap<>();
        // derivate path & containing mets
        MCRPath derivatePath = this.derivate.getPath();
        this.setDerivatePath(derivatePath);
        if (Files.exists(derivatePath.resolve("mets.xml"))) {
            try {
                Mets mets = MetsUtil.getMets(derivateId);
                this.setOldMets(mets);
            } catch (Exception exc) {
                LogManager.getLogger().error("Unable to parse mets.xml", exc);
            }
        }
    }

    @Override
    protected DmdSec createDmdSection() {
        String dmdSec = "dmd_" + this.rootPeriodical.getId();
        return new DmdSec(dmdSec);
    }

    @Override
    protected FileSec createFileSection() {
        final FileSec fileSec = new FileSec();
        try {
            // get paths
            ArrayList<MCRPath> ignorePaths = new ArrayList<>();
            MCRPath derivatePath = derivate.getPath();
            ignorePaths.add(MCRPath.toMCRPath(derivatePath.resolve("mets.xml")));
            ignorePaths.add(MCRPath.toMCRPath(derivatePath.resolve("dfgViewerMets.xml")));
            ignorePaths.add(MCRPath.toMCRPath(derivatePath.resolve("zvddMets.xml")));
            List<MCRPath> filePaths = MCRMetsSave.listFiles(derivatePath, ignorePaths);

            // build file refs
            for (MCRPath file : filePaths) {
                String contentType = MCRContentTypes.probeContentType(file);
                if (contentType.startsWith("image/")) {
                    // we always using image/jpeg cause we deliver with the MCRTileCombineServlet
                    FileRef ref = buildFileRef(file, "image/jpeg");
                    this.files.add(ref);
                }
            }

            // build groups
            Collection<String> groupIds = GROUP_TO_ZOOM_LEVEL_MAP.keySet();
            this.fileGroups = groupIds.stream().map(FileGrp::new).collect(Collectors.toList());
            this.fileGroups.forEach(fileSec::addFileGrp);

            // add to file sec
            files.forEach(fileRef -> this.fileGroups.forEach(group -> {
                try {
                    File file = new File(fileRef.toFileId(group), fileRef.getContentType());
                    String href = fileRef.toFileHref(group);
                    FLocat fLocat = new FLocat(LOCTYPE.URL, href);
                    file.setFLocat(fLocat);
                    group.addFile(file);
                } catch (Exception uriExc) {
                    LogManager.getLogger().error("Unable to resolve path " + fileRef.getPath(), uriExc);
                }
            }));

        } catch (IOException ioExc) {
            throw new MCRException("Unable to generate mets.xml because cannot read files.", ioExc);
        }
        return fileSec;
    }

    @Override
    protected PhysicalStructMap createPhysicalStruct() {
        final PhysicalStructMap struct = new PhysicalStructMap();
        final PhysicalDiv physicalDiv = new PhysicalDiv();
        struct.setDivContainer(physicalDiv);

        // add file refs
        AtomicInteger order = new AtomicInteger(1);
        this.files.forEach(file -> {
            final String orderLabel = getOrderLabelOfPhysicalId(file.toPhysId());
            final PhysicalSubDiv div = new PhysicalSubDiv(file.toPhysId(), PhysicalSubDiv.TYPE_PAGE, orderLabel);
            div.setOrder(order.getAndIncrement());
            this.fileGroups.forEach(group -> {
                Fptr fptr = new Fptr(file.toFileId(group));
                div.add(fptr);
            });
            physicalDiv.add(div);
        });
        return struct;
    }

    protected String getOrderLabelOfPhysicalId(String physId) {
        return getOldMets().map(mets -> {
            PhysicalDiv divContainer = mets.getPhysicalStructMap().getDivContainer();
            return divContainer.get(physId).getOrderLabel();
        }).orElse(null);
    }

    @Override
    protected LogicalStructMap createLogicalStruct() {
        final LogicalStructMap struct = new LogicalStructMap();

        // journal div & pointer
        JPJournal journal = rootPeriodical.getJournal();
        LogicalDiv journalDiv = new LogicalDiv("log_" + journal.getId().toString(), "periodical", journal.getTitle());
        struct.setDivContainer(journalDiv);

        Mptr mptr = getMptr(journal);
        journalDiv.setMptr(mptr);

        // volume itself
        LogicalDiv volumeDiv = new LogicalDiv("log_" + rootPeriodical.getId().toString(), "volume",
            rootPeriodical.getTitle());
        volumeDiv.setDmdId("dmd_" + rootPeriodical.getId());
        volumeDiv.setAmdId("amd_" + rootPeriodical.getId());
        journalDiv.add(volumeDiv);

        // struct link
        updateStructLinkMapUsingDerivateLinks(volumeDiv, rootPeriodical.getObject(), this.fileGroups.get(0));

        // hierarchy
        handleLogicalHierarchy(volumeDiv);

        // return
        return struct;
    }

    protected void handleLogicalHierarchy(LogicalDiv volumeDiv) {
        if (rootPeriodical instanceof JPContainer) {
            ((JPContainer) rootPeriodical).getChildren()
                .forEach(childId -> buildHierarchy(childId, volumeDiv, false));
        }
    }

    protected Mptr getMptr(JPJournal journal) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/dfg/" + journal.getId().toString();
        return new Mptr(href, LOCTYPE.URL);
    }

    protected void buildHierarchy(final MCRObjectID id, final LogicalDiv parentDiv, boolean addDmdId) {
        getPeriodical(id).ifPresent(periodical -> {
            // add div
            String type = JPArticle.TYPE.equals(periodical.getType()) ? "article" : "issue";
            LogicalDiv childDiv = new LogicalDiv("log_" + id, type, periodical.getTitle());
            if (addDmdId) {
                childDiv.setDmdId("dmd_" + id);
            }
            parentDiv.add(childDiv);

            // struct link
            updateStructLinkMapUsingDerivateLinks(childDiv, periodical.getObject(), this.fileGroups.get(0));

            // recursive children
            if (JPVolume.TYPE.equals(periodical.getType())) {
                JPVolume volume = (JPVolume) periodical;
                volume.getChildren().forEach(childId -> buildHierarchy(childId, childDiv, addDmdId));
            }
        });
    }

    @Override
    protected FileRef buildFileRef(MCRPath path, String contentType) {
        return new DfgFileRef(path, contentType);
    }

    protected Optional<JPPeriodicalComponent> getPeriodical(MCRObjectID id) {
        JPPeriodicalComponent periodical = this.periodicalMap.get(id);
        if (periodical != null) {
            return Optional.of(periodical);
        }
        Optional<JPPeriodicalComponent> periodicalOptional = JPComponentUtil.getPeriodical(id);
        periodicalOptional.ifPresent(jpPeriodicalComponent -> this.periodicalMap.put(id, jpPeriodicalComponent));
        return periodicalOptional;
    }

    protected static class DfgFileRef implements FileRef {

        private MCRPath path;

        private String contentType;

        DfgFileRef(MCRPath path, String contentType) {
            this.path = path;
            this.contentType = contentType;
        }

        @Override
        public String toFileId(FileGrp fileGrp) {
            return fileGrp.getUse() + "_" + MCRMetsSave.getFileBase(path);
        }

        @Override
        public String toFileHref(FileGrp fileGrp) {
            String path = getPath().getOwnerRelativePath().substring(1);
            try {
                String imagePath = MCRXMLFunctions.encodeURIPath(path, true);
                String zoomLevel = GROUP_TO_ZOOM_LEVEL_MAP.get(fileGrp.getUse());
                return MCRFrontendUtil.getBaseURL() + "servlets/MCRTileCombineServlet/"
                    + zoomLevel + "/" + getPath().getOwner() + "/" + imagePath;
            } catch (URISyntaxException exc) {
                throw new MCRException("Unable to encode " + path, exc);
            }
        }

        public String toPhysId() {
            return PhysicalSubDiv.ID_PREFIX + MCRMetsSave.getFileBase(path);
        }

        public MCRPath getPath() {
            return path;
        }

        public String getContentType() {
            return contentType;
        }

    }

}
