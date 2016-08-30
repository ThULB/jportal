package fsu.jportal.mets;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;

public class ALTOMETSHierarchyGenerator extends JPortalMetsGenerator {

    private static final Logger LOGGER = Logger.getLogger(ALTOMETSHierarchyGenerator.class);

    @Override
    protected FileSec createFileSection(MCRPath dir, Set<MCRPath> ignoreNodes) throws IOException {
        FileSec fileSec = new FileSec();
        FileGrp masterGroup = new FileGrp(FileGrp.USE_MASTER);
        FileGrp altoGroup = new FileGrp("ALTO");
        fileSec.addFileGrp(masterGroup);
        fileSec.addFileGrp(altoGroup);

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(dir)) {
            for (Path child : dirStream) {
                MCRPath imagePath = MCRPath.toMCRPath(child);
                if (ignoreNodes.contains(imagePath)) {
                    continue;
                }
                if (Files.isDirectory(imagePath)) {
                    continue;
                }
                // add image
                final UUID uuid = UUID.randomUUID();
                addFile("master_" + uuid.toString(), masterGroup, imagePath);
                // add alto
                String imageFileName = imagePath.getFileName().toString();
                String altoFileName = imageFileName.substring(0, imageFileName.lastIndexOf(".")) + ".xml";
                MCRPath altoPath = (MCRPath) imagePath.getParent().resolve("alto").resolve(altoFileName);
                if (Files.exists(altoPath)) {
                    addFile("alto_" + uuid.toString(), altoGroup, altoPath);
                }
            }
        }
        return fileSec;
    }

    private File addFile(String id, FileGrp fileGroup, MCRPath imagePath) throws IOException {
        final String mimeType = MCRContentTypes.probeContentType(imagePath);
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
        // set main div
        PhysicalDiv physicalDiv = new PhysicalDiv("phys_" + this.mcrDer.getId().toString(), PhysicalDiv.TYPE_PHYS_SEQ);
        pstr.setDivContainer(physicalDiv);
        // run through files
        FileGrp masterGroup = this.fileSection.getFileGroup(FileGrp.USE_MASTER);
        List<File> fList = masterGroup.getFileList();
        for (File imageFile : fList) {
            String imageId = imageFile.getId();
            String uuid = imageId.replaceAll("master_", "");
            // add page
            PhysicalSubDiv page = new PhysicalSubDiv(PhysicalSubDiv.ID_PREFIX + uuid, PhysicalSubDiv.TYPE_PAGE);
            physicalDiv.add(page);
            // add file pointers
            page.add(new Fptr(imageId));
            page.add(new Fptr("alto_" + uuid));
        }
        return pstr;
    }

}
