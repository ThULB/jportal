package fsu.jportal.xml;

import java.util.ArrayList;
import java.util.List;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
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

    public List<String> getClassIDs(String id) {
        Document xml = xmlDataManager.getXML(id);
        try {
            XPath classiVolXPath = XPath.newInstance("/mycoreobject/metadata/*[contains(name(),'hidden_classiVol')]/*[contains(name(),'hidden_classiVol')]/text()");
            XPath classiPubXPath = XPath.newInstance("/mycoreobject/metadata/*[contains(name(),'hidden_classispub')]/*[contains(name(),'hidden_classipub')]/text()");
            XPath classiPubTypeXPath = XPath.newInstance("/mycoreobject/metadata/*[contains(name(),'hidden_pubTypesID')]/*[contains(name(),'hidden_pubTypeID')]/text()");
            XPath classiRubricXPath = XPath.newInstance("/mycoreobject/metadata/*[contains(name(),'hidden_rubricsID')]/*[contains(name(),'hidden_rubricID')]/text()");
            ArrayList<String> idList = new ArrayList<String>();
            List<Text> classiVolIDs = classiVolXPath.selectNodes(xml);
            for (Text classVolID : classiVolIDs) {
                idList.add(classVolID.getText());
            }
            List<Text> classiPubIDs = classiPubXPath.selectNodes(xml);
            for (Text classPubID : classiPubIDs) {
                idList.add(classPubID.getText());
            }
            List<Text> classiPubTypeIDs = classiPubTypeXPath.selectNodes(xml);
            for (Text classPubTypeID : classiPubTypeIDs) {
                idList.add(classPubTypeID.getText());
            }
            List<Text> classiRubricIDs = classiRubricXPath.selectNodes(xml);
            for (Text classRubricID : classiRubricIDs) {
                idList.add(classRubricID.getText());
            }
            
            return idList;
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
    
}
