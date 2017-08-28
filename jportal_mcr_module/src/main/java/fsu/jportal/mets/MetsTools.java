package fsu.jportal.mets;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.tools.MCRMetsSave;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class MetsTools {
    static class MetsContainer {
        private MCRFile metsFile;

        private Mets mets;

        public MetsContainer(String derivateID) throws Exception {
            MCRDirectory rootDirectory = MCRDirectory.getRootDirectory(derivateID);
            if (rootDirectory == null) {
                throw new Exception("Could not create mets container, derivate " + derivateID + " does not exits.");
            }

            MCRFilesystemNode metsFile = rootDirectory.getChildByPath(MCRMetsSave.getMetsFileName());

            if (!(metsFile instanceof MCRFile)) {
                throw new Exception("Derivate " + derivateID + " has no METS file.");
            }

            this.metsFile = (MCRFile) metsFile;
        }

        public void updateFileEntry(MCRPath sourceNode, MCRPath target) {
            try {
                File sourceFileMetsEntry = getMetsEntry(sourceNode);
                PhysicalSubDiv sourceSubDiv = getPhysicalSubDiv(sourceFileMetsEntry);

                File targetFileMaetsEntry = getMetsEntry(target);
                PhysicalSubDiv targetSubDiv = getPhysicalSubDiv(targetFileMaetsEntry);

                List<SmLink> smLinkToSourceFile = getMets().getStructLink().getSmLinkByTo(sourceSubDiv.getId());

                for (SmLink smLink : smLinkToSourceFile) {
                    smLink.setTo(targetSubDiv.getId());
                }

                metsFile.setContentFrom(getMets().asDocument());
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        public File getMetsEntry(MCRPath fileNode) throws Exception, IOException, JDOMException {
            String sourcePath = fileNode.toAbsolutePath().toString();
            if (!sourcePath.equals("/")) {
                sourcePath = sourcePath.substring(1);
            }

            File sourceFileMetsEntry = getMets().getFileSec()
                                                .getFileGroup(FileGrp.USE_MASTER)
                                                .getFileByHref(sourcePath);
            return sourceFileMetsEntry;
        }

        public PhysicalSubDiv getPhysicalSubDiv(File metsFileEntry) throws Exception, IOException, JDOMException {
            String sourceFileMetsID = metsFileEntry.getId();
            PhysicalStructMap physStructMap = (PhysicalStructMap) getMets().getStructMap(PhysicalStructMap.TYPE);
            PhysicalDiv physDiv = physStructMap.getDivContainer();
            PhysicalSubDiv physicalSubDiv = physDiv.get("phys_" + sourceFileMetsID);

            return physicalSubDiv;
        }

        public Mets getMets() throws Exception, IOException, JDOMException {
            if (mets == null) {
                Document contentAsJDOM = metsFile.getContentAsJDOM();
                mets = new Mets(contentAsJDOM);
            }

            return mets;
        }
    }

    public static void updateFileEntry(MCRPath sourceNode, MCRPath target) {
        if (Files.exists(sourceNode) || Files.exists(target)) {
            return;
        }

        String derivateID = sourceNode.getOwner();
        try {
            MetsContainer metsContainer = new MetsContainer(derivateID);
            metsContainer.updateFileEntry(sourceNode, target);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
