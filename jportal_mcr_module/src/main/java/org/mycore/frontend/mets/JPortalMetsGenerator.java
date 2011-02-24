package org.mycore.frontend.mets;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.mycore.datamodel.ifs.MCRDirectory;
import org.mycore.datamodel.ifs.MCRFile;
import org.mycore.datamodel.ifs.MCRFileContentTypeFactory;
import org.mycore.datamodel.ifs.MCRFilesystemNode;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.Div;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.model.struct.StructLink;
import org.mycore.mets.model.struct.SubDiv;

public class JPortalMetsGenerator extends MCRMETSGenerator {

    private static final Logger LOGGER = Logger.getLogger(JPortalMetsGenerator.class);

    @Override
    public Document getMETS(MCRDirectory dir, Set<MCRFilesystemNode> ignoreNodes) {
        long startTime = System.currentTimeMillis();        
        // get derivate
        MCRObjectID derId = MCRObjectID.getInstance(dir.getOwnerID());
        MCRDerivate mcrDer = MCRMetadataManager.retrieveMCRDerivate(derId);
        // get mycore object
        MCRObjectID objId = mcrDer.getDerivate().getMetaLink().getXLinkHrefID();
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(objId);
        
        LOGGER.info("create mets for derivate " + derId.toString() + "...");

        // create new mets
        Mets mets = new Mets();
        // add empty amd and dmd section
        String amdId = "amd_" + derId.toString();
        String dmdId = "dmd_" + derId.toString();
        AmdSec amdSec = new AmdSec(amdId);
        DmdSec dmdSec=  new DmdSec(dmdId);
        mets.addAmdSec(amdSec);
        mets.addDmdSec(dmdSec);
        // create file section
        FileSec fileSection = createFileSection(dir, ignoreNodes);
        mets.setFileSec(fileSection);
        // create physical section
        PhysicalStructMap physicalStructMap = createPhysicalStruct(derId, fileSection);
        mets.setPysicalStructMap(physicalStructMap);
        // create logical section
        LogicalStructMap logicalStructMap = createLogicalStruct(derId, mcrObj, amdId, dmdId);
        mets.setLogicalStructMap(logicalStructMap);
        // create struct link
        StructLink structLink = createStructLink(physicalStructMap, logicalStructMap);
        mets.setStructLink(structLink);

        LOGGER.info("mets creation for derivate " + derId.toString() + " took " +
                    (System.currentTimeMillis() - startTime) +  "ms!");

        return mets.asDocument();
    }

    private FileSec createFileSection(MCRDirectory dir, Set<MCRFilesystemNode> ignoreNodes) {
        FileSec fsec = new FileSec();
        FileGrp fgroup = new FileGrp(FileGrp.USE_MASTER);
        fsec.addFileGrp(fgroup);
        addFolder(fgroup, dir, ignoreNodes);
        return fsec;
    }

    private void addFolder(FileGrp fgroup, MCRDirectory dir, Set<MCRFilesystemNode> ignoreNodes) {
        MCRFilesystemNode[] children = dir.getChildren(MCRDirectory.SORT_BY_NAME_IGNORECASE);
        for (MCRFilesystemNode node : children) {
            if (ignoreNodes.contains(node)) {
                continue;
            } else if (node instanceof MCRDirectory) {
                MCRDirectory subDir = (MCRDirectory) node;
                addFolder(fgroup, subDir, ignoreNodes);
            } else {
                MCRFile subFile = (MCRFile) node;
                // create new file
                final UUID uuid = UUID.randomUUID();
                final String fileID = FileGrp.PREFIX_MASTER + uuid.toString();
                final String mimeType = MCRFileContentTypeFactory.getType(subFile.getContentTypeID()).getMimeType();
                File file = new File(fileID, mimeType);
                // set fLocat
                try {
                    final String href = new URI(null, subFile.getAbsolutePath().substring(1), null).toString();
                    FLocat fLocat = new FLocat(FLocat.LOCTYPE_URL, href);
                    file.setFLocat(fLocat);
                } catch(URISyntaxException uriSyntaxException) {
                    LOGGER.error("invalid href",uriSyntaxException);
                    continue;
                }
                fgroup.addFile(file);
            }
        }
    }

