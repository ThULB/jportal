package fsu.jportal.backend;

import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import java.util.Optional;
import java.util.stream.StreamSupport;

/**
 * Abstraction of a jportal institution. Be aware that this class is not fully implemented.
 *
 * @author Matthias Eichner
 */
public class JPInstitution extends JPLegalEntity {

    public static String TYPE = JPObjectType.jpinst.name();

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
        return Optional.ofNullable(object.getMetadata().getMetadataElement("names"))
                       .map(_names -> StreamSupport.stream(_names.spliterator(), false))
                       .flatMap(
                               stream -> stream.filter(_names -> _names.getInherited() == 0)
                                               .map(c -> (MCRMetaInstitutionName) c)
                                               .findFirst()
                       ).orElse(null).getFullName();
    }

    @Override
    public Optional<String> getId(String type) {
        return getText("identifiers", type);
    }

}
