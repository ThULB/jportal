package fsu.jportal.mets;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Uses the jportal mets generator and adds the support for ALTO files. This includes
 * adding alto files to the fileSec and physical struct map.
 * <p>
 * An additional feature is coping ALTO block references from an "old"
 * mets to the newly generated one.
 * </p>
 * 
 * @author Matthias Eichner
 */
public class ALTOMETSHierarchyGenerator extends JPortalMetsGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    private Mets oldMets;

    private Set<FileRef> files;

    public ALTOMETSHierarchyGenerator(Mets oldMets) {
        super();
        this.oldMets = oldMets;
        this.files = new TreeSet<>();
    }

    @Override
    protected FileSec createFileSection(MCRPath dir, Set<MCRPath> ignoreNodes) throws IOException {
        FileSec fileSec = new FileSec();
        FileGrp masterGroup = new FileGrp(FileGrp.USE_MASTER);
        FileGrp altoGroup = new FileGrp("ALTO");

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            for (Path child : dirStream) {
                MCRPath imagePath = MCRPath.toMCRPath(child);
                if (ignoreNodes.contains(imagePath)) {
                    continue;
                }
                if (Files.isDirectory(imagePath)) {
                    continue;
                }
                String contentType = MCRContentTypes.probeContentType(imagePath);
                if (!contentType.startsWith("image")) {
                    continue;
                }
                FileRef ref = new FileRef();
                ref.uuid = UUID.randomUUID().toString();
                ref.imagePath = imagePath;
                ref.imageContentType = contentType;

                // check if alto exists
                String imageFileName = imagePath.getFileName().toString();
                String altoFileName = imageFileName.substring(0, imageFileName.lastIndexOf(".")) + ".xml";
                MCRPath altoPath = (MCRPath) imagePath.getParent().resolve("alto").resolve(altoFileName);
                if (Files.exists(altoPath)) {
                    ref.altoPath = altoPath;
                    ref.altoContentType = MCRContentTypes.probeContentType(altoPath);
                }
                this.files.add(ref);
            }
        }
        for (FileRef ref : this.files) {
            addFile(ref.toImageId(), masterGroup, ref.imagePath, ref.imageContentType);
            if (ref.altoPath != null) {
                addFile(ref.toAltoId(), altoGroup, ref.altoPath, ref.altoContentType);
            }
        }
        if (!masterGroup.getFileList().isEmpty()) {
            fileSec.addFileGrp(masterGroup);
        }
        if (!altoGroup.getFileList().isEmpty()) {
            fileSec.addFileGrp(altoGroup);
        }
        return fileSec;
    }

    private File addFile(String id, FileGrp fileGroup, MCRPath imagePath, String mimeType) {
        File imageFile = new File(id, mimeType);
        try {
            final String href = MCRXMLFunctions.encodeURIPath(imagePath.getOwnerRelativePath().substring(1), true);
            FLocat fLocat = new FLocat(LOCTYPE.URL, href);
            imageFile.setFLocat(fLocat);
            fileGroup.addFile(imageFile);
        } catch (URISyntaxException uriSyntaxException) {
            LOGGER.error("invalid href", uriSyntaxException);
        }
        return imageFile;
    }

    @Override
    protected PhysicalStructMap createPhysicalStruct() {
        PhysicalStructMap pstr = new PhysicalStructMap();
        PhysicalDiv physicalDiv = new PhysicalDiv("phys_" + this.mcrDer.getId().toString(), PhysicalDiv.TYPE_PHYS_SEQ);
        pstr.setDivContainer(physicalDiv);
        // run through file references
        for (FileRef ref : this.files) {
            PhysicalSubDiv page = new PhysicalSubDiv(PhysicalSubDiv.ID_PREFIX + ref.uuid, PhysicalSubDiv.TYPE_PAGE);
            physicalDiv.add(page);
            // add file pointers
            page.add(new Fptr(ref.toImageId()));
            if (ref.altoPath != null) {
                page.add(new Fptr(ref.toAltoId()));
            }
        }
        return pstr;
    }

    @Override
    protected LogicalStructMap createLogicalStruct() {
        LogicalStructMap lsm = super.createLogicalStruct();
        if (this.oldMets == null) {
            return lsm;
        }
        LogicalStructMap oldLsm = this.oldMets.getLogicalStructMap();

        FileGrp oldAltoGroup = this.oldMets.getFileSec().getFileGroup("ALTO");
        FileGrp newAltoGroup = this.fileSection.getFileGroup("ALTO");

        List<LogicalDiv> descendants = oldLsm.getDivContainer().getDescendants();
        descendants.stream().filter(div -> {
            return !div.getFptrList().isEmpty();
        }).forEach(oldDiv -> {
            String id = oldDiv.getId();
            LogicalDiv newDiv = lsm.getDivContainer().getLogicalSubDiv(id);
            if (newDiv != null) {
                for (Fptr fptr : oldDiv.getFptrList()) {
                    for (Seq seq : fptr.getSeqList()) {
                        for (Area area : seq.getAreaList()) {
                            String oldFileID = area.getFileId();
                            File oldFile = oldAltoGroup.getFileById(oldFileID);
                            String href = oldFile.getFLocat().getHref();
                            File newFile = newAltoGroup.getFileByHref(href);
                            area.setFileId(newFile.getId());
                        }
                    }
                    newDiv.getFptrList().add(fptr);
                }
            }
        });
        return lsm;
    }

    private class FileRef implements Comparable<FileRef> {

        String uuid;

        public MCRPath imagePath;

        public MCRPath altoPath;

        public String imageContentType;

        public String altoContentType;

        public String toImageId() {
            return "master_" + uuid;
        }

        public String toAltoId() {
            return "alto_" + uuid;
        }

        @Override
        public int compareTo(FileRef ref) {
            return imagePath.compareTo(ref.imagePath);
        }
    }

}
