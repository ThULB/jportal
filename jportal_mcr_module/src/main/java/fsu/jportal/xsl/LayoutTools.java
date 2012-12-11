package fsu.jportal.xsl;

import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class LayoutTools {
    public interface JournalInfo {
        public String getInfo(Object node);
    }

    public static String getNameOfTemplate(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/hidden_templates/hidden_template/text()");
        return infoProvider.get(new TemplateName());
    }
    
    public String getListType(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/contentClassis1/contentClassi1[@categid = 'calendar']");
        return infoProvider.get(new ListType());
    }

    static class InfoProvider {
        private String id;
        private String xpath;
        
        public InfoProvider(String id, String xpath) {
            this.id = id;
            this.xpath = xpath;
        }
        
        public String get(JournalInfo fromObj) throws JDOMException {
            Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(id));
            XPath hiddenTemplateXpath = XPath.newInstance(xpath);
            Object node = hiddenTemplateXpath.selectSingleNode(journalXML);
            return fromObj.getInfo(node);
        }
    }

    static class TemplateName implements JournalInfo {
        public String getInfo(Object selectSingleNode) {
            Text hiddenTemplate = (Text) selectSingleNode;
            if (hiddenTemplate != null) {
                return hiddenTemplate.getText();
            }

            return "";
        }
    }
    
    static class ListType implements JournalInfo{
        @Override
        public String getInfo(Object node) {
            if (node == null) {
                return "journal";
            } else {
                return "calendar";
            }
        }
        
    }
}
