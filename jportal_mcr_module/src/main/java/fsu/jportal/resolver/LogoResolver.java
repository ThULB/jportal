package fsu.jportal.resolver;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.transform.JDOMSource;
import org.mycore.datamodel.metadata.MCRMetaElement;
import org.mycore.datamodel.metadata.MCRMetaInstitutionName;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPPerson;
import fsu.jportal.mods.MODSLogoEntity;
import fsu.jportal.xml.mapper.MODSLogoEntityXMLMapper;

/**
 * URIResolver to retrieve the logos of participant partners.
 * 
 * <br />
 * Use: "logo:journalID"
 * 
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "logo")
public class LogoResolver implements URIResolver {

    static Logger LOGGER = LogManager.getLogger(LogoResolver.class);

    private static enum ParticipantRoleTypes {
        operator, sponsor, partner;
        public static boolean contains(String type) {
            for (ParticipantRoleTypes participantType : ParticipantRoleTypes.values()) {
                if (type.equals(participantType.toString())) {
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String journalID = href.substring(href.indexOf(":") + 1);
        return new JDOMSource(get(journalID));
    }

    public static Document get(String journalID) {
        LOGGER.info("MODS logo for " + journalID);
        List<MODSLogoEntity> entities = new ArrayList<MODSLogoEntity>();
        MCRObject mcrObject = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(journalID));
        MCRMetaElement participants = mcrObject.getMetadata().getMetadataElement("participants");
        if (participants != null) {
            for (MCRMetaInterface retrievedElem : participants) {
                if (retrievedElem instanceof MCRMetaLinkID) {
                    MCRMetaLinkID participant = (MCRMetaLinkID) retrievedElem;
                    String role = participant.getType();
                    if (role != null && ParticipantRoleTypes.contains(role)) {
                        String participantID = participant.getXLinkHref();
                        MCRObject participantMcrObj = MCRMetadataManager
                            .retrieveMCRObject(MCRObjectID.getInstance(participantID));
                        entities.add(createLogoEntity(role, participantMcrObj));
                    }
                }
            }
        }
        return new MODSLogoEntityXMLMapper(entities).getXML();
    }

    protected static MODSLogoEntity createLogoEntity(String role, MCRObject participantMcrObj) {
        MODSLogoEntity modsLogoEntity = new MODSLogoEntity();
        modsLogoEntity.setName(getFullname(participantMcrObj));
        modsLogoEntity.setRole(role);
        modsLogoEntity.setSiteURL(getSite(participantMcrObj));
        modsLogoEntity.setLogo(getLogoURL(participantMcrObj));
        return modsLogoEntity;
    }

    protected static String getLogoURL(MCRObject participantMcrObj) {
        String type = participantMcrObj.getId().getTypeId();
        if(type.equals("person")) {
            JPPerson person = new JPPerson(participantMcrObj);
            return person.getLogo();
        } else if(type.equals("jpinst")) {
            JPInstitution jpinst = new JPInstitution(participantMcrObj);
            return jpinst.getLogo();
        }
        return null;
    }

    protected static String getFullname(MCRObject participantMcrObj) {
        MCRMetaElement names = participantMcrObj.getMetadata().getMetadataElement("names");
        if (names != null) {
            for (MCRMetaInterface nameElem : names) {
                if (nameElem instanceof MCRMetaInstitutionName) {
                    MCRMetaInstitutionName name = (MCRMetaInstitutionName) nameElem;
                    return name.getFullName();
                }
            }
        }
        return "No Name";
    }

    protected static String getSite(MCRObject participantMcrObj) {
        MCRMetaElement urls = participantMcrObj.getMetadata().getMetadataElement("urls");
        if (urls != null) {
            for (MCRMetaInterface url : urls) {
                if (url instanceof MCRMetaLink) {
                    return ((MCRMetaLink) url).getXLinkHref();
                }
            }
        }
        return null;
    }
}
