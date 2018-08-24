package fsu.jportal.resolver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.jdom2.transform.JDOMSource;
import org.mycore.common.MCRConstants;
import org.mycore.common.config.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaLink;
import org.mycore.datamodel.metadata.MCRMetaLinkID;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRFrontendUtil;

import fsu.jportal.backend.JPInstitution;
import fsu.jportal.backend.JPJournal;
import fsu.jportal.backend.JPPeriodicalComponent;
import fsu.jportal.util.JPComponentUtil;

/**
 * URIResolver to retrieve the logos of participant partners and owners. You can choose between different xml mappers
 * which define how the returning xml looks like. Right now there are two mappers implemented:
 * 
 * <ul>
 *     <li>footer: used to transform the logos of the bottom of the page</li>
 *     <li>mods: used to create the http://www.urmel-dl.de/ns/mods-entities in the mods part for oai</li>
 * </ul>
 *
 * <p>Use: logo:{mapper}:{objectID} e.g. logo:footer:jportal_journal_00000001</p>
 *
 * @author Matthias Eichner
 */
@URIResolverSchema(schema = "logo")
public class LogoResolver implements URIResolver {

    private static final Logger LOGGER = LogManager.getLogger(LogoResolver.class);

    private static Map<String, LogoEntityXMLMapper> ENTITY_MAPPERS;

    static {
        ENTITY_MAPPERS = new HashMap<>();
        ENTITY_MAPPERS.put("mods", new MODSLogoEntityXMLMapper());
        ENTITY_MAPPERS.put("footer", new FooterLogoEntityXMLMapper());
    }

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        String[] split = href.split(":");
        String mapperId = split[1];
        String objectID = split.length >= 3 ? split[2] : null;
        return new JDOMSource(get(mapperId, objectID));
    }

    public static Document get(String mapperId, String objectID) throws TransformerException {
        LOGGER.info("MODS logo for {} mapped with {}", objectID, mapperId);
        LogoEntityXMLMapper mapper = ENTITY_MAPPERS.get(mapperId);
        if (mapper == null) {
            throw new TransformerException("Unable to get logos for '" + objectID + "' with mapper '" + mapperId
                + "' cause mapper is not defined!");
        }
        List<LogoEntity> entities = new ArrayList<>();
        if (!MCRObjectID.isValid(objectID)) {
            return mapper.toXML(entities);
        }
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(objectID);
        JPJournal journal = JPComponentUtil.getPeriodical(mcrObjectID)
            .map(JPPeriodicalComponent::getJournal)
            .orElse(null);
        if (journal == null || journal.getParticipants().isEmpty()) {
            return mapper.toXML(entities);
        }
        List<MCRMetaLinkID> participants = journal.getParticipants();
        for (MCRMetaLinkID participant : participants) {
            String role = participant.getType();
            if (role != null && mapper.supportRole(role)) {
                String participantID = participant.getXLinkHref();
                JPComponentUtil
                    .get(MCRObjectID.getInstance(participantID), JPInstitution.class)
                    .ifPresent(institution -> entities.add(createLogoEntity(role, institution)));
            }
        }
        return mapper.toXML(entities);
    }

    protected static LogoEntity createLogoEntity(String role, JPInstitution institution) {
        LogoEntity logoEntity = new LogoEntity();
        logoEntity.setName(institution.getTitle());
        logoEntity.setRole(role);
        logoEntity.setSiteURL(institution.getURL().map(MCRMetaLink::getXLinkHref).orElse(null));
        logoEntity.setLogo(institution.getLogo());
        return logoEntity;
    }

    protected static class LogoEntity {

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

    protected interface LogoEntityXMLMapper {

        Document toXML(List<LogoEntity> logoEntities);

        boolean supportRole(String role);
    }

    protected static class MODSLogoEntityXMLMapper implements LogoEntityXMLMapper {

        private Namespace urmelNamespace = Namespace.getNamespace("urmel", "http://www.urmel-dl.de/ns/mods-entities");

        @Override
        public boolean supportRole(String role) {
            return Arrays.asList("operator", "sponsor", "partner").contains(role);
        }

        @Override
        public Document toXML(List<LogoEntity> logoEntities) {
            Document modsLogoEntities = new Document();
            Element entitiesTag = createUrmelTag("entities");
            entitiesTag.addNamespaceDeclaration(MCRConstants.XLINK_NAMESPACE);
            for (LogoEntity entity : logoEntities) {
                Element entityTag = createEntityTag(entity);
                if (entity.getSiteURL() != null) {
                    entityTag.addContent(createSiteTag(entity.getSiteURL()));
                }
                if (entity.getLogo() != null) {
                    entityTag.addContent(createLogoTag(entity.getLogo()));
                }
                entitiesTag.addContent(entityTag);
            }

            modsLogoEntities.setRootElement(entitiesTag);
            return modsLogoEntities;
        }

        protected Element createLogoTag(String url) {
            Element logoTag = createUrmelTag("logo");
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

        protected Element createEntityTag(LogoEntity entity) {
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

    protected static class FooterLogoEntityXMLMapper implements LogoEntityXMLMapper {

        @Override
        public boolean supportRole(String role) {
            return Arrays.asList("owner", "partner").contains(role);
        }

        @Override
        public Document toXML(List<LogoEntity> logoEntities) {
            // get default logo entity
            LogoEntity defaulLogoEntity = getDefaultLogo(logoEntities);
            logoEntities.add(0, defaulLogoEntity);

            // create document
            Document footer = new Document(new Element("logos"));
            Element root = footer.getRootElement();

            // remove doublets (same site url | same logo), convert to xml & add to root
            Set<String> doublets = new HashSet<>();
            for (LogoEntity logoEntity : logoEntities) {
                String logo = logoEntity.getLogo();
                String siteURL = logoEntity.getSiteURL();
                if (logo == null || doublets.contains(logo) || doublets.contains(siteURL)) {
                    continue;
                }
                doublets.add(logo);
                doublets.add(siteURL);
                root.addContent(toXML(logoEntity));
            }
            return footer;
        }

        protected Element toXML(LogoEntity logoEntity) {
            Element entity = new Element("entity");
            entity.setAttribute("label", logoEntity.getName() != null ? logoEntity.getName() : "");
            entity.setAttribute("url", logoEntity.getSiteURL() != null ? logoEntity.getSiteURL() : "");
            String basePart = MCRFrontendUtil.getBaseURL();
            String proxyLogoPart = MCRConfiguration.instance().getString("JP.Site.Logo.Proxy.url");
            String imagePart = logoEntity.getLogo();
            String logoURL = basePart + (imagePart.startsWith(proxyLogoPart) ? "" : proxyLogoPart) + imagePart;
            entity.setAttribute("logoURL", logoURL);
            return entity;
        }

        protected LogoEntity getDefaultLogo(List<LogoEntity> logoEntities) {
            LogoEntity defaultLogoEntity = new LogoEntity();
            MCRConfiguration conf = MCRConfiguration.instance();
            defaultLogoEntity.setName(conf.getString("JP.Site.Owner.label"));
            defaultLogoEntity.setRole("owner");
            defaultLogoEntity.setSiteURL(conf.getString("JP.Site.Footer.Logo.url"));
            defaultLogoEntity.setLogo(
                logoEntities.isEmpty() ? conf.getString("JP.Site.Footer.Logo.default")
                    : conf.getString("JP.Site.Footer.Logo.small"));
            return defaultLogoEntity;
        }

    }

}
