package fsu.jportal.mets;

import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.AmdSec;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.Mptr;

import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import static fsu.jportal.mets.ZvddMetsTools.modsDefault;
import static fsu.jportal.mets.ZvddMetsTools.modsYearOrIssue;
import fsu.jportal.util.JPComponentUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This implementation supports all volumes for a specific year containing a derivate. So it should work with magazines,
 * address books and parliament documents. It does not work for newspapers or calendars cause its based on the
 * derivate!</p>
 * 
 * <p>Be aware that this is the second level mets (the year), to get the first level (journal) see
 * {@link ZvddJournalMetsGenerator}.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddDerivateMetsGenerator extends DfgViewerMetsGenerator {

    @Override
    public synchronized Mets generate() throws MCRException {
        Mets mets = super.generate();
        this.periodicalMap.values().stream()
            .filter(periodical -> !periodical.getId().equals(this.rootVolume.getId()))
            .filter(this::hasDmdId)
            .map((periodical) -> toDmdSec(periodical, modsDefault(periodical, periodical.getTitle())))
            .forEach(mets::addDmdSec);
        return mets;
    }

    @Override
    protected DmdSec createDmdSection() {
        return toDmdSec(this.rootVolume, modsYearOrIssue(this.rootVolume, this.derivate, "volume"));
    }

    @Override
    protected AmdSec createAmdSection() {
        return ZvddMetsTools.createAmdSec(this.rootVolume);
    }

    @Override
    protected Mptr getMptr(JPJournal journal) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + journal.getId().toString();
        return new Mptr(href, LOCTYPE.URL);
    }

    protected void handleLogicalHierarchy(LogicalDiv volumeDiv) {
        rootVolume.getChildren().forEach(childId -> buildHierarchy(childId, volumeDiv));
    }

    @Override
    protected boolean hasDmdId(JPPeriodicalComponent periodical) {
        if (JPComponentUtil.is(periodical, JPObjectType.jpvolume)) {
            JPVolume volume = (JPVolume) periodical;
            return !volume.getType().equals("issue");
        }
        return true;
    }

    public DmdSec toDmdSec(JPPeriodicalComponent periodical, Element mods) {
        DmdSec dmdSec = new DmdSec("dmd_" + periodical.getId().toString());
        dmdSec.setMdWrap(new MdWrap(MDTYPE.MODS, mods));
        return dmdSec;
    }

}
