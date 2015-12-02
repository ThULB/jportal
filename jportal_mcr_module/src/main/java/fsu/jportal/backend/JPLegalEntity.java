package fsu.jportal.backend;

import java.util.Optional;

import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Base class for persons and institutions.
 * 
 * @author Matthias Eichner
 */
public abstract class JPLegalEntity extends JPBaseComponent {

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
        return streamNotInherited("logo").filter(m -> type.equals(m.getType())).map(c -> (MCRMetaLangText) c)
            .map(MCRMetaLangText::getText).findFirst();
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
}
