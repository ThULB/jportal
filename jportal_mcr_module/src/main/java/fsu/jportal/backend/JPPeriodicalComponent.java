package fsu.jportal.backend;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaISO8601Date;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.util.DerivateLinkUtil;

/**
 * Base class for jparticle, jpvolume and jpjournal.
 * 
 * @author Matthias Eichner
 */
public abstract class JPPeriodicalComponent extends JPBaseComponent {

    public static enum DateType {
        published, published_from, published_until
    }

    protected MCRObject object;

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
     * Returns the correct main title as <code>MCRMetaInterface</code> for this component.
     * 
     * @return the main title
     */
    protected Optional<MCRMetaLangText> getMaintitle() {
        MCRMetaElement maintitles = object.getMetadata().getMetadataElement("maintitles");
        if (maintitles == null) {
            return Optional.empty();
        }
        return StreamSupport.stream(maintitles.spliterator(), false).filter(m -> m.getInherited() == 0)
            .map(c -> (MCRMetaLangText) c).findFirst();
    }

    /**
     * Sets a derivate link for this object.
     * 
     * @param derivate the derivate to set
     * @param href the image which should be linked
     * @throws MCRActiveLinkException when the link couldn't be set
     */
    public void setDerivateLink(MCRDerivate derivate, String href) throws MCRActiveLinkException {
        String pathOfImage = derivate.getId().toString() + "/" + href;
        DerivateLinkUtil.setLink(object, pathOfImage);
    }

    /**
     * Sets a derivate link for this object
     * 
     * @param link the link, this should include the derivate and the path to the file
     * @throws MCRActiveLinkException
     */
    public void setDerivateLink(String link) throws MCRActiveLinkException {
        DerivateLinkUtil.setLink(object, link);
    }

    /**
     * The derivate link in form of derivateId/pathToFile or null.
     * 
     * @return derivate link as string
     */
    public String getDerivateLink() {
        MCRMetaElement derivateLinks = object.getMetadata().getMetadataElement("derivateLinks");
        if (derivateLinks == null) {
            return null;
        }
        MCRMetaDerivateLink derivateLink = (MCRMetaDerivateLink) derivateLinks.getElementByName("derivateLink");
        return derivateLink.getXLinkHref();
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
        dates.addMetaObject(buildISODate(from, fromType));
        if (until != null) {
            dates.addMetaObject(buildISODate(until, DateType.published_until.name()));
        }
        object.getMetadata().setMetadataElement(dates);
    }

    /**
     * Builds an <code>MCRMetaISO8601Date</code> based on the date and a type.
     * 
     * @param dateString the date, should be in form of YYYY-MM-DD, YYYY-MM or YYYY.
     * @param type the type of the date (e.g. published)
     * @return an {@link MCRMetaISO8601Date}
     */
    protected MCRMetaISO8601Date buildISODate(String dateString, String type) {
        MCRMetaISO8601Date isoDate = new MCRMetaISO8601Date();
        isoDate.setSubTag("date");
        isoDate.setType(type);
        isoDate.setDate(dateString);
        return isoDate;
    }

    /**
     * Return all dates of this component and all inherited from parents.
     * 
     * @return list of dates.
     */
    public List<MCRMetaISO8601Date> getDates() {
        MCRMetaElement dates = object.getMetadata().getMetadataElement("dates");
        List<MCRMetaISO8601Date> dateList = new ArrayList<>();
        if (dates == null) {
            return dateList;
        }
        for (MCRMetaInterface date : dates) {
            if (date instanceof MCRMetaISO8601Date) {
                dateList.add((MCRMetaISO8601Date) date);
            }
        }
        return dateList;
    }

}
