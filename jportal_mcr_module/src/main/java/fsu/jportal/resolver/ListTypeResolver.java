package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ListTypeResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Element listType = new Element("listType");
        String journalID = href.substring(href.indexOf(":") + 1);
        Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
        try {
            XPath hiddenTemplateXpath = XPath.newInstance("/mycoreobject/metadata/contentClassis1/contentClassi1/@categid");
            Attribute categIDAttr = (Attribute) hiddenTemplateXpath.selectSingleNode(journalXML);
            String categID = categIDAttr.getValue();
            if(categIDAttr != null && categID.equals("calendar")) {
                listType.addContent(categID);
            }else{
                listType.addContent("journal");
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new JDOMSource(listType);
    }

}
