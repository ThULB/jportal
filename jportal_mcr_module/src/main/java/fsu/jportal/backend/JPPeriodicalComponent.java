package fsu.jportal.backend;

import java.time.temporal.Temporal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fsu.jportal.util.DerivateLinkUtil;
import fsu.jportal.util.JPComponentUtil;
import fsu.jportal.util.JPDateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.metadata.JPMetaDate;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
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
        published, reviewedWork, reportingPeriod
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
        if (parent == null) {
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
            MCRMetaElement maintitles = new MCRMetaElement(MCRMetaLangText.class, "maintitles", false, true, null);
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
        return metadataStreamNotInherited("subtitles", MCRMetaLangText.class).collect(Collectors.toList());
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
     * Sets a specific date.
     *
     * @param dateString the date to set
     * @param type the type of the date e.g. published or reviewedWork
     */
    public void setDate(String dateString, String type) {
        JPMetaDate jpDate = getOrCreateDate(type);
        jpDate.setFrom(null);
        jpDate.setUntil(null);
        jpDate.setDate(JPDateUtil.parse(dateString));
    }

    /**
     * Sets a date range.
     *
     * @param fromString date range start
     * @param untilString date range end
     * @param type the type of the date e.g. published or reviewedWork
     */
    public void setDate(String fromString, String untilString, String type) {
        JPMetaDate jpDate = getOrCreateDate(type);
        jpDate.setFrom(JPDateUtil.parse(fromString));
        jpDate.setUntil(JPDateUtil.parse(untilString));
        jpDate.setDate(null);
    }

    /**
     * Creates a jpdate with the given type if does not exists yet.
     *
     * @param type the type of the date e.g. published or reviewedWork
     * @return the date
     */
    protected JPMetaDate getOrCreateDate(String type) {
        MCRMetaElement dates = object.getMetadata().getMetadataElement("dates");
        if (dates == null) {
            dates = new MCRMetaElement(JPMetaDate.class, "dates", false, false, null);
            object.getMetadata().setMetadataElement(dates);
        }
        Optional<JPMetaDate> oldDate = getDate(type);
        JPMetaDate jpDate;
        if(oldDate.isPresent()) {
            jpDate = oldDate.get();
        } else {
            jpDate = new JPMetaDate("date", type, 0);
            dates.addMetaObject(jpDate);
        }
        return jpDate;
    }

    /**
     * Removes the specific date.
     *
     * @param type type of the date to be removed e.g. published
     */
    public void removeDate(String type) {
        object.getMetadata().findFirst("dates", type, 0).ifPresent((date) -> {
            object.getMetadata().getMetadataElement("dates").removeMetaObject(date);
        });
    }

    /**
     * Return all dates of this component and all inherited from parents.
     *
     * @return list of dates.
     */
    public List<JPMetaDate> getDates() {
        return metadataStream("dates", JPMetaDate.class).collect(Collectors.toList());
    }

    /**
     * Returns the first not inherited date based on the given type.
     *
     * @param type the type of the date. e.g. published
     * @return the date object
     */
    public Optional<JPMetaDate> getDate(DateType type) {
        return getDate(type.name());
    }

    /**
     * Returns the first not inherited date based on the given type.
     *
     * @param type the type of the date. e.g. published
     * @return the date object
     */
    public Optional<JPMetaDate> getDate(String type) {
        return metadataStreamNotInherited("dates", JPMetaDate.class).filter(date -> {
            if (date.getType() == null) {
                LOGGER.error("Invalid dates/date metadata at '" + getObject().getId() + "'. Missing type attribute.");
                return false;
            }
            return date.getType().equals(type);
        }).findFirst();
    }

    /**
     * Returns the {@link Temporal} for the published date of this object. Be aware that this method does not go through
     * the ancestors to find the most likley published date.
     *
     * @return optional of the temporal accessor
     */
    public Optional<Temporal> getPublishedTemporal() {
        Optional<JPMetaDate> published = getDate(DateType.published.name());
        return published.map(JPMetaDate::getDateOrFrom);
    }

    /**
     * Returns the published date. This method will go through all the ancestors if this component does not have an
     * own published date to return the closest published date available.
     *
     * @return the published date
     */
    public Optional<JPMetaDate> getPublishedDate() {
        Optional<JPMetaDate> published = getDate(DateType.published.name());
        if(published.isPresent()) {
            return published;
        }
        return getParent().flatMap(JPPeriodicalComponent::getPublishedDate);
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
            throw new IllegalArgumentException("Invalid object type for participants " + objectType.name()
                    + ". Only person or jpinst is allowed.");
        }
        return metadataStreamNotInherited("participants", MCRMetaLinkID.class)
                .filter(link -> link.getXLinkHrefID().getTypeId().equals(objectType.name()))
                .map(MCRMetaLinkID::getXLinkHrefID).collect(Collectors.toList());
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
            throw new IllegalArgumentException("Invalid object type for participants " + objectType.name()
                    + ". Only person or jpinst is allowed.");
        }
        return metadataStreamNotInherited("participants", MCRMetaLinkID.class)
                .filter(link -> link.getXLinkHrefID().getTypeId().equals(objectType.name())).filter(typeFilter(role))
                .map(MCRMetaLinkID::getXLinkHrefID).collect(Collectors.toList());
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
        return stream.map(MCRMetaLangText::getText).findFirst().orElse(null);
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
