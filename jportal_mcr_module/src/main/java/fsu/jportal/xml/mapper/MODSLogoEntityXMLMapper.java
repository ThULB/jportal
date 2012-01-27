package fsu.jportal.xml.mapper;

import java.util.List;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.mycore.common.MCRConstants;

import fsu.jportal.mods.MODSLogoEntity;

public class MODSLogoEntityXMLMapper {
    private List<MODSLogoEntity> entities;
    private Namespace urmelNamespace = Namespace.getNamespace("urmel", "http://www.urmel-dl.de/ns/mods-entities");

    public MODSLogoEntityXMLMapper(List<MODSLogoEntity> entities) {
        this.entities = entities;
    }
    
    public Document getXML(){
        Document modsLogoEntities = new Document();
        Element entitiesTag = createUrmelTag("entities");
        entitiesTag.addNamespaceDeclaration(MCRConstants.XLINK_NAMESPACE);
        
        for (MODSLogoEntity entity : entities) {
            Element entityTag = createEntityTag(entity);
            createSiteTag(entity, entitiesTag);
            entityTag.addContent(createLogoTag("logo", entity.getLogoPlainURL()));
            entityTag.addContent(createLogoTag("full-logo", entity.getLogoPlusTextURL()));
            
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

    protected void createSiteTag(MODSLogoEntity entity, Element entityTag) {
        String siteURL = entity.getSiteURL();
        if(siteURL != null){
            Element siteTag = createUrmelTag("site");
            siteTag.setAttribute(createXlinkAttr("href", siteURL));
            siteTag.setAttribute(createXlinkAttr("type", "locator"));
            entityTag.addContent(siteTag);
        }
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