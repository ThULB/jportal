package fsu.jportal.mets;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.niofs.MCRPath;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.misc.DefaultStructLinkGenerator;
import org.mycore.mets.model.MCRMETSHierarchyGenerator;
import org.mycore.mets.model.Mets;
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
import org.mycore.mets.model.struct.SmLink;
import org.mycore.mets.model.struct.StructLink;

import fsu.jportal.backend.JPArticle;
import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPDerivateComponent;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.mcr.MetadataManager;
import fsu.jportal.util.JPComponentUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This implementation is used for all issues in newspapers.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddNewspaperIssueMetsGenerator extends JPMetsHierarchyGenerator {

    protected List<FileGrp> fileGroups;

    protected List<JPArticle> articles;

    protected JPVolume volume;

    protected JPDerivateComponent derivate;

    public ZvddNewspaperIssueMetsGenerator(JPVolume volume) {
        this.volume = volume;
        this.rootObj = this.volume.getObject();
    }

    @Override
    public Mets generate() throws MCRException {
        // get derivate
        this.derivate = locateDerivate(this.volume);
        if (derivate == null) {
            throw new MCRException("Unable to locate a derivate in the object '" + this.volume.getId() + "' or its"
                + " ancestors. The creating of the issue is not possible.");
        }
        this.mcrDer = this.derivate.getObject();
        // get files and build file groups
        this.files = getFiles();
        Collection<String> groupIds = DfgViewerFileRef.GROUP_TO_ZOOM_LEVEL_MAP.keySet();
        this.fileGroups = groupIds.stream().map(FileGrp::new).collect(Collectors.toList());

        // get articles
        this.articles = this.volume.streamArticles().collect(Collectors.toList());

        // initialize struct link map
        this.structLinkMap = new HashMap<>();

        // create mets
        Mets mets = new Mets();
        mets.addDmdSec(ZvddMetsTools.createDmdSec(this.volume, "issue"));
        mets.addAmdSec(ZvddMetsTools.createAmdSec(volume));
        this.articles.stream().map(ZvddMetsTools::createDmdSec).forEach(mets::addDmdSec);

        mets.setFileSec(createFileSec());
        mets.addStructMap(this.physicalStructMap = ZvddMetsTools.createPhysicalStructMap(files, fileGroups, null));
        mets.addStructMap(this.logicalStructMap = createLogicalStructMap(mets.getPhysicalStructMap()));
        mets.setStructLink(this.structLink = createStructLink(mets));
        return mets;
    }

    @Override
    public MCRPath getDerivatePath() {
        return this.derivate.getPath();
    }

    protected FileSec createFileSec() {
        FileSec fileSec = new FileSec();
        this.fileGroups.forEach(fileSec::addFileGrp);
        ZvddMetsTools.addFilesToGroups(this.files, this.fileGroups);
        return fileSec;
    }

    protected List<MCRMETSHierarchyGenerator.FileRef> getFiles() {
        try {
            Mets mets = MetadataManager.getMets(derivate);
            List<JPPeriodicalComponent> descendantsAndSelf = MetadataManager
                .getDescendantsAndSelf(volume.getObject())
                .stream()
                .map(JPComponentUtil::getPeriodical)
                .collect(Collectors.toList());

            List<PhysicalSubDiv> physicalIds = getPhysicalIds(descendantsAndSelf, mets);
            return physicalIds.stream().map(div -> toFileRef(div, mets, derivate)).collect(Collectors.toList());
        } catch (Exception exc) {
            throw new MCRException("Unable to get files of " + this.volume.getId(), exc);
        }
    }

    /**
     * Tries to get the first derivate with a mets.xml of the component itself or its ancestors.
     * 
     * @param component the component to look for the derivate
     * @return the derivate component or null
     */
    protected JPDerivateComponent locateDerivate(JPPeriodicalComponent component) {
        if (component == null) {
            return null;
        }
        List<JPDerivateComponent> derivates = component.getDerivates();
        if (!derivates.isEmpty()) {
            for (JPDerivateComponent derivate : derivates) {
                if(MetadataManager.hasMetsFile(derivate)){
                    return derivate;
                }
            }
        }
        return locateDerivate(component.getParent().orElse(null));
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

    protected LogicalStructMap createLogicalStructMap(PhysicalStructMap physicalStructMap) {
        LogicalStructMap struct = new LogicalStructMap();

        // journal div & pointer
        JPJournal journal = volume.getJournal();
        LogicalDiv journalDiv = new LogicalDiv("log_" + journal.getId().toString(), "newspaper", journal.getTitle());
        struct.setDivContainer(journalDiv);
        addMptr(journal, journalDiv);

        // get year and optional day volume
        JPVolume year = null, day = null;
        Optional<JPContainer> parentOptional = this.volume.getParent();
        while (parentOptional.isPresent()) {
            JPContainer parent = parentOptional.get();
            if (!JPComponentUtil.is(parent, JPObjectType.jpvolume)) {
                break;
            }
            JPVolume parentVolume = (JPVolume) parent;
            List<String> volumeTypes = parentVolume.getVolumeTypes();
            if (volumeTypes.contains("year")) {
                year = parentVolume;
            } else if (volumeTypes.contains("day")) {
                day = parentVolume;
            }
            parentOptional = parentVolume.getParent();
        }

        // add year
        if (year == null) {
            throw new MCRException("Unable to create logical struct map for " + this.volume.getId() + " because there "
                + "couldn't be a year parent volume determined.");
        }
        LogicalDiv yearDiv = new LogicalDiv("log_" + year.getId().toString(), "year", year.getTitle());
        journalDiv.add(yearDiv);
        addMptr(year, yearDiv);

        // add month
        Optional<Temporal> publishedDate = volume.getPublishedDate()
                .map(JPMetaDate::getDateOrFrom);
        int month = publishedDate
            .map(t -> t.get(ChronoField.MONTH_OF_YEAR)).orElse(1);
        LogicalDiv monthDiv = new LogicalDiv("log_month_" + month, "month", year.getTitle());
        publishedDate.map(LocalDate::from)
                .map(d -> d.format(DateTimeFormatter.ofPattern("YYYY-MM")))
                .ifPresent(monthDiv::setOrderLabel);
        yearDiv.add(monthDiv);

        // day if present
        LogicalDiv parentIssueDiv = monthDiv;
        if (day != null) {
            LogicalDiv dayDiv = new LogicalDiv("log_" + day.getId(), "day", day.getTitle());
            publishedDate.map(LocalDate::from)
                    .map(d -> d.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")))
                    .ifPresent(dayDiv::setOrderLabel);
            monthDiv.add(dayDiv);
            parentIssueDiv = dayDiv;
        }

        // the issue itself
        LogicalDiv issueDiv = new LogicalDiv("log_" + volume.getId(), "issue", volume.getTitle());
        issueDiv.setDmdId("dmd_" + volume.getId());
        parentIssueDiv.add(issueDiv);
        addToStructLinkMap(issueDiv, physicalStructMap.getDivContainer().getChildren().get(0));

        // articles
        for (JPArticle article : this.articles) {
            LogicalDiv articleDiv = new LogicalDiv("log_" + article.getId(), "article", article.getTitle());
            articleDiv.setDmdId("dmd_" + article.getId());
            issueDiv.add(articleDiv);
            updateStructLinkMapUsingDerivateLinks(articleDiv, article.getObject(), this.fileGroups.get(0));
        }

        return struct;
    }

    protected void addMptr(JPContainer container, LogicalDiv yearDiv) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + container.getId().toString();
        Mptr mptr = new Mptr(href, LOCTYPE.URL);
        yearDiv.setMptr(mptr);
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
            return buildFileRef(path, contentType);
        }
        return null;
    }

    @Override
    protected FileRef buildFileRef(MCRPath path, String contentType) {
        return new DfgViewerFileRef(path, contentType);
    }

    protected StructLink createStructLink(Mets mets) {
        DefaultStructLinkGenerator structLinkGenerator = new DefaultStructLinkGenerator(this.structLinkMap);
        structLinkGenerator.setFailEasy(false);
        return structLinkGenerator.generate(mets.getPhysicalStructMap(), mets.getLogicalStructMap());
    }

}
