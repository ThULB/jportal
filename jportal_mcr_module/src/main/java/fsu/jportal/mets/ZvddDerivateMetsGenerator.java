package fsu.jportal.mets;

import org.jdom2.Element;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.sections.DmdSec;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.MDTYPE;
import org.mycore.mets.model.struct.MdWrap;
import org.mycore.mets.model.struct.Mptr;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.util.JPComponentUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This version is compatible with the DFG viewer. But adds a mets:dmdSec for each periodical.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddDerivateMetsGenerator extends DfgViewerMetsGenerator {

    @Override
    public synchronized Mets generate() throws MCRException {
        Mets mets = super.generate();
        this.periodicalMap.values().stream()
            .filter(periodical -> !periodical.getId().equals(this.rootPeriodical.getId()))
            .map((periodical) -> toDmdSec(periodical, simpleMods(periodical)))
            .forEach(mets::addDmdSec);
        return mets;
    }

    @Override
    protected DmdSec createDmdSection() {
        return toDmdSec(this.rootPeriodical, rootMods(this.rootPeriodical));
    }

    @Override
    protected Mptr getMptr(JPJournal journal) {
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + journal.getId().toString();
        return new Mptr(href, LOCTYPE.URL);
    }

    protected void handleLogicalHierarchy(LogicalDiv volumeDiv) {
        if (rootPeriodical instanceof JPContainer) {
            ((JPContainer) rootPeriodical).getChildren()
                .forEach(childId -> buildHierarchy(childId, volumeDiv, true));
        }
    }

    public DmdSec toDmdSec(JPPeriodicalComponent periodical, Element mods) {
        DmdSec dmdSec = new DmdSec("dmd_" + periodical.getId().toString());
        dmdSec.setMdWrap(new MdWrap(MDTYPE.MODS, mods));
        return dmdSec;
    }

    protected Element simpleMods(JPPeriodicalComponent periodical) {
        Element mods = mods("mods");

        // identifier
        Element mcrIdentifier = modsIdentifier("identifier", "mycore", periodical.getId().toString());
        mods.addContent(mcrIdentifier);
        periodical.listIdenti().stream().map(text -> modsIdentifier("identifier", text.getType(), text.getText()))
            .forEach(mods::addContent);

        // title
        Element title = mods("title");
        title.setText(periodical.getTitle());
        Element titleInfo = mods("titleInfo").addContent(title);
        mods.addContent(titleInfo);

        // add content
        return mods;
    }

    protected Element rootMods(JPPeriodicalComponent periodical) {
        Element mods = simpleMods(periodical);
        this.derivate.getURN().ifPresent(urn -> {
            mods.addContent(0, modsIdentifier("identifier", "urn", "urn"));
        });
        if (!JPComponentUtil.is(this.rootPeriodical, JPObjectType.jpjournal)) {
            JPJournal journal = this.rootPeriodical.getJournal();
            // related item host
            Element recordIdentifier = modsIdentifier("recordIdentifier", "mycore", journal.getId().toString());
            Element relatedItem = mods("relatedItem").setAttribute("type", "host").addContent(recordIdentifier);
            mods.addContent(relatedItem);
        }
        return mods;
    }

    protected Element mods(String name) {
        return new Element(name, MCRConstants.MODS_NAMESPACE);
    }

    private Element modsIdentifier(String name, String type, String value) {
        Element identifier = mods(name);
        identifier.setAttribute("type", type);
        identifier.setText(value);
        return identifier;
    }

}
