package fsu.jportal.xml;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class XMLContentTools {

    public Element getParents(String childID) {
        Element parents = new Element("parents");
        MCRObjectID mcrObjectID = MCRObjectID.getInstance(childID);
        if (!MCRXMLMetadataManager.instance().exists(mcrObjectID)) {
            return parents;
        }
        Document childXML = MCRXMLMetadataManager.instance().retrieveXML(mcrObjectID);
        XPathExpression<Element> parentIdXpath = XPathFactory.instance().compile(
                "/mycoreobject/structure/parents/parent[@inherited='0']", Filters.element());
        XPathExpression<Text> titleXpath = XPathFactory.instance().compile(
                "/mycoreobject/metadata/maintitles/maintitle[@inherited='1']/text()", Filters.text());
        while (true) {
            Element parent = parentIdXpath.evaluateFirst(childXML);
            if (parent != null) {
                Text parentTitle = titleXpath.evaluateFirst(childXML);
                parent.setAttribute("inherited", String.valueOf(parents.getContentSize()));
                parent.setAttribute("title", shortenText(parentTitle.getText()), MCRConstants.XLINK_NAMESPACE);
                parents.addContent(0, parent.detach());
                MCRObjectID parentId = MCRObjectID.getInstance(parent.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE));
                if (!MCRMetadataManager.exists(parentId)) {
                    parent.setAttribute("error", "not found"); 
                    break;
                }
                childXML = MCRXMLMetadataManager.instance().retrieveXML(parentId);
            } else {
                break;
            }
        }
        return parents;
    }

    private String shortenText(String text) {
        if (text.length() > 20) {
            return text.substring(0, 20) + "...";
        }
        return text;
    }
}