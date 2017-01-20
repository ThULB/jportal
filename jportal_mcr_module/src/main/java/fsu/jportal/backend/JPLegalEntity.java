package fsu.jportal.backend;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Base class for persons and institutions.
 * 
 * @author Matthias Eichner
 */
public abstract class JPLegalEntity extends JPObjectComponent {

    public JPLegalEntity() {
        super();
    }

    public JPLegalEntity(String mcrId) {
        super(mcrId);
    }

    public JPLegalEntity(MCRObjectID mcrId) {
        super(mcrId);
    }

    public JPLegalEntity(MCRObject mcrObject) {
        super(mcrObject);
    }

    /**
     * Returns the logo url. If available the logo plain is returned,
     * if not the logo with text and otherwise null.
     * 
     * @return the logo url
     */
    public String getLogo() {
        return getLogoPlain().orElse(getLogoPlusText().orElse(null));
    }

    /**
     * Finds the logo by type.
     * 
     * @param type type of logo e.g. logoPlain.
     * @return logo if present
     */
    protected Optional<String> findLogo(String type) {
        return getText("logo", type);
    }

    /**
     * Returns the plain logo url.
     * 
     * @return logo url if present
     */
    public Optional<String> getLogoPlain() {
        return findLogo("logoPlain");
    }

    /**
     * Returns the logo with text url.
     * 
     * @return logo url if present
     */
    public Optional<String> getLogoPlusText() {
        return findLogo("logoPlusText");
    }

    /**
     * Returns the identifier by type. Valid types are 'gnd' and 'ppn'.
     * 
     * @param type type of the identifer
     * @return the identifier itself or null
     */
    public abstract Optional<String> getId(String type);

}
