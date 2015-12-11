package fsu.jportal.backend;

import java.util.Optional;
import java.util.stream.StreamSupport;

import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

/**
 * Abstraction of a jportal institution. Be aware that this class is not fully implemented.
 * 
 * @author Matthias Eichner
 */
public class JPInstitution extends JPLegalEntity {

    public static String TYPE = "jpinst";

    public JPInstitution() {
        super();
    }

    public JPInstitution(String mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPPerson container for the given mcrId.
     * 
     * @param mcrId a mycore object id
     */
    public JPInstitution(MCRObjectID mcrId) {
        super(mcrId);
    }

    /**
     * Creates a new JPPerson container for the given mycore object.
     * 
     * @param mcrObject the mycore object
     */
    public JPInstitution(MCRObject mcrObject) {
        super(mcrObject);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Returns the name of the institution.
     * 
     * @return name of the person
     */
    @Override
    public String getTitle() {
        return getInstitutionName();
    }

    /**
     * Returns the full name of the institution.
     * 
     * @return name of the institution.
     */
    protected String getInstitutionName() {
        MCRMetaElement names = object.getMetadata().getMetadataElement("names");
        if (names == null) {
            return null;
        }
        Optional<MCRMetaInstitutionName> name = StreamSupport.stream(names.spliterator(), false)
            .filter(m -> m.getInherited() == 0).map(c -> (MCRMetaInstitutionName) c).findFirst();
        return name.orElse(null).getFullName();
    }

    @Override
    public Optional<String> getId(String type) {
        return metadataStreamNotInherited("identifiers", MCRMetaLangText.class).filter(t -> t.getType().equals(type))
            .map(MCRMetaLangText::getText).findFirst();
    }

}
