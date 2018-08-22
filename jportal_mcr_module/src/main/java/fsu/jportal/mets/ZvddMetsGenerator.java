package fsu.jportal.mets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
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
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPComponentUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This version is compatible with the DFG viewer.</p>
 *
 * <p></p>
 *
 * @author Matthias Eichner
 */
public class ZvddMetsGenerator extends JPMetsHierarchyGenerator {

    private static Map<String, String> GROUP_TO_ZOOM_LEVEL_MAP;

    static {
        GROUP_TO_ZOOM_LEVEL_MAP = new HashMap<>();
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_MIN, "MIN");
        GROUP_TO_ZOOM_LEVEL_MAP.put(FileGrp.USE_DEFAULT, "MID");
    }

    private JPVolume volume;

    private JPDerivateComponent derivate;

    private List<FileGrp> fileGroups;

    public ZvddMetsGenerator(MCRObjectID id) {
        Optional<JPVolume> optionalVolume = JPComponentUtil.get(id, JPVolume.class);
        if (!optionalVolume.isPresent()) {
            throw new IllegalArgumentException("Unsupported periodical type " + id.getTypeId());
        }
        this.volume = optionalVolume.get();
        this.derivate = volume.getFirstDerivate()
            .orElseThrow(() -> new MCRException(id.toString() + " has no derivate!"));
        this.files = new ArrayList<>();
        this.setDerivatePath(this.derivate.getPath());
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
            throw new MCRException("Unable to generate zvdd mets.xml because cannot read files.", ioExc);
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
            final PhysicalSubDiv div = new PhysicalSubDiv(file.toPhysId(), PhysicalSubDiv.TYPE_PAGE);
            div.setOrder(order.getAndIncrement());
            this.fileGroups.forEach(group -> {
                Fptr fptr = new Fptr(file.toFileId(group));
                div.add(fptr);
            });
            physicalDiv.add(div);
        });

        return struct;
    }

    @Override
    protected LogicalStructMap createLogicalStruct() {
        final LogicalStructMap struct = new LogicalStructMap();

        // journal div & pointer
        JPJournal journal = volume.getJournal();
        LogicalDiv journalDiv = new LogicalDiv("log_" + journal.getId().toString(), "periodical", journal.getTitle());
        struct.setDivContainer(journalDiv);

        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + journal.getId().toString();
        Mptr mptr = new Mptr(href, LOCTYPE.URL);
        journalDiv.setMptr(mptr);

        // volume itself
        LogicalDiv volumeDiv = new LogicalDiv("log_" + volume.getId().toString(), "volume", volume.getTitle());
        volumeDiv.setDmdId("dmd_" + volume.getId());
        volumeDiv.setAmdId("amd_" + volume.getId());
        journalDiv.add(volumeDiv);

        // struct link
        updateStructLinkMapUsingDerivateLinks(volumeDiv, volume.getObject(), this.fileGroups.get(0));

        // hierarchy
        volume.getChildren().forEach(childId -> buildHierarchy(childId, volumeDiv));

        return struct;
    }

    private void buildHierarchy(final MCRObjectID id, final LogicalDiv parentDiv) {
        Optional<JPPeriodicalComponent> periodicalOptional = JPComponentUtil.getPeriodical(id);
        periodicalOptional.ifPresent(periodical -> {
            // add div
            String type = JPArticle.TYPE.equals(periodical.getType()) ? "article" : "issue";
            LogicalDiv childDiv = new LogicalDiv("log_" + id, type, periodical.getTitle());
            childDiv.setDmdId("dmd_" + id);
            parentDiv.add(childDiv);

            // struct link
            updateStructLinkMapUsingDerivateLinks(childDiv, periodical.getObject(), this.fileGroups.get(0));

            // recursive children
            if (JPVolume.TYPE.equals(periodical.getType())) {
                JPVolume volume = (JPVolume) periodical;
                volume.getChildren().forEach(childId -> buildHierarchy(childId, childDiv));
            }
        });
    }

    @Override
    protected FileRef buildFileRef(MCRPath path, String contentType) {
        return new ZvddFileRef(path, contentType);
    }

    protected static class ZvddFileRef implements FileRef {

        private MCRPath path;

        private String contentType;

        ZvddFileRef(MCRPath path, String contentType) {
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
