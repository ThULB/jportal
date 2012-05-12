package fsu.jportal.xml;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConstants;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class XMLContentTools{
	public Element getParents(String childID) {
		Document childXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(childID));
		Element parents = new Element("parents");
		try {
			XPath parentIdXpath = XPath.newInstance("/mycoreobject/structure/parents/parent[@inherited='0']");
			XPath titleXpath = XPath.newInstance("/mycoreobject/metadata/maintitles/maintitle[@inherited='1']/text()");
			
			while (true) {
				Element parent = (Element) parentIdXpath.selectSingleNode(childXML);
				if (parent != null) {
					Text parentTitle = (Text) titleXpath.selectSingleNode(childXML);
					parent.setAttribute("inherited", String.valueOf(parents.getContentSize()));
					parent.setAttribute("title", shortenText(parentTitle.getText()),MCRConstants.XLINK_NAMESPACE);
					parents.addContent(parent.detach());
					childXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(parent.getAttributeValue("href", MCRConstants.XLINK_NAMESPACE)));
				}else{
					break;
				}
			}
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return parents;
	}
	
	private String shortenText(String text) {
		if(text.length() > 20){
			return text.substring(0, 20) + "...";
		}
		return text;
	}
}