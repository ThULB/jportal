package fsu.jportal.resolver;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.transform.JDOMSource;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class NameOfTemplateResolver implements URIResolver {

    @Override
    public Source resolve(String href, String base) throws TransformerException {
        Element nameOfTemplate = new Element("nameOfTemplate");
        String journalID = href.substring(href.indexOf(":") + 1);
        Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
        try {
            XPath hiddenTemplateXpath = XPath.newInstance("/mycoreobject/metadata/hidden_templates/hidden_template/text()");
            Text hiddenTemplate = (Text) hiddenTemplateXpath.selectSingleNode(journalXML);
            if(hiddenTemplate != null) {
                nameOfTemplate.addContent(hiddenTemplate.detach());
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return new JDOMSource(nameOfTemplate);
    }

}
