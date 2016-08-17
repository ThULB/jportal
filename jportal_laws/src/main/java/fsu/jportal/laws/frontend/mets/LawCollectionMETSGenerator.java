/**
 * 
 */
package fsu.jportal.laws.frontend.mets;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

import javax.activation.MimetypesFileTypeMap;

import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.misc.LogicalIdProvider;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.model.struct.StructLink;
import org.mycore.mets.tools.MCRMetsSave;

/**
 * @author shermann
 * @author tchef
 */
public class LawCollectionMETSGenerator extends MCRMETSGenerator {

    @Override
    public Mets getMETS(MCRPath dir, Set<MCRPath> ignoreNodes) throws IOException {
        final String metsFile = MCRMetsSave.getMetsFileName();
        SortedSet<Path> files = new TreeSet<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path curPath : stream) {
                if (!curPath.getFileName().toString().equals(metsFile)) {
                    files.add(curPath);
                }
            }
        }

        // init the mets document
        Mets mets = initMets(dir.getOwner(), files.first().getFileName().toString());
        PhysicalStructMap physicalStructMap = (PhysicalStructMap) mets.getStructMap(PhysicalStructMap.TYPE);
        LogicalStructMap logicalStructMap = (LogicalStructMap) mets.getStructMap(LogicalStructMap.TYPE);

        LogicalIdProvider idProvider = new LogicalIdProvider("log_", 6);
        LogicalDiv logDiv = null;

        Path[] nodes = files.toArray(new Path[files.size()]);

        for (int i = 0; i < nodes.length; i++) {
            Path node = nodes[i];
            String filename = node.getFileName().toString();
            if (filename.equals(metsFile)) {
                continue;
            }
            // add the file to the filesection and filegrp
            File file = addFileToFileSection(filename, mets);
            // add file to the physical struct map
            PhysicalSubDiv physIdv = addFileToPhysicalStructMap(physicalStructMap, file);
            // now the tricky part, add file to logical struct map
            String[] filenameParts = filename.split("_");

            if (logDiv == null) {
                logDiv = new LogicalDiv(idProvider.getNextId(), DFGTypeProvider.getDFGType(filenameParts[3]),
                    LabelProvider.getLabel(filenameParts[3]));
                logicalStructMap.getDivContainer().add(logDiv);
            }
            // add file to smlink
            mets.getStructLink().addSmLink(new SmLink(logDiv.getId(), physIdv.getId()));

            // is there a file after the current?
            if (i < nodes.length - 1) {
                // set log div to null if condition applies
                if (!(nodes[i + 1].getFileName().toString().split("_")[3].equals(filenameParts[3]))) {
                    logDiv = null;
                }
            }
        }
        return mets;
    }

    /**
     * Adds the file to the physical struct map
     * 
     * @param mets
     *            the mets document
     * @param file
     *            the file to add
     * @param order
     *            the order of the file
     * @return the {@link PhysicalSubDiv} created
     */
    private PhysicalSubDiv addFileToPhysicalStructMap(PhysicalStructMap physicalStructMap, File file) {
        PhysicalSubDiv div = new PhysicalSubDiv(PhysicalSubDiv.ID_PREFIX + file.getId(), PhysicalSubDiv.TYPE_PAGE);
        div.add(new Fptr(file.getId()));
        physicalStructMap.getDivContainer().add(div);
        return div;
    }

    /**
     * Adds the file to the master file group within the file section.
     * 
     * @param filename
     *            the name of the file
     * @param mets
     *            the mets document
     * @return the {@link File} created
     */
    private File addFileToFileSection(String filename, Mets mets) {
        // create file ref
        UUID uuid = UUID.randomUUID();
        File file = new File(FileGrp.USE_MASTER + uuid, new MimetypesFileTypeMap().getContentType(filename));

        FLocat fLoc = new FLocat(LOCTYPE.URL, filename);
        file.setFLocat(fLoc);
        // add file to group
        mets.getFileSec().getFileGroup(FileGrp.USE_MASTER).addFile(file);

        return file;
    }

    /**
     * Method inits the {@link Mets} object.
     * 
     * @param derivateId
     */
    private Mets initMets(String derivateId, String sampleFilename) {
        FileSec fileSec;
        FileGrp fileGrpMaster;
        PhysicalStructMap physicalStructMp;
        LogicalStructMap logicalStructMp;
        StructLink structLink;

        Mets mets = new Mets();
        AmdSec amdSec = new AmdSec("amd_" + derivateId);
        DmdSec dmdSec = new DmdSec("dmd_" + derivateId);
        mets.addAmdSec(amdSec);
        mets.addDmdSec(dmdSec);

        // filesec
        fileSec = new FileSec();
        fileGrpMaster = new FileGrp(FileGrp.USE_MASTER);
        fileSec.addFileGrp(fileGrpMaster);

        mets.setFileSec(fileSec);

        /* init the two structure maps */
        /* init logical structure map */
        logicalStructMp = new LogicalStructMap();

        String[] fNameParts = sampleFilename.split("_");
        String authority = LabelProvider.getLabel(fNameParts[1]);
        String year = fNameParts[2];
        String label = authority + " - " + year;
        LogicalDiv logDivContainer = new LogicalDiv("log_" + derivateId, "monograph", label, amdSec.getId(),
            dmdSec.getId());
        logicalStructMp.setDivContainer(logDivContainer);

        /* init physical structure map */
        physicalStructMp = new PhysicalStructMap();
        PhysicalDiv physDivContainer = new PhysicalDiv("phys_" + dmdSec.getId(), PhysicalDiv.TYPE_PHYS_SEQ);
        physicalStructMp.setDivContainer(physDivContainer);

        /* add the different structure maps */
        mets.addStructMap(physicalStructMp);
        mets.addStructMap(logicalStructMp);

        /* add the struct link section */
        structLink = new StructLink();
        mets.setStructLink(structLink);

        return mets;
    }
}
