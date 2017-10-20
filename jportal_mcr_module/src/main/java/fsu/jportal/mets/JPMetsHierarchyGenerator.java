package fsu.jportal.mets;

import static fsu.jportal.frontend.SolrToc.buildQuery;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.mycore.common.xml.MCRXMLFunctions;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.niofs.MCRContentTypes;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.files.FLocat;
import org.mycore.mets.model.files.File;
import org.mycore.mets.model.files.FileGrp;
import org.mycore.mets.model.files.FileSec;
import org.mycore.mets.model.struct.Area;
import org.mycore.mets.model.struct.Fptr;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.PhysicalDiv;
import org.mycore.mets.model.struct.PhysicalStructMap;
import org.mycore.mets.model.struct.PhysicalSubDiv;
import org.mycore.mets.model.struct.Seq;
import org.mycore.mets.tools.MCRMetsSave;
import org.mycore.solr.MCRSolrClientFactory;
import org.mycore.solr.search.MCRSolrSearchUtils;

/**
 * Uses the mycore mets hierarchy generator and adds the support for ALTO files. This includes
 * adding alto files to the fileSec and physical struct map.
 * <p>
 * An additional feature is coping ALTO block references from an "old"
 * mets to the newly generated one.
 * </p>
 *
 * @author Matthias Eichner
 */
public class JPMetsHierarchyGenerator extends MCRMETSHierarchyGenerator {

    private static final Logger LOGGER = LogManager.getLogger();

    private Set<FileRef> files;

    public JPMetsHierarchyGenerator() {
        super();
        this.files = new TreeSet<>();
    }

    protected String getType(MCRObject obj) {
        return obj.getId().getTypeId().substring(2);
    }

    protected String getLabel(MCRObject obj) {
        Optional<MCRMetaLangText> maintitle = obj.getMetadata().findFirst("maintitles");
        return maintitle.map(MCRMetaLangText::getText).orElse("no title for " + obj.getId());
    }

    protected List<MCRObject> getChildren(MCRObject parentObject) {
        if (parentObject.getId().getTypeId().equals("jparticle")) {
            return Collections.emptyList();
        }
        List<MCRObject> children = new ArrayList<>();
        getChildren(parentObject, "jpvolume").stream()
                                             .map(MCRMetadataManager::retrieveMCRObject)
                                             .forEach(children::add);
        getChildren(parentObject, "jparticle").stream()
                                              .map(MCRMetadataManager::retrieveMCRObject)
                                              .forEach(children::add);
        return children;
    }

    protected List<MCRObjectID> getChildren(MCRObject parentObject, String objectType) {
        String parentID = parentObject.getId().toString();
        ModifiableSolrParams solrParams = buildQuery(parentID, objectType, "order asc");
        solrParams.set("fl", "id objectType");
        SolrClient solrClient = MCRSolrClientFactory.getSolrClient();
        return MCRSolrSearchUtils.stream(solrClient, solrParams).map(doc -> {
            String id = (String) doc.getFieldValue("id");
            return MCRObjectID.getInstance(id);
        }).collect(Collectors.toList());
    }

    @Override
    protected FileSec createFileSection() throws IOException {
        FileSec fileSec = new FileSec();
        FileGrp masterGroup = new FileGrp(FileGrp.USE_MASTER);
        FileGrp altoGroup = new FileGrp("ALTO");

        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(getDerivatePath())) {
            for (Path child : dirStream) {
                MCRPath imagePath = MCRPath.toMCRPath(child);
                if (getIgnorePaths().contains(imagePath)) {
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

    private void addFile(String id, FileGrp fileGroup, MCRPath imagePath, String mimeType) {
        File imageFile = new File(id, mimeType);
        try {
            final String href = MCRXMLFunctions.encodeURIPath(imagePath.getOwnerRelativePath().substring(1), true);
            FLocat fLocat = new FLocat(LOCTYPE.URL, href);
            imageFile.setFLocat(fLocat);
            fileGroup.addFile(imageFile);
        } catch (URISyntaxException uriSyntaxException) {
            LOGGER.error("invalid href", uriSyntaxException);
        }
    }

    @Override
    protected PhysicalStructMap createPhysicalStruct() {
        PhysicalStructMap pstr = new PhysicalStructMap();
        PhysicalDiv physicalDiv = new PhysicalDiv("phys_" + this.mcrDer.getId().toString(), PhysicalDiv.TYPE_PHYS_SEQ);
        pstr.setDivContainer(physicalDiv);
        // run through file references
        for (FileRef ref : this.files) {
            PhysicalSubDiv page = new PhysicalSubDiv(ref.toPhysId(), PhysicalSubDiv.TYPE_PAGE);
            getOrderLabel(ref.toImageId()).ifPresent(page::setOrderLabel);
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
        if (!this.getOldMets().isPresent()) {
            return lsm;
        }
        Mets oldMets = this.getOldMets().get();
        LogicalStructMap oldLsm = oldMets.getLogicalStructMap();
        FileGrp oldAltoGroup = oldMets.getFileSec().getFileGroup("ALTO");
        FileGrp newAltoGroup = this.fileSection.getFileGroup("ALTO");

        List<LogicalDiv> descendants = oldLsm.getDivContainer().getDescendants();
        descendants.stream().filter(div -> !div.getFptrList().isEmpty()).forEach(oldDiv -> {
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

        public MCRPath imagePath;

        public MCRPath altoPath;

        public String imageContentType;

        public String altoContentType;

        public String toImageId() {
            return "master_" + MCRMetsSave.getFileBase(imagePath);
        }

        public String toAltoId() {
            return "alto_" + MCRMetsSave.getFileBase(altoPath);
        }

        public String toPhysId() {
            return PhysicalSubDiv.ID_PREFIX + MCRMetsSave.getFileBase(imagePath);
        }

        @Override
        public int compareTo(FileRef ref) {
            return imagePath.compareTo(ref.imagePath);
        }
    }

    protected String getEnclosingDerivateLinkName() {
        return "derivateLinks";
    }

    protected String getDerivateLinkName() {
        return "derivateLink";
    }

}
