package fsu.jportal.mets;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.Mptr;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.tools.MCRMetsSave;

import fsu.jportal.backend.JPArticle;
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

    protected JPVolume rootVolume;

    protected JPDerivateComponent derivate;

    protected List<FileGrp> fileGroups;

    protected HashMap<MCRObjectID, JPPeriodicalComponent> periodicalMap;

    @Override
    public void setup(String derivateId) {
        super.setup(derivateId);
        this.rootVolume = new JPVolume(this.rootObj);
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

        // files
        try {
            this.files = getFiles();
        } catch (IOException ioExc) {
            throw new MCRException("Unable to generate mets.xml because cannot read files.", ioExc);
        }

        // file groups
        Collection<String> groupIds = DfgViewerFileRef.GROUP_TO_ZOOM_LEVEL_MAP.keySet();
        this.fileGroups = groupIds.stream().map(FileGrp::new).collect(Collectors.toList());

    }

    @Override
    protected DmdSec createDmdSection() {
        String dmdSec = "dmd_" + this.rootVolume.getId();
        return new DmdSec(dmdSec);
    }

    @Override
    protected FileSec createFileSection() {
        return ZvddMetsTools.createFileSection(this.files, this.fileGroups);
    }

    @Override
    protected PhysicalStructMap createPhysicalStruct() {
        return ZvddMetsTools.createPhysicalStructMap(this.files, this.fileGroups, getOldMets().orElse(null));
    }

    @Override
    protected LogicalStructMap createLogicalStruct() {
        final LogicalStructMap struct = new LogicalStructMap();

        // journal div & pointer
        JPJournal journal = rootVolume.getJournal();
        LogicalDiv journalDiv = new LogicalDiv("log_" + journal.getId().toString(), "periodical", journal.getTitle());
        struct.setDivContainer(journalDiv);

        Mptr mptr = getMptr(journal);
        journalDiv.setMptr(mptr);

        // volume itself
        LogicalDiv volumeDiv = new LogicalDiv("log_" + rootVolume.getId().toString(), "volume",
            rootVolume.getTitle());
        volumeDiv.setDmdId("dmd_" + rootVolume.getId());
        volumeDiv.setAdmId("amd_" + rootVolume.getId());
        journalDiv.add(volumeDiv);

        // struct link
        updateStructLinkMapUsingDerivateLinks(volumeDiv, rootVolume.getObject(), this.fileGroups.get(0));

        // hierarchy
        handleLogicalHierarchy(volumeDiv);

        // return
        return struct;
    }

    protected void handleLogicalHierarchy(LogicalDiv volumeDiv) {
        rootVolume.getChildren().forEach(childId -> buildHierarchy(childId, volumeDiv, false));
    }

    protected Mptr getMptr(JPJournal journal) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/dfg/" + journal.getId().toString();
        return new Mptr(href, LOCTYPE.URL);
    }

    protected List<FileRef> getFiles() throws IOException {
        List<FileRef> files = new ArrayList<>();
        List<MCRPath> ignorePaths = new ArrayList<>();
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
                files.add(ref);
            }
        }
        return files;
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
        return new DfgViewerFileRef(path, contentType);
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

}
