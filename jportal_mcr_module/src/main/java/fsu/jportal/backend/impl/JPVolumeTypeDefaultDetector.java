package fsu.jportal.backend.impl;

import java.time.temporal.Temporal;
import java.util.List;

import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPContainer;
import fsu.jportal.backend.JPObjectType;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.backend.JPVolume;
import fsu.jportal.backend.JPVolumeTypeDetector;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPDateUtil;

/**
 * The default jportal volume type detector.
 * 
 * @author Matthias Eichner
 */
public class JPVolumeTypeDefaultDetector implements JPVolumeTypeDetector {

    public enum JPVolumeType {

        /**
         * There journal collections in some jpjournals. So the jpjournal itself is just a container and each volume in
         * this container is a logical "journal".
         */
        journal,

        /**
         * The real physical representation of a volume. A volume ALWAYS has a derivate.
         */
        volume,

        /**
         * A logical type. Used for volumes which describe a certain year or a range of years and do not contain any
         * derivates.
         */
        year,

        /**
         * A logical type. Used for volumes which describe a certain month or a range of month and do not contain any
         * derivates.
         */
        month,

        /**
         * Used for volumes which describe a certain day. Usually a logical type which does not contain any derivates.
         * In some instances (e.g. JVB) the derivate is added to the day and not the issue.
         */
        day,

        /**
         * The volume is an issue. Depending on the journal type, the issue can contain a derivate.
         */
        issue,

        /**
         *
         */
        calendar,

        /**
         * The prognostic contained all astronomical and astrological data for the course of the year. Sun and moon
         * rising and setting times, expected solar and lunar eclipses, beginning of the seasons were important dates
         * for everyday life.
         */
        prognostic

    }

    @Override
    public String detect(JPVolume volume) {
        List<MCRObjectID> children = volume.getChildren();
        boolean hasChildren = !children.isEmpty();
        boolean hasDerivate = volume.getFirstDerivate().isPresent();
        Temporal publishedTemporal = volume.getPublishedTemporal().orElse(null);

        // volume without any children
        if (!hasChildren) {
            if (hasDerivate) {
                return JPVolumeType.volume.name();
            }
            return getByPublished(publishedTemporal);
        }

        // volume with articles
        boolean hasArticles = children.stream()
            .anyMatch(linkId -> linkId.getTypeId().equals(JPObjectType.jparticle.name()));
        if (hasArticles) {
            // if there are articles but no derivates -> its an issue
            // if there is a derivate and the volume is publishedTemporal on a day -> its an issue
            if (!hasDerivate || (publishedTemporal != null && JPDateUtil.isDay(publishedTemporal))) {
                return JPVolumeType.issue.name();
            }
            return JPVolumeType.volume.name();
        }

        // handle special type calendars
        boolean isCalendar = volume.getJournal().isJournalType("jportal_class_00000200", "calendars");
        if (isCalendar) {
            if (publishedTemporal != null) {
                return getByPublished(publishedTemporal);
            }
            if (hasDerivate) {
                boolean isPrognistic = volume.isVolContentClassis(2, "jportal_class_00000090", "prognostic");
                return isPrognistic ? JPVolumeType.prognostic.name() : JPVolumeType.calendar.name();
            }
            // unknown -> probably a site -> not sure if we should define it?
            return null;
        }

        // journal
        // -> needs a jpjournal as parent
        // -> published date should contain a from and until date
        JPContainer parent = volume.getParent().orElse(null);
        JPMetaDate published = volume.getDate(JPPeriodicalComponent.DateType.published).orElse(null);
        if (parent != null && JPComponentUtil.is(parent, JPObjectType.jpjournal) && published != null
            && published.getFrom() != null && published.getUntil() != null) {
            return JPVolumeType.journal.name();
        }

        // no more info, check by published
        return getByPublished(publishedTemporal);
    }

    private String getByPublished(Temporal published) {
        if (published == null) {
            // cannot determine this volume cause it does not contain any derivate nor its published date is set
            return null;
        }
        return JPDateUtil.isDay(published) ? JPVolumeType.day.name()
            : JPDateUtil.isMonth(published) ? JPVolumeType.month.name() : JPVolumeType.year.name();
    }

}
