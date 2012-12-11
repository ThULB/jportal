package fsu.jportal.xsl;

import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class LayoutTools {
    public class DerivateDisplay implements MCRObjectInfo {

        @Override
        public String getInfo(Object node) {
            if (node == null) {
                return "false";
            } else {
                return "true";
            }
        }

    }
    
    public class InfoProvider {
        private String id;
        private String xpath;
        
        public InfoProvider(String id, String xpath) {
            this.id = id;
            this.xpath = xpath;
        }
        
        public String get(MCRObjectInfo fromObj) throws JDOMException {
            Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(id));
            XPath hiddenTemplateXpath = XPath.newInstance(xpath);
            Object node = hiddenTemplateXpath.selectSingleNode(journalXML);
            return fromObj.getInfo(node);
        }
    }

    public class TemplateName implements MCRObjectInfo {
        public String getInfo(Object selectSingleNode) {
            Text hiddenTemplate = (Text) selectSingleNode;
            if (hiddenTemplate != null) {
                return hiddenTemplate.getText();
            }

            return "";
        }
    }
    
    public class ListType implements MCRObjectInfo{
        @Override
        public String getInfo(Object node) {
            if (node == null) {
                return "journal";
            } else {
                return "calendar";
            }
        }
        
    }

    public interface MCRObjectInfo {
        public String getInfo(Object node);
    }

    public String getNameOfTemplate(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/hidden_templates/hidden_template/text()");
        return infoProvider.get(new TemplateName());
    }
    
    public String getListType(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/contentClassis1/contentClassi1[@categid = 'calendar']");
        return infoProvider.get(new ListType());
    }
    
    public String getDerivateDisplay(String derivateID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(derivateID, "/mycorederivate/derivate[not(@display) or @display!='false']");
        return infoProvider.get(new DerivateDisplay());
    }
}
