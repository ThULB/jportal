package fsu.jportal.mets;

import static fsu.jportal.mets.ZvddMetsTools.dv;
import static fsu.jportal.mets.ZvddMetsTools.mods;
import static fsu.jportal.mets.ZvddMetsTools.modsIdentifier;
import static fsu.jportal.mets.ZvddMetsTools.modsTitleInfo;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DigiprovMD;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.sections.RightsMD;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.Mptr;
import org.mycore.mets.model.struct.PhysicalStructMap;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPDateUtil;
import fsu.jportal.xml.JPXMLFunctions;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This implementation is used for all jpjournals.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddJournalMetsGenerator implements MCRMETSGenerator {

    private JPJournal journal;

    public ZvddJournalMetsGenerator(JPJournal journal) {
        this.journal = journal;
    }

    @Override
    public Mets generate() throws MCRException {
        Mets mets = new Mets();
        mets.removeStructMap(PhysicalStructMap.TYPE);
        mets.addDmdSec(createDmdSec());
        mets.addAmdSec(createAmdSec());
        mets.addStructMap(createLogicalStructMap());
        return mets;
    }

    protected DmdSec createDmdSec() {
        DmdSec dmd = new DmdSec("dmd_" + journal.getId().toString());

        Element mods = mods("mods");
        // title
        mods.addContent(modsTitleInfo(journal.getTitle(), null));
        // language
        journal.getLanguageCode().map(ZvddMetsTools::modsLanguage).ifPresent(mods::addContent);
        // identifier
        journal.listIdenti().stream().map(text -> modsIdentifier("identifier", text.getType(), text.getText()))
            .forEach(mods::addContent);

        MdWrap mdWrap = new MdWrap(MDTYPE.MODS, mods);
        dmd.setMdWrap(mdWrap);
        return dmd;
    }

    protected AmdSec createAmdSec() {
        AmdSec amd = new AmdSec("amd_" + journal.getId().toString());
        amd.setDigiprovMD(createDigiprovMD());
        List<RightsMD> owners = journal.getParticipants("owner").stream()
            .map(JPInstitution::new)
            .map(this::createRightsMD)
            .collect(Collectors.toList());
        if (owners.isEmpty()) {
            owners.add(createDefaultRightsMD());
        }
        owners.forEach(rightsMD -> amd.listRightsMD().add(rightsMD));
        return amd;
    }

    /**
     * Creates the default rightsMD using the mycore properties JP.Site.Owner.*. This method should only be used if
     * the journals does not specify owners.
     *
     * @return a new rightsMD object using mycore properties
     */
    protected RightsMD createDefaultRightsMD() {
        MCRConfiguration conf = MCRConfiguration.instance();
        Element dvRights = dv("rights");
        String label = conf.getString("JP.Site.Owner.label");
        String logo = conf.getString("JP.Site.Footer.Logo.url");
        String siteURL = conf.getString("JP.Site.Owner.url");
        dvRights.addContent(dv("owner").setText(label));
        dvRights.addContent(dv("ownerLogo").setText(logo));
        dvRights.addContent(dv("ownerSiteURL").setText(MCRFrontendUtil.getBaseURL() + siteURL));
        return buildRightsMD("RIGHTS", dvRights);
    }

    /**
     * Creates a rightsMD object for the given institution.
     *
     * @param institution the institution to convert
     * @return a new rightsMD object
     */
    protected RightsMD createRightsMD(JPInstitution institution) {
        Element dvRights = dv("rights");
        dvRights.addContent(dv("owner").setText(institution.getTitle()));
        institution.getLogo()
            .map(JPXMLFunctions::getLogoURL)
            .map(logoURL -> dv("ownerLogo").setText(logoURL))
            .ifPresent(dvRights::addContent);
        institution.getURL()
            .map(url -> dv("ownerSiteURL").setText(url.getXLinkHref()))
            .ifPresent(dvRights::addContent);
        return buildRightsMD("RIGHTS_" + institution.getId().toString(), dvRights);
    }

    private RightsMD buildRightsMD(String id, Element dvRights) {
        RightsMD rightsMD = new RightsMD(id);
        MdWrap mdWrap = new MdWrap(MDTYPE.OTHER, dvRights);
        mdWrap.setOtherMdType("DVRIGHTS");
        rightsMD.setMdWrap(mdWrap);
        return rightsMD;
    }

    protected DigiprovMD createDigiprovMD() {
        DigiprovMD digiprovMD = new DigiprovMD("DIGIPROV");
        Element presentation = dv("presentation")
            .setText(MCRFrontendUtil.getBaseURL() + "receive/" + journal.getId().toString());
        Element links = dv("links");
        links.addContent(presentation);
        MdWrap mdWrap = new MdWrap(MDTYPE.OTHER, links);
        mdWrap.setOtherMdType("DVLINKS");
        digiprovMD.setMdWrap(mdWrap);
        return digiprovMD;
    }

    protected LogicalStructMap createLogicalStructMap() {
        LogicalStructMap lsm = new LogicalStructMap();
        String journalId = journal.getId().toString();
        LogicalDiv root = new LogicalDiv("log_" + journalId, getType(journal), journal.getTitle(), "amd_" + journalId,
            "dmd_" + journalId);
        lsm.setDivContainer(root);
        listYearVolumes(this.journal).stream().map(this::toLogicalDiv).forEach(root::add);
        return lsm;
    }

    /**
     * Returns the @TYPE attribute of the logical div of the journal.
     * 
     * @param journal the journal to get the type from
     * @return the type. e.g. periodical, newspaper...
     */
    protected String getType(JPJournal journal) {
        return journal.isJournalType("jportal_class_00000200", "newspapers") ? "newspaper" : "periodical";
    }

    /**
     * Converts a volume to a logical div.
     * 
     * @param volume the volume to convert
     * @return a new logical div
     */
    protected LogicalDiv toLogicalDiv(JPVolume volume) {
        LogicalDiv div = new LogicalDiv("log_" + volume.getId(), "volume", ZvddMetsTools.getTitle(volume));
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + volume.getId();
        div.setMptr(new Mptr(href, LOCTYPE.URL));
        return div;
    }

    /**
     * Checks first if the journals has "real" year volumes. The date of the volumes is exactly a year. If there are
     * no volumes with years we search for volumes with derivates and take those instead.
     *
     * @param journal the journal to get the year volumes
     * @return list of volumes
     */
    protected List<JPVolume> listYearVolumes(JPJournal journal) {
        List<JPVolume> yearVolumes = listYearVolumesByDate(journal);
        if (yearVolumes.isEmpty()) {
            yearVolumes = listYearVolumesByDerivate(journal);
        }
        return yearVolumes;
    }

    /**
     * Runs recursive through the given container and tries to find all volumes with a published year.
     *
     * @param container the container to search in
     * @return list of volumes
     */
    protected List<JPVolume> listYearVolumesByDate(JPContainer container) {
        List<JPVolume> yearVolumes = new ArrayList<>();
        List<JPVolume> children = container.getChildren(JPObjectType.jpvolume).stream().map(JPVolume::new).collect(
            Collectors.toList());
        for (JPVolume child : children) {
            Optional<JPMetaDate> publishedOptional = child.getDate(JPPeriodicalComponent.DateType.published);
            if (publishedOptional.isPresent()) {
                Temporal published = publishedOptional.get().getDate();
                if (published != null && JPDateUtil.isYear(published)) {
                    yearVolumes.add(child);
                    continue;
                }
            }
            yearVolumes.addAll(listYearVolumesByDate(child));
        }
        return yearVolumes;
    }

    /**
     * Runs recursive through the given container and tries to find all volumes with a derivate.
     *
     * @param container the container to search in
     * @return list of volumes
     */
    protected List<JPVolume> listYearVolumesByDerivate(JPContainer container) {
        List<JPVolume> yearVolumes = new ArrayList<>();
        List<JPVolume> children = container.getChildren(JPObjectType.jpvolume).stream().map(JPVolume::new).collect(
            Collectors.toList());
        for (JPVolume child : children) {
            if (child.getFirstDerivate().isPresent()) {
                yearVolumes.add(child);
                continue;
            }
            yearVolumes.addAll(listYearVolumesByDerivate(child));
        }
        return yearVolumes;
    }

}
