package fsu.jportal.backend.impl;

import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import static fsu.jportal.util.MetsUtil.MONTH_NAMES;

/**
 * The default jportal volume type detector.
 * 
 * @author Matthias Eichner
 */
public class JPVolumeTypeDefaultDetector implements JPVolumeTypeDetector {

    public enum JPVolumeType {

        /**
         * There are journal collections in some jpjournals. So the jpjournal itself is just a container and each volume
         * in this container is a logical "journal".
         */
        journal,

        /**
         * Used for volumes which describe a certain year or a range of years.
         */
        year,

        /**
         * Used for volumes which describe a certain month or a range of month.
         */
        month,

        /**
         * Used for volumes which describe a certain day.
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

    protected List<String> single(String type) {
        return type != null ? Collections.singletonList(type) : new ArrayList<>();
    }

    @Override
    public List<String> detect(JPVolume volume) {
        List<MCRObjectID> children = volume.getChildren();
        Temporal publishedTemporal = volume.getPublishedTemporal().orElse(null);

        // handle special type calendars
        boolean isCalendar = volume.getJournal().isJournalType("jportal_class_00000200", "calendars");
        if (isCalendar) {
            Optional<String> publishedType = getByPublished(publishedTemporal);
            if (publishedType.isPresent()) {
                return single(publishedType.get());
            }
            boolean hasDerivate = volume.getFirstDerivate().isPresent();
            if (hasDerivate) {
                boolean isPrognistic = volume.isVolContentClassis(2, "jportal_class_00000090", "prognostic");
                return isPrognistic ? single(JPVolumeType.prognostic.name()) : single(JPVolumeType.calendar.name());
            }
            // unknown -> probably a site -> not sure if we should define it?
            return null;
        }

        // issue and year at the same time
        boolean hasArticles = children.stream()
            .anyMatch(linkId -> linkId.getTypeId().equals(JPObjectType.jparticle.name()));
        JPContainer parent = volume.getParent().orElse(null);
        boolean hasJournalParent = parent != null && JPComponentUtil.is(parent, JPObjectType.jpjournal);
        boolean hasPublishedDate = publishedTemporal != null;
        boolean isYear = hasPublishedDate && JPDateUtil.isYear(publishedTemporal);
        if (hasArticles && hasJournalParent && isYear) {
            return Arrays.asList(JPVolumeType.issue.name(), JPVolumeType.year.name());
        }

        // just the issue issue
        boolean hasChildren = !children.isEmpty();
        boolean isDay = hasPublishedDate && JPDateUtil.isDay(publishedTemporal);
        if (hasArticles || (!hasChildren && (!hasPublishedDate || isDay))) {
            return single(JPVolumeType.issue.name());
        }

        // journal
        // -> needs a jpjournal as parent
        // -> published date should contain a from and until date OR the child volumes are years
        // -> it NEVER has a derivate
        JPMetaDate published = volume.getDate(JPPeriodicalComponent.DateType.published).orElse(null);
        boolean hasFromAndUntil = hasJournalParent && JPComponentUtil.is(parent, JPObjectType.jpjournal)
            && published != null
            && published.getFrom() != null && published.getUntil() != null;
        List<JPVolume> childVolumes = children.stream()
            .filter(linkId -> linkId.getTypeId().equals(JPObjectType.jpvolume.name()))
            .map(JPVolume::new)
            .collect(Collectors.toList());
        boolean hasYears = childVolumes.stream()
            .map(JPVolume::getPublishedTemporal)
            .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
            .anyMatch(JPDateUtil::isYear);
        boolean hasDerivate = volume.getFirstDerivate().isPresent();
        if (!hasDerivate && hasJournalParent && (hasFromAndUntil || hasYears)) {
            return single(JPVolumeType.journal.name());
        }

        // no more info, check by published or by title
        return single(getByPublished(publishedTemporal).orElse(getByTitle(volume.getTitle()).orElse(null)));
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
