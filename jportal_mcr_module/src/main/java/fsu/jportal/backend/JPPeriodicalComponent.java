package fsu.jportal.backend;

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.access.MCRAccessException;
import org.mycore.common.MCRException;
import org.mycore.datamodel.common.MCRActiveLinkException;
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

import com.ibm.icu.util.LocaleData;

import fsu.jportal.util.DerivateLinkUtil;

/**
 * Base class for jparticle, jpvolume and jpjournal.
 * 
 * @author Matthias Eichner
 */
public abstract class JPPeriodicalComponent extends JPObjectComponent {

    static Logger LOGGER = LogManager.getLogger(JPPeriodicalComponent.class);

    public static enum DateType {
        published, published_from, published_until
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
     * Sets a derivate link for this object.
     * 
     * @param derivate the derivate to set
     * @param href the image which should be linked
     * @throws MCRActiveLinkException when the link couldn't be set
     */
    public void setDerivateLink(MCRDerivate derivate, String href) throws MCRActiveLinkException, MCRAccessException {
        String pathOfImage = derivate.getId().toString() + "/" + href;
        DerivateLinkUtil.setLink(object, pathOfImage);
    }

    /**
     * Sets a derivate link for this object
     * 
     * @param link the link, this should include the derivate and the path to the file
     * @throws MCRActiveLinkException
     */
    public void setDerivateLink(String link) throws MCRActiveLinkException, MCRAccessException {
        DerivateLinkUtil.setLink(object, link);
    }

    /**
     * The derivate link in form of derivateId/pathToFile or null.
     * 
     * @return derivate link as string
     */
    public String getDerivateLink() {
        return metadataStreamNotInherited("derivateLinks", MCRMetaDerivateLink.class)
                                                                                     .map(
                                                                                         MCRMetaDerivateLink::getXLinkHref)
                                                                                     .findFirst()
                                                                                     .orElse(null);
    }

    /**
     * Sets the date for the component. If until is null, it will be ignored and only from is set.
     * If from is null, the date will be removed. From and until should be in the form of YYYY-MM-DD
     * or YYYY-MM or YYYY.
     * 
     * @param from from as string
     * @param until until as string
     */
    public void setDate(String from, String until) {
        if (from == null) {
            object.getMetadata().removeMetadataElement("dates");
            return;
        }
        MCRMetaElement dates = new MCRMetaElement(MCRMetaISO8601Date.class, "dates", true, false, null);
        String fromType = until == null ? DateType.published.name() : DateType.published_from.name();
        dates.addMetaObject(buildISODate("date", from, fromType));
        if (until != null) {
            dates.addMetaObject(buildISODate("date", until, DateType.published_until.name()));
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
     * Returns the published date of this periodical component.
     * 
     * @return optional of the published date
     */
    public Optional<LocalDate> getPublishedDate() {
        Optional<MCRMetaISO8601Date> published = getDate(DateType.published.name());
        Optional<MCRMetaISO8601Date> publishedFrom = getDate(DateType.published_from.name());
        return Optional.ofNullable(published.orElse(publishedFrom.orElse(null)))
                       .map(MCRMetaISO8601Date::getMCRISO8601Date)
                       .map(MCRISO8601Date::getDt)
                       .map(dt -> {
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
     * @param id the object identifier of the participant
     * @param title the name of the participant
     * @param type the participant type e.g. author
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
     * Returns the template name for this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the template of this journal.
     * 
     * @return the template name
     */
    public String getNameOfTemplate() {
        MCRObject journal = MCRObjectUtils.getRoot(object);
        if (!journal.getId().getTypeId().equals(JPJournal.TYPE)) {
            throw new MCRException("Unable to get template of object " + journal.getId()
                + " because its not a journal but the root ancestor of " + object.getId() + ".");
        }
        return new JPJournal(journal).getNameOfTemplate();
    }

    /**
     * Returns the journal id for this component. Each component is either a journal or
     * should have a journal as ancestor. This method returns the id of this journal.
     * By default the journal id is stored in the metadata field 'hidden_jpjournalsID'
     * and this field is inherited through the whole journal. 
     * 
     * @return the journal id
     */
    public String getJournalId() {
        return metadataStream("hidden_jpjournalsID", MCRMetaLangText.class).map(MCRMetaLangText::getText)
                                                                           .findFirst()
                                                                           .orElse(null);
    }

}
