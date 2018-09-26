package fsu.jportal.mets;

import static fsu.jportal.mets.ZvddMetsTools.mods;
import static fsu.jportal.mets.ZvddMetsTools.modsIdentifier;
import static fsu.jportal.mets.ZvddMetsTools.modsTitleInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.Mptr;
import org.mycore.mets.model.struct.PhysicalStructMap;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPVolume;

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
        mets.addAmdSec(ZvddMetsTools.createAmdSec(journal));
        mets.addStructMap(createLogicalStructMap());
        mets.setStructLink(null);
        return mets;
    }

    protected DmdSec createDmdSec() {
        DmdSec dmd = new DmdSec("dmd_" + journal.getId().toString());

        Element mods = mods("mods");
        // title
        mods.addContent(modsTitleInfo(journal.getTitle(), null));
        // recordInfo
        String isil = MCRConfiguration.instance().getString("JP.Site.ISIL", null);
        Element recordIdentifier = ZvddMetsTools
            .modsIdentifier("recordIdenitfier", null, journal.getId().toString(), isil);
        mods.addContent(mods("recordInfo").addContent(recordIdentifier));
        // language
        journal.getLanguageCode().map(ZvddMetsTools::modsLanguage).ifPresent(mods::addContent);
        // identifier
        journal.listIdenti().stream().map(text -> modsIdentifier("identifier", text.getType(), text.getText()))
            .forEach(mods::addContent);

        MdWrap mdWrap = new MdWrap(MDTYPE.MODS, mods);
        dmd.setMdWrap(mdWrap);
        return dmd;
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
        return isNewspaper(journal) ? "newspaper" : "periodical";
    }

    /**
     * Checks if the given journal is a newspaper.
     *
     * @param journal the journal to check
     * @return true if its a newspaper otherwise false
     */
    protected boolean isNewspaper(JPJournal journal) {
        return journal.isJournalType("jportal_class_00000200", "newspapers");
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
     * Returns all "year" volumes.
     *
     * <ul>
     *     <li>newspaper: volume type = "year"</li>
     *     <li>all others: volume type = "year" & the volume has a derivate, cause they are based on the
     *     {@link ZvddDerivateMetsGenerator}</li>
     * </ul>
     *
     * @param journal the journal to get the year volumes
     * @return list of volumes
     */
    protected List<JPVolume> listYearVolumes(JPJournal journal) {
        List<JPVolume> yearVolumes = listYearVolumesByType(journal);
        if (isNewspaper(journal)) {
            return yearVolumes.stream().filter(volume -> !volume.getDerivates().isEmpty()).collect(Collectors.toList());
        }
        return yearVolumes;
    }

    /**
     * Runs recursive through the given container and tries to find all volumes where the volumeType = "year".
     *
     * @param container the container to search in
     * @return list of volumes
     */
    protected List<JPVolume> listYearVolumesByType(JPContainer container) {
        List<JPVolume> yearVolumes = new ArrayList<>();
        List<JPVolume> children = container.getChildren(JPObjectType.jpvolume).stream()
            .map(JPVolume::new)
            .collect(Collectors.toList());
        for (JPVolume childVolume : children) {
            if (childVolume.isVolumeType("year")) {
                yearVolumes.add(childVolume);
                continue;
            }
            yearVolumes.addAll(listYearVolumesByType(childVolume));
        }
        return yearVolumes;
    }

}
