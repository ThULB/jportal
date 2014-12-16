package fsu.jportal.xml.mapper;

import java.util.List;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Namespace;
import org.mycore.common.MCRConstants;

import fsu.jportal.mods.MODSLogoEntity;

public class MODSLogoEntityXMLMapper {
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
