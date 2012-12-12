package fsu.jportal.xsl;

import javax.xml.transform.TransformerException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class LayoutTools {

    private static class InfoProvider {
        private String id;

        private String xpath;

        public InfoProvider(String id, String xpath) {
            this.id = id;
            this.xpath = xpath;
        }

        public String get(MCRObjectInfo fromObj) throws JDOMException {
            MCRObjectID mcrid = MCRObjectID.getInstance(id);
            MCRXMLMetadataManager metadataManager = MCRXMLMetadataManager.instance();
            if (metadataManager.exists(mcrid)) {

                Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(mcrid);
                XPath hiddenTemplateXpath = XPath.newInstance(xpath);
                Object node = hiddenTemplateXpath.selectSingleNode(journalXML);
                return fromObj.getInfo(node);
            }
            return "";
        }
    }

    private static class SimpleText implements MCRObjectInfo {
        public String getInfo(Object node) {
            Text textNode = (Text) node;
            if (textNode != null) {
                return textNode.getText();
            }
            return "";
        }
    }

    private static class ListType implements MCRObjectInfo {
        @Override
        public String getInfo(Object node) {
            if (node == null) {
                return "journal";
            } else {
                return "calendar";
            }
        }

    }

    private static class DerivateDisplay implements MCRObjectInfo {
        @Override
        public String getInfo(Object node) {
            if (node == null) {
                return "false";
            } else {
                return "true";
            }
        }

    }

    private static interface MCRObjectInfo {
        public String getInfo(Object node);
    }

    public String getNameOfTemplate(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/hidden_templates/hidden_template/text()");
        return infoProvider.get(new SimpleText());
    }

    public String getMaintitle(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/maintitles/maintitle/text()");
        return infoProvider.get(new SimpleText());
    }

    public String getListType(String journalID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(journalID,
                "/mycoreobject/metadata/contentClassis1/contentClassi1[@categid = 'calendar']");
        return infoProvider.get(new ListType());
    }

    public String getDerivateDisplay(String derivateID) throws TransformerException, JDOMException {
        InfoProvider infoProvider = new InfoProvider(derivateID, "/mycorederivate/derivate[not(@display) or @display!='false']");
        return infoProvider.get(new DerivateDisplay());
    }
}
