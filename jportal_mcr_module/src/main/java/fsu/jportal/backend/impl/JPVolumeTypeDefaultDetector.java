package fsu.jportal.backend.impl;

import static fsu.jportal.util.MetsUtil.MONTH_NAMES;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        boolean hasDerivate = volume.getFirstDerivate().isPresent();
        Temporal publishedTemporal = volume.getPublishedTemporal().orElse(null);

        // handle special type calendars
        boolean isCalendar = volume.getJournal().isJournalType("jportal_class_00000200", "calendars");
        if (isCalendar) {
            Optional<String> publishedType = getByPublished(publishedTemporal);
            if (publishedType.isPresent()) {
                return publishedType.get();
            }
            if (hasDerivate) {
                boolean isPrognistic = volume.isVolContentClassis(2, "jportal_class_00000090", "prognostic");
                return isPrognistic ? JPVolumeType.prognostic.name() : JPVolumeType.calendar.name();
            }
            // unknown -> probably a site -> not sure if we should define it?
            return null;
        }

        List<JPVolume> childVolumes = children.stream()
            .filter(linkId -> linkId.getTypeId().equals(JPObjectType.jpvolume.name()))
            .map(JPVolume::new)
            .collect(Collectors.toList());
        boolean hasVolumes = !childVolumes.isEmpty();

        // handle volume, day and issue
        boolean hasChildren = !children.isEmpty();
        boolean hasArticles = children.stream()
            .anyMatch(linkId -> linkId.getTypeId().equals(JPObjectType.jparticle.name()));
        if (!hasChildren || hasArticles || hasDerivate) {
            boolean isDay = publishedTemporal != null && JPDateUtil.isDay(publishedTemporal);
            if (!hasDerivate || isDay) {
                return hasVolumes ? JPVolumeType.day.name() : JPVolumeType.issue.name();
            }
            return JPVolumeType.volume.name();
        }

        // journal
        // -> needs a jpjournal as parent
        // -> published date should contain a from and until date OR the child volumes are years
        JPContainer parent = volume.getParent().orElse(null);
        JPMetaDate published = volume.getDate(JPPeriodicalComponent.DateType.published).orElse(null);
        boolean hasParent = parent != null;
        boolean hasFromAndUntil = hasParent && JPComponentUtil.is(parent, JPObjectType.jpjournal) && published != null
            && published.getFrom() != null && published.getUntil() != null;
        boolean hasYears = childVolumes.stream()
            .map(JPVolume::getPublishedTemporal)
            .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
            .anyMatch(JPDateUtil::isYear);
        if (hasParent && (hasFromAndUntil || hasYears)) {
            return JPVolumeType.journal.name();
        }

        // no more info, check by published or by title
        return getByPublished(publishedTemporal).orElse(getByTitle(volume.getTitle()).orElse(null));
    }

    private Optional<String> getByTitle(String title) {
        if (MONTH_NAMES.containsValue(title)) {
            return Optional.of(JPVolumeType.month.name());
        }
        return Optional.empty();
    }

    private Optional<String> getByPublished(Temporal published) {
        if (published == null) {
            return Optional.empty();
        }
        return Optional.of(JPDateUtil.isDay(published) ? JPVolumeType.day.name()
            : JPDateUtil.isMonth(published) ? JPVolumeType.month.name() : JPVolumeType.year.name());
    }

}
