package fsu.jportal.mets;

import static fsu.jportal.mets.ZvddMetsTools.getModsPart;
import static fsu.jportal.mets.ZvddMetsTools.mods;
import static fsu.jportal.mets.ZvddMetsTools.modsIdentifier;
import static fsu.jportal.mets.ZvddMetsTools.modsName;
import static fsu.jportal.mets.ZvddMetsTools.modsTitleInfo;

import org.jdom2.Element;
import org.mycore.common.MCRException;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.Mptr;

import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPPerson;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This version is compatible with the DFG viewer. But adds a mets:dmdSec for each periodical.</p>
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
            .map((periodical) -> toDmdSec(periodical, simpleMods(periodical)))
            .forEach(mets::addDmdSec);
        return mets;
    }

    @Override
    protected DmdSec createDmdSection() {
        return toDmdSec(this.rootVolume, rootMods(this.rootVolume));
    }

    @Override
    protected Mptr getMptr(JPJournal journal) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + journal.getId().toString();
        return new Mptr(href, LOCTYPE.URL);
    }

    protected void handleLogicalHierarchy(LogicalDiv volumeDiv) {
        rootVolume.getChildren().forEach(childId -> buildHierarchy(childId, volumeDiv, true));
    }

    public DmdSec toDmdSec(JPPeriodicalComponent periodical, Element mods) {
        DmdSec dmdSec = new DmdSec("dmd_" + periodical.getId().toString());
        dmdSec.setMdWrap(new MdWrap(MDTYPE.MODS, mods));
        return dmdSec;
    }

    protected Element simpleMods(JPPeriodicalComponent periodical) {
        Element mods = mods("mods");

        // identifier
        periodical.listIdenti().stream().map(text -> modsIdentifier("identifier", text.getType(), text.getText()))
            .forEach(mods::addContent);

        // title
        String title = periodical == this.rootVolume ? ZvddMetsTools.getTitle(this.rootVolume) : periodical.getTitle();
        mods.addContent(modsTitleInfo(title, null));

        // participants
        periodical.getParticipants(JPObjectType.person).stream()
            .map(personPair -> modsName(new JPPerson(personPair.getKey()), personPair.getValue()))
            .forEach(mods::addContent);
        periodical.getParticipants(JPObjectType.jpinst).stream()
            .map(jpinstPair -> modsName(new JPInstitution(jpinstPair.getKey()), jpinstPair.getValue()))
            .forEach(mods::addContent);

        // add content
        return mods;
    }

    protected Element rootMods(JPPeriodicalComponent periodical) {
        Element mods = simpleMods(periodical);
        this.derivate.getURN().ifPresent(urn -> {
            mods.addContent(0, modsIdentifier("identifier", "urn", "urn"));
        });
        JPJournal journal = this.rootVolume.getJournal();
        // related item host
        String isil = MCRConfiguration.instance().getString("JP.Site.ISIL", null);
        Element recordIdentifier = modsIdentifier("recordIdentifier", "mycore", journal.getId().toString(), isil);
        Element relatedItem = mods("relatedItem").setAttribute("type", "host").addContent(recordIdentifier);
        mods.addContent(relatedItem);
        // mods:part
        Integer order = ZvddMetsTools.calculateOrder(this.rootVolume);
        String number = this.rootVolume.getPublishedDate().map(JPMetaDate::toString).orElse(String.valueOf(order));
        Element part = getModsPart(order, "volume", number);
        mods.addContent(part);
        return mods;
    }

}
