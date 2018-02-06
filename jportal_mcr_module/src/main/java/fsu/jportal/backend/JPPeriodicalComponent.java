package fsu.jportal.backend;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fsu.jportal.util.DerivateLinkUtil;
import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRISO8601Date;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.datamodel.metadata.MCRObjectUtils;

/**
 * Base class for jparticle, jpvolume and jpjournal.
 *
 * @author Matthias Eichner
 */
public abstract class JPPeriodicalComponent extends JPObjectComponent {

    static Logger LOGGER = LogManager.getLogger(JPPeriodicalComponent.class);

    public enum DateType {
        published, published_from, published_until
    }

    public enum SubtitleType {
        title_spokenAbout, title_main, title_short, title_beside, title_rezensation, title_original, additional, misc
    }

    /**
     * Creates a new <code>MCRObject</code> based on the {@link #getType()} method.
     */
    public JPPeriodicalComponent() {
        super();
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     *
     * @param mcrId a mycore object id
     */
    public JPPeriodicalComponent(String mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     *
     * @param mcrId a mycore object id
     */
    public JPPeriodicalComponent(MCRObjectID mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPComponent container for the mycore object.
     *
     * @param mcrObject the mycore object
     */
    public JPPeriodicalComponent(MCRObject mcrObject) {
        super(mcrObject);
    }

    /**
     * Returns the parent of this periodical component.
     *
     * @return optional parent
     */
    public Optional<JPPeriodicalComponent> getParent() {
        MCRObjectID parent = getObject().getParent();
        if(parent == null) {
            return Optional.empty();
        }
        return JPComponentUtil.getPeriodical(parent);
    }

    @Override
    public String getTitle() {
        Optional<MCRMetaLangText> maintitle = getMaintitle();
        if (maintitle.isPresent()) {
            return maintitle.get().getText();
        }
        return null;
    }

    /**
     * Sets the main title for this component.
     *
     * @param newTitle the new main title
     */
    public void setTitle(String newTitle) {
        Optional<MCRMetaLangText> maintitle = getMaintitle();
        if (maintitle.isPresent()) {
            maintitle.get().setText(newTitle);
        } else {
            MCRMetaElement maintitles = new MCRMetaElement(MCRMetaLangText.class, "maintitles", true, false, null);
            maintitles.addMetaObject(new MCRMetaLangText("maintitle", null, null, 0, null, newTitle));
            object.getMetadata().setMetadataElement(maintitles);
        }
    }

    /**
     * Returns the correct main title as <code>MCRMetaLangText</code> for this component.
     *
     * @return the main title
     */
    protected Optional<MCRMetaLangText> getMaintitle() {
        return metadataStreamNotInherited("maintitles", MCRMetaLangText.class).findFirst();
    }

    /**
     * Returns a list of all subtitles.
     *
     * @return list of subtitles
     */
    public List<MCRMetaLangText> getSubtitles() {
        return metadataStreamNotInherited("subtitles", MCRMetaLangText.class)
                .collect(Collectors.toList());
    }

    /**
     * Returns the subtitle of the given type.
     *
     * @param type the type of the subtitle
     * @return the subtitle
     */
    public Optional<String> getSubtitle(SubtitleType type) {
        String subType = type.name().equals("title_short") ? "short" : type.name();
        return getText("subtitles", subType);
    }

    /**
     * Sets a derivate link for this object.
     *
     * @param derivate the derivate to set
     * @param href     the image which should be linked
     */
    public void setDerivateLink(MCRDerivate derivate, String href) throws MCRAccessException {
        String pathOfImage = derivate.getId().toString() + "/" + href;
        DerivateLinkUtil.setLink(object, pathOfImage);
    }

    /**
     * Sets a derivate link for this object. The link should look like
     * <b>jportal_derivate_xxxxxxxx/path_to_image</b>.
     *
     * @param link the link, this should include the derivate and the path to the file
     * @throws MCRPersistenceException cannot set link due I/O error
     * @throws MCRAccessException if the write permission is missing
     */
    public void setDerivateLink(String link) throws MCRPersistenceException, MCRAccessException {
        DerivateLinkUtil.setLink(object, link);
    }

    /**
     * The derivate link in form of derivateId/pathToFile or null.
     *
     * @return derivate link as string
     */
    public String getDerivateLink() {
        Stream<MCRMetaDerivateLink> stream = metadataStreamNotInherited("derivateLinks", MCRMetaDerivateLink.class);
        return stream.map(MCRMetaDerivateLink::getXLinkHref).findFirst().orElse(null);
    }

    /**
     * Sets the date for the component. If until is null, it will be ignored and only from is set.
     * If from is null, the date will be removed. From and until should be in the form of YYYY-MM-DD
     * or YYYY-MM or YYYY.
     *
     * @param from  from as string
     * @param until until as string
     */
    public void setDate(String from, String until) {
        if (from == null) {
            object.getMetadata().removeMetadataElement("dates");
            return;
        }
        MCRMetaElement dates = new MCRMetaElement(MCRMetaISO8601Date.class, "dates", true, false, null);
        String fromType = until == null ? DateType.published.name() : DateType.published_from.name();
        MCRMetaISO8601Date fromIsoDate = buildISODate("date", from, fromType);
        if (fromIsoDate == null) {
            LOGGER.error("Unable to add published date cause '" + from + "' couldn't be parsed.");
            return;
        }
        dates.addMetaObject(fromIsoDate);
        if (until != null) {
            MCRMetaISO8601Date untilIsoDate = buildISODate("date", until, DateType.published_until.name());
            if (untilIsoDate != null) {
                dates.addMetaObject(untilIsoDate);
            }
        }
        object.getMetadata().setMetadataElement(dates);
    }

    /**
     * Return all dates of this component and all inherited from parents.
     *
     * @return list of dates.
     */
    public List<MCRMetaISO8601Date> getDates() {
        return metadataStream("dates", MCRMetaISO8601Date.class).collect(Collectors.toList());
    }

    /**
     * Returns the first not inherited date based on the given type.
     *
     * @param type the type of the date. e.g. published
     * @return the date object
     */
    public Optional<MCRMetaISO8601Date> getDate(String type) {
        return metadataStreamNotInherited("dates", MCRMetaISO8601Date.class).filter(d -> {
            if (d.getType() == null) {
                LOGGER.error("Invalid dates/date metadata at '" + getObject().getId() + "'. Missing type attribute.");
                return false;
            }
            return d.getType().equals(type);
        }).findFirst();
    }

    /**
     * Returns the {@link TemporalAccessor} for the published date.
     *
     * @return optional of the temporal accessor
     */
    public Optional<TemporalAccessor> getPublishedTemporalAccessor() {
        Optional<MCRMetaISO8601Date> published = getDate(DateType.published.name());
        Optional<MCRMetaISO8601Date> publishedFrom = getDate(DateType.published_from.name());
        return Optional.ofNullable(published.orElse(publishedFrom.orElse(null)))
                .map(MCRMetaISO8601Date::getMCRISO8601Date)
                .map(MCRISO8601Date::getDt);
    }

    /**
     * Returns the published date of this periodical component. Be aware that this local date
     * is not necessarily the original published temporal accessor.
     * See {@link #buildLocalDate(TemporalAccessor)} for more information.
     *
     * @return optional of the published date
     */
    public Optional<LocalDate> getPublishedDate() {
        return getPublishedTemporalAccessor().map(dt -> {
            try {
                return LocalDate.from(dt);
            } catch (Exception exc) {
                return buildLocalDate(dt);
            }
        });
    }

    /**
     * Helper method to build a {@link LocalDate} out of a {@link TemporalAccessor}.
     * If the month or day are not present, they are set to 1.
     *
     * @param dt the temporal accessor
     * @return a local date
     */
    private LocalDate buildLocalDate(TemporalAccessor dt) {
        if (!dt.isSupported(ChronoField.YEAR)) {
            return null;
        }
        int year = dt.get(ChronoField.YEAR);
        int month = dt.isSupported(ChronoField.MONTH_OF_YEAR) ? dt.get(ChronoField.MONTH_OF_YEAR) : 1;
        int day = dt.isSupported(ChronoField.DAY_OF_MONTH) ? dt.get(ChronoField.DAY_OF_MONTH) : 1;
        return LocalDate.of(year, month, day);
    }

    /**
     * Adds a new participant to this object.
     *
     * @param id    the object identifier of the participant
     * @param title the name of the participant
     * @param type  the participant type e.g. author
     */
    public void addParticipant(MCRObjectID id, String title, String type) {
        MCRMetaElement participants = object.getMetadata().getMetadataElement("participants");
        if (participants == null) {
            participants = new MCRMetaElement(MCRMetaLinkID.class, "participants", false, false, null);
            object.getMetadata().setMetadataElement(participants);
        }
        MCRMetaLinkID link = new MCRMetaLinkID("participant", id, null, title);
        link.setType(type);
        participants.addMetaObject(link);
    }

    /**
     * Returns a list of all participants.
     *
     * @return list of participants
     */
    public List<MCRMetaLinkID> getParticipants() {
        return metadataStream("participants", MCRMetaLinkID.class).collect(Collectors.toList());
    }

    /**
     * Returns a list of participants of the given type.
     *
     * @param role the role, e.g. author
     * @return list of participants
     */
    public List<MCRObjectID> getParticipants(String role) {
        return getLinks("participants", role);
    }

    /**
     * List all participants of the given object type (only person or jpinst is allowed).
     *
     * @param objectType person or jpinst
     * @return list of participants
     */
    public List<MCRObjectID> getParticipants(JPObjectType objectType) {
        if (!(objectType.equals(JPObjectType.person) || objectType.equals(JPObjectType.jpinst))) {
            throw new IllegalArgumentException("Invalid object type for participants " + objectType.name() +
                    ". Only person or jpinst is allowed.");
        }
        return metadataStreamNotInherited("participants", MCRMetaLinkID.class)
                .filter(link -> link.getXLinkHrefID().getTypeId().equals(objectType.name()))
                .map(MCRMetaLinkID::getXLinkHrefID)
                .collect(Collectors.toList());
    }

    /**
     * List all participants of the given object type (only person or jpinst is allowed)
     * and role.
     *
     * @param objectType person or institution
     * @param role the role, e.g. author
     * @return list of participants
     */
    public List<MCRObjectID> getParticipants(JPObjectType objectType, String role) {
        if (!(objectType.equals(JPObjectType.person) || objectType.equals(JPObjectType.jpinst))) {
            throw new IllegalArgumentException("Invalid object type for participants " + objectType.name() +
                    ". Only person or jpinst is allowed.");
        }
        return metadataStreamNotInherited("participants", MCRMetaLinkID.class)
                .filter(link -> link.getXLinkHrefID().getTypeId().equals(objectType.name()))
                .filter(typeFilter(role))
                .map(MCRMetaLinkID::getXLinkHrefID)
                .collect(Collectors.toList());
    }

    /**
     * Returns the first found legal entity of the given type and role.
     *
     * @param objectType person or institution
     * @param role the role, e.g. author
     * @return the first found participant
     */
    public Optional<JPLegalEntity> getParticipant(JPObjectType objectType, String role) {
        return getParticipants(objectType, role).stream().findFirst().flatMap(JPComponentUtil::getLegalEntity);
    }

    /**
     * Returns the person or institution who created this component. This is usually the author or publisher.
     *
     * @return optional legal entity
     */
    public abstract Optional<JPLegalEntity> getCreator();

    /**
     * Get the identifier of the given type.
     *
     * @param type the id type, e.g. doi, isbn, issn...
     * @return optional of the ID
     */
    public Optional<String> getIdenti(String type) {
        return getText("identis", type);
    }

    /**
     * Sets the identifier for the given type.
     *
     * @param type the id type, e.g. doi, isbn, issn...
     * @param id the new id
     */
    public void setIdenti(String type, String id) {
        setText("identis", "identi", id, type, false, true);
    }

    /**
     * Returns the journal of this component. Each component is either a journal or should have a journal as ancestor.
     *
     * @return the journal
     */
    public JPJournal getJournal() {
        MCRObject journal = MCRObjectUtils.getRoot(object);
        if (!journal.getId().getTypeId().equals(JPJournal.TYPE)) {
            throw new MCRException("Unable to get template of object " + journal.getId()
                    + " because its not a journal but the root ancestor of " + object.getId() + ".");
        }
        return new JPJournal(journal);
    }

    /**
     * Returns the template name for this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the template of this journal.
     *
     * @return the template name
     */
    public String getNameOfTemplate() {
        return getJournal().getNameOfTemplate();
    }

    /**
     * Returns the journal id for this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the id of this journal.
     * By default the journal id is stored in the metadata field 'hidden_jpjournalsID'
     * and this field is inherited through the whole journal.
     *
     * @return the journal id or null
     */
    public String getJournalIdAsString() {
        Stream<MCRMetaLangText> stream = metadataStream("hidden_jpjournalsID", MCRMetaLangText.class);
        return stream.map(MCRMetaLangText::getText)
                .findFirst()
                .orElse(null);
    }

    /**
     * Returns the ISO 639-1 language code of this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the language code of this journal.
     *
     * @return the language code
     */
    public String getLanguageCode() {
        MCRObject journal = MCRObjectUtils.getRoot(object);
        if (!journal.getId().getTypeId().equals(JPJournal.TYPE)) {
            throw new MCRException("Unable to get language code of object " + journal.getId()
                    + " because its not a journal but the root ancestor of " + object.getId() + ".");
        }
        return new JPJournal(journal).getLanguageCode();
    }

    /**
     * Returns the journal id for this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the id of this journal.
     * By default the journal id is stored in the metadata field 'hidden_jpjournalsID'
     * and this field is inherited through the whole journal.
     *
     * @return optional of the journalId
     */
    public Optional<MCRObjectID> getJournalId() {
        return Optional.ofNullable(getJournalIdAsString()).map(MCRObjectID::getInstance);
    }

}
