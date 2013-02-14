package fsu.jportal.xml;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRObjectUtils;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class XMLContentTools {

    public Element getParents(String childID) {
        Element parentsElement = new Element("parents");
        MCRObjectID mcrChildID = MCRObjectID.getInstance(childID);
        if (MCRXMLMetadataManager.instance().exists(mcrChildID)) {
            XPathExpression<Text> titleXpath = XPathFactory.instance().compile(
                    "/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()", Filters.text());
            List<MCRObject> parents = MCRObjectUtils.getAncestors(MCRMetadataManager.retrieveMCRObject(mcrChildID));
            for(int i = 0; i < parents.size(); i++) {
                MCRObject parent = parents.get(i);
                Document parentXML = parent.createXML();
                Element parentElement = new Element("parent");
                parentElement.setAttribute("inherited", String.valueOf(i + 1));
                parentElement.setAttribute("title", shortenText(titleXpath.evaluateFirst(parentXML).getText()));
                parentsElement.addContent(parentElement);
            }
        }
        return parentsElement;
    }

    private String shortenText(String text) {
        if (text.length() > 20) {
            return text.substring(0, 20) + "...";
        }
        return text;
    }
}