    private PhysicalStructMap createPhysicalStruct(MCRObjectID derId, FileSec fileSec) {
        PhysicalStructMap pstr = new PhysicalStructMap();
        // set main div
        Div mainDiv = new Div("phys_" + derId.toString(), Div.TYPE_PHYS_SEQ);
        pstr.setDivContainer(mainDiv);
        // run through files
        FileGrp masterGroup = fileSec.getFileGroup(FileGrp.USE_MASTER);
        List<File> fList = masterGroup.getfList();
        int order = 1;
        for(File file : fList) {
            String fileId = file.getId();
            // add page
            SubDiv page = new SubDiv(SubDiv.ID_PREFIX + fileId, SubDiv.TYPE_PAGE, order++, true);
            mainDiv.addSubDiv(page);
            // add file pointer
            Fptr fptr = new Fptr(fileId);
            page.addFptr(fptr);
        }
        return pstr;
    }

    private LogicalStructMap createLogicalStruct(MCRObjectID derId, MCRObject mcrObj, String amdId, String dmdId) {
        LogicalStructMap lstr = new LogicalStructMap();
        // create main div
        Div mainDiv = new Div("log_" + derId.toString(), dmdId, amdId, getType(mcrObj.getId()), getLabel(mcrObj));
        mainDiv.setOrder(1);
        lstr.setDivContainer(mainDiv);

        // run through all children
        List<MCRMetaLinkID> links = mcrObj.getStructure().getChildren();
        for(int i = 0; i < links.size(); i++) {
            MCRMetaLinkID linkId = links.get(i);
            SubDiv subDiv = createLogicalStruct(linkId.getXLinkHrefID(), i + 1);
            mainDiv.addSubDiv(subDiv);
        }
        return lstr;
    }

    private SubDiv createLogicalStruct(MCRObjectID mcrId, int order) {
        MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(mcrId);
        String id = "log_" + mcrId.toString();
        SubDiv parentDiv = new SubDiv(id, getType(mcrId), order, getLabel(mcrObj));
        // run through all children
        List<MCRMetaLinkID> links = mcrObj.getStructure().getChildren();
        for(int i = 0; i < links.size(); i++) {
            MCRMetaLinkID linkId = links.get(i);
            SubDiv subDiv = createLogicalStruct(linkId.getXLinkHrefID(), i + 1);
            parentDiv.addLogicalDiv(subDiv);
        }
        return parentDiv;
    }

    private StructLink createStructLink(PhysicalStructMap physMap, LogicalStructMap logicalStructMap) {
        StructLink structLink = new StructLink();
        SubDiv logicalMainDiv = logicalStructMap.getDivContainer().asLogicalSubDiv();
        Div mainDiv = physMap.getDivContainer();
        List<SubDiv> subDivList = mainDiv.getSubDivList();
        for(SubDiv physLink : subDivList) {
            SmLink smLink = new SmLink(logicalMainDiv, physLink);
            structLink.addSmLink(smLink);
        }
        return structLink;
    }

    private String getType(MCRObjectID id) {
        return id.getTypeId().substring(2);
    }

    private String getLabel(MCRObject obj) {
        MCRMetaElement me = obj.getMetadata().getMetadataElement("maintitles");
        if(me != null) {
            Iterator<MCRMetaInterface> it = me.iterator();
            while(it.hasNext()) {
                MCRMetaInterface mi = it.next();
                if(mi.getInherited() == 0 && mi.getSubTag().equals("maintitle") && mi instanceof MCRMetaLangText) {
                    return ((MCRMetaLangText)mi).getText();
                }
            }
        }
        return "no title for " + obj.getId().toString();
    }
}
