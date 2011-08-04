package fsu.jportal.xml;

import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class ClassificationIDExtractor {
    private XMLDataManager xmlDataManager;

    public ClassificationIDExtractor() {
        xmlDataManager = new XMLDataManager() {
            @Override
            public Document getXML(String id) {
                return MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(id));
            }
        };
    }
    
    public ClassificationIDExtractor(XMLDataManager xmlDataManager) {
        this.xmlDataManager = xmlDataManager;
    }

    public void getClassIDs(String id) {
        Document xml = xmlDataManager.getXML(id);
        try {
            XPath xPath = XPath.newInstance("/mycoreobject/metadata/*[contains(name(),'hidden_classiVol')]");
            List selectNodes = xPath.selectNodes(xml);
            System.out.println("nodes: " + selectNodes.toString());
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
}
