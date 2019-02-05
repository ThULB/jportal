package fsu.jportal.mets;

import static fsu.jportal.util.MetsUtil.MONTH_NAMES;

import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.mycore.common.MCRException;
import org.mycore.frontend.MCRFrontendUtil;
import org.mycore.mets.model.MCRMETSGenerator;
import org.mycore.mets.model.Mets;
import org.mycore.mets.model.struct.LOCTYPE;
import org.mycore.mets.model.struct.LogicalDiv;
import org.mycore.mets.model.struct.LogicalStructMap;
import org.mycore.mets.model.struct.Mptr;
import org.mycore.mets.model.struct.PhysicalStructMap;

import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.util.JPDateUtil;

/**
 * Zvdd implementation of a mets generator using this
 * <a href="http://www.zvdd.de/fileadmin/AGSDD-Redaktion/METS_Anwendungsprofil_2.0.pdf">profile</a>.
 *
 * <p>This implementation is used for all volumes (year) in newspapers.</p>
 *
 * @author Matthias Eichner
 */
public class ZvddNewspaperYearMetsGenerator implements MCRMETSGenerator {

    private JPVolume volume;

    public ZvddNewspaperYearMetsGenerator(JPVolume volume) {
        this.volume = volume;
    }

    @Override
    public Mets generate() throws MCRException {
        Mets mets = new Mets();
        mets.removeStructMap(PhysicalStructMap.TYPE);
        mets.addDmdSec(ZvddMetsTools.createDmdSec(volume, "volume"));
        mets.addAmdSec(ZvddMetsTools.createAmdSec(volume));
        mets.addStructMap(createLogicalStructMap());

        if(mets.getFileSec().getFileGroups().isEmpty()){
            mets.setFileSec(null);
        }

        if(mets.getStructLink().getSmLinks().isEmpty()){
            mets.setStructLink(null);
        }

        return mets;
    }

    protected LogicalStructMap createLogicalStructMap() {
        LogicalStructMap struct = new LogicalStructMap();

        // journal div & pointer
        JPJournal journal = volume.getJournal();
        LogicalDiv journalDiv = new LogicalDiv("log_" + journal.getId(), "newspaper", journal.getTitle());
        struct.setDivContainer(journalDiv);

        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + journal.getId().toString();
        Mptr mptr = new Mptr(href, LOCTYPE.URL);
        journalDiv.setMptr(mptr);

        // the year itself
        String publishedDate = ZvddMetsTools.getPublishedDate(volume);

        LogicalDiv yearDiv = new LogicalDiv("log_" + volume.getId(), "year", publishedDate);
        yearDiv.setDmdId("dmd_" + volume.getId());
        journalDiv.add(yearDiv);

        // get days
        List<JPVolume> days = getDays(volume);
        if (days.isEmpty()) {
            return struct;
        }

        // build month -> day -> issue hierarchy
        buildHierarchy(yearDiv, days);

        return struct;
    }

    /**
     * Returns a flat list of descendant volumes for the given volume where the published date is in the form of
     * YYYY-MM-DD. The list will be sorted in ascending order by the published date.
     *
     * @param volume the parent volume
     * @return list of descendant volumes with published date = YYYY-MM-DD in ascending order
     */
    protected List<JPVolume> getDays(JPVolume volume) {
        List<JPVolume> days = getVolumesByDay(volume);
        days.sort((v1, v2) -> {
            Temporal published1 = v1.getPublishedTemporal().orElse(null);
            Temporal published2 = v2.getPublishedTemporal().orElse(null);
            return JPDateUtil.compare(published1, published2, true);
        });
        return days;
    }

    /**
     * Builds the month -> day -> issue logical struct map hierarchy. The day level is optional and only required if
     * there are multiple issues per day.
     * 
     * @param rootDiv the root logical div
     * @param days list of sorted days
     */
    protected void buildHierarchy(LogicalDiv rootDiv, List<JPVolume> days) {
        LogicalDiv monthDiv = null;
        LogicalDiv dayDiv = null;
        for (JPVolume volume : days) {
            Optional<Temporal> publishedTemporal = volume.getPublishedTemporal();

            // month is required
            int month = publishedTemporal.map(t -> t.get(ChronoField.MONTH_OF_YEAR)).orElse(1);
            if (monthDiv == null || monthDiv.getOrder() != month) {
                dayDiv = null;
                monthDiv = new LogicalDiv("log_month_" + month, "month", MONTH_NAMES.get(month));
                monthDiv.setOrder(month);
                monthDiv.setOrderLabel(String.valueOf(month));
                rootDiv.add(monthDiv);
            }

            // day is only required if there are multiple issues on one day
            List<JPVolume> issues = volume.getChildren(JPObjectType.jpvolume).stream()
                .map(JPVolume::new)
                .collect(Collectors.toList());
            if (!issues.isEmpty()) {
                int day = publishedTemporal.map(t -> t.get(ChronoField.DAY_OF_MONTH)).orElse(1);
                if (dayDiv == null || dayDiv.getOrder() != day) {
                    dayDiv = new LogicalDiv("log_day_" + day + "_" + month, "day", String.valueOf(day));
                    dayDiv.setOrder(day);
                    dayDiv.setOrderLabel(String.valueOf(day));
                    monthDiv.add(dayDiv);
                }
                issues.stream().map(this::createIssue).forEach(dayDiv::add);
            } else {
                // volume is the issue -> should be directly added to the month -> no need for an extra day
                monthDiv.add(createIssue(volume));
            }
        }
    }

    /**
     * Creates the logical div for the given issue. Adds a mets pointer to the zvdd mets resource.
     * 
     * @param volume the volume to convert to a logical div
     * @return the new logical div
     */
    protected LogicalDiv createIssue(JPVolume volume) {
        LogicalDiv issue = new LogicalDiv("log_issue_" + volume.getId().toString(), "issue", volume.getTitle());
        String href = MCRFrontendUtil.getBaseURL() + "rsc/mets/zvdd/" + volume.getId().toString();
        Mptr mptr = new Mptr(href, LOCTYPE.URL);
        issue.setMptr(mptr);
        return issue;
    }

    /**
     * Returns a flat list of descendant volumes for the given volume where the published date is in the form of
     * YYYY-MM-DD.
     * 
     * @param volume the parent volume
     * @return list of descendant volumes with published date = YYYY-MM-DD
     */
    protected List<JPVolume> getVolumesByDay(JPVolume volume) {
        List<JPVolume> days = new ArrayList<>();
        List<JPVolume> children = volume.getChildren(JPObjectType.jpvolume).stream()
            .map(JPVolume::new)
            .collect(Collectors.toList());
        for (JPVolume childVolume : children) {
            Temporal published = childVolume.getPublishedTemporal().orElse(null);
            if (published != null && JPDateUtil.isDay(published)) {
                days.add(childVolume);
                continue;
            }
            days.addAll(getVolumesByDay(childVolume));
        }
        return days;
    }

}
