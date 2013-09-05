package fsu.jportal.util;

import java.util.List;

import org.jdom2.Document;
import org.jdom2.Text;
import org.jdom2.filter.Filters;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRObjectUtils;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;



public class ContentTools {
    
    public <T> T getParents(String childID, ParentsList<T> parentsList) {
        MCRObjectID mcrChildID = MCRObjectID.getInstance(childID);
        if (MCRXMLMetadataManager.instance().exists(mcrChildID)) {
            XPathExpression<Text> titleXpath = XPathFactory.instance().compile(
                    "/mycoreobject/metadata/maintitles/maintitle[@inherited='0']/text()", Filters.text());
            List<MCRObject> parents = MCRObjectUtils.getAncestors(MCRMetadataManager.retrieveMCRObject(mcrChildID));
            for (int i = 0; i < parents.size(); i++) {
                MCRObject parent = parents.get(i);
                Document parentXML = parent.createXML();
                String title = shortenText(titleXpath.evaluateFirst(parentXML).getText());
                String id = parent.getId().toString();
                String inherited = String.valueOf(i + 1);
                parentsList.addParent(title, id, inherited);
            }
        }
        
        return parentsList.getParents();
    }
    
    private String shortenText(String text) {
        if (text.length() > 20) {
            return text.substring(0, 20) + "...";
        }
        return text;
    }
}