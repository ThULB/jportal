package fsu.jportal.backend;

import java.util.Optional;
import java.util.stream.StreamSupport;

import org.mycore.common.MCRPersistenceException;
import org.mycore.datamodel.common.MCRActiveLinkException;
import org.mycore.datamodel.metadata.MCRDerivate;
import org.mycore.datamodel.metadata.MCRMetaDerivateLink;
import org.mycore.datamodel.metadata.MCRMetaElement;
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
public abstract class JPPeriodicalComponent implements JPComponent {

    protected MCRObject object;

    /**
     * Creates a new <code>MCRObject</code> based on the {@link #getType()} method.
     */
    public JPPeriodicalComponent() {
        object = new MCRObject();
        object.setId(MCRObjectID.getNextFreeId("jportal_" + getType()));
        object.setSchema("datamodel-" + getType() + ".xsd");
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPPeriodicalComponent(String mcrId) {
        this(MCRObjectID.getInstance(mcrId));
    }

    /**
     * Creates a new JPComponent container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPPeriodicalComponent(MCRObjectID mcrId) {
        this(MCRMetadataManager.retrieveMCRObject(mcrId));
    }

    /**
     * Creates a new JPComponent container for the mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPPeriodicalComponent(MCRObject mcrObject) {
        if (!mcrObject.getId().getTypeId().equals(getType())) {
            throw new IllegalArgumentException("Object " + mcrObject.getId() + " is not a " + getType());
        }
        this.object = mcrObject;
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

    @Override
    public MCRObject getObject() {
        return object;
    }

    @Override
    public void store() throws MCRPersistenceException, MCRActiveLinkException {
        MCRMetadataManager.update(object);
    }

    /**
     * Returns the type of the component. One of jpjournal, jpvolume or jparticle should be returned.
     * 
     * @return the tpye of the component
     */
    public abstract String getType();

}
