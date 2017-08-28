package fsu.jportal.resolver;

import fsu.jportal.util.JPComponentUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.metadata.*;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import java.util.ArrayList;
import java.util.List;

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
        MCRObjectID id = participantMcrObj.getId();
        try {
            return JPComponentUtil.getLegalEntity(id).get().getLogo();
        } catch (Exception exc) {
            LOGGER.warn("Unable to get logo of participant " + id);
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

    protected static class MODSLogoEntity {

        private String name;

        private String role;

        private String siteURL;

        private String logo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public void setSiteURL(String siteURL) {
            this.siteURL = siteURL;
        }

        public String getSiteURL() {
            return siteURL;
        }

        public void setLogo(String logo) {
            this.logo = logo;
        }

        public String getLogo() {
            return logo;
        }

    }

    protected static class MODSLogoEntityXMLMapper {
        private List<MODSLogoEntity> entities;

        private Namespace urmelNamespace = Namespace.getNamespace("urmel", "http://www.urmel-dl.de/ns/mods-entities");

        public MODSLogoEntityXMLMapper(List<MODSLogoEntity> entities) {
            this.entities = entities;
        }

        public Document getXML() {
            Document modsLogoEntities = new Document();
            Element entitiesTag = createUrmelTag("entities");
            entitiesTag.addNamespaceDeclaration(MCRConstants.XLINK_NAMESPACE);
            for (MODSLogoEntity entity : entities) {
                Element entityTag = createEntityTag(entity);
                if (entity.getSiteURL() != null) {
                    entityTag.addContent(createSiteTag(entity.getSiteURL()));
                }
                if (entity.getLogo() != null) {
                    entityTag.addContent(createLogoTag("logo", entity.getLogo()));
                }
                entitiesTag.addContent(entityTag);
            }

            modsLogoEntities.setRootElement(entitiesTag);
            return modsLogoEntities;
        }

        protected Element createLogoTag(String tagName, String url) {
            Element logoTag = createUrmelTag(tagName);
            logoTag.setAttribute(createXlinkAttr("href", url));
            logoTag.setAttribute(createXlinkAttr("type", "resource"));
            return logoTag;
        }

        protected Element createSiteTag(String url) {
            Element siteTag = createUrmelTag("site");
            siteTag.setAttribute(createXlinkAttr("href", url));
            siteTag.setAttribute(createXlinkAttr("type", "locator"));
            return siteTag;
        }

        protected Element createEntityTag(MODSLogoEntity entity) {
            Element entityTag = createUrmelTag("entity");
            entityTag.setAttribute(createXlinkAttr("title", entity.getName()));
            entityTag.setAttribute(createXlinkAttr("type", "extended"));
            entityTag.setAttribute(new Attribute("type", entity.getRole()));
            return entityTag;
        }

        protected Attribute createXlinkAttr(String attrName, String attrValue) {
            return new Attribute(attrName, attrValue, MCRConstants.XLINK_NAMESPACE);
        }

        protected Element createUrmelTag(String tagName) {
            return new Element(tagName, urmelNamespace);
        }
    }

}
