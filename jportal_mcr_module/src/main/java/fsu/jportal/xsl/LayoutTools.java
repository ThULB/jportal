package fsu.jportal.xsl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Text;
import org.jdom2.output.DOMOutputter;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;
import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class LayoutTools {
    private static class InfoProvider {
        private String id;
        
        private String[] xpathList;

        public InfoProvider(String id, String xpath) {
            this(id, new String[]{xpath});
        }
        
        public InfoProvider(String id, String... xpath) {
            this.id = id;
            this.xpathList = xpath;
        }

        public <T> T get(MCRObjectInfo<T> fromObj) throws JDOMException, IOException, SAXException {
            MCRObjectID mcrid = MCRObjectID.getInstance(id);
            MCRXMLMetadataManager metadataManager = MCRXMLMetadataManager.instance();
            if (metadataManager.exists(mcrid)) {
                List<Object> nodes = new ArrayList<Object>();
                Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(mcrid);
                for (String xpath : xpathList) {
                    XPathExpression<Object> hiddenTemplateXpath = XPathFactory.instance().compile(xpath);
                    Object node = hiddenTemplateXpath.evaluateFirst(journalXML);
                    if (node != null) {
                        nodes.add(node);
                    }
                }
                return fromObj.getInfo(nodes);
            }
            return null;
        }
    }

    private static class SimpleText implements MCRObjectInfo<String> {
        public String getInfo(List<Object> node) {
            if (node.size() == 1) {
                Text textNode = (Text) node.get(0);
                return textNode.getText();
            }
            return "";
        }
    }

    private static class SimpleAttribute implements MCRObjectInfo<String> {
        public String getInfo(List<Object> node) {
            if (node.size() == 1) {
                Attribute attrNode = (Attribute) node.get(0);
                return attrNode.getValue();
            }
            return "";
        }
    }

    private static class DerivateDisplay implements MCRObjectInfo<String> {
        @Override
        public String getInfo(List<Object> node) {
            if (node.size() == 0) {
                return "false";
            } else {
                return "true";
            }
        }

    }

    private static class DatesInfo implements MCRObjectInfo<Node> {
        @Override
        public Node getInfo(List<Object> nodes) {
            Element root = new Element("datesInfo");
            Document datesDoc = new Document(root);
            
            for (Object node : nodes) {
                node.getClass().getCanonicalName();
                if(node instanceof Element){
                    root.addContent(((Element) node).detach());
                }
            }
            
            DOMOutputter domOutputter = new DOMOutputter();
            try {
                return domOutputter.output(datesDoc).getFirstChild();
            } catch (JDOMException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }
    }

    private static interface MCRObjectInfo<T> {
        public T getInfo(List<Object> node);
    }

    public String getNameOfTemplate(String journalID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/hidden_templates/hidden_template/text()");
        return infoProvider.get(new SimpleText());
    }
    
    public String getJournalID(String mcrID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(mcrID, "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()");
        return infoProvider.get(new SimpleText());
    }

    public String getMaintitle(String journalID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/maintitles/maintitle/text()");
        return infoProvider.get(new SimpleText());
    }

    public String getListType(String journalID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/contentClassis1/contentClassi1/@categid");
        return infoProvider.get(new SimpleAttribute());
    }

    public String getDerivateDisplay(String derivateID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(derivateID, "/mycorederivate/derivate[not(@display) or @display!='false']");
        return infoProvider.get(new DerivateDisplay());
    }
    
    public Node getDatesInfo(String journalID) throws TransformerException, JDOMException, IOException, SAXException {
        InfoProvider infoProvider = new InfoProvider(journalID, "/mycoreobject/metadata/dates","/mycoreobject/metadata/hidden_genhiddenfields1");
        return infoProvider.get(new DatesInfo());
    }
    
    public String getUserName(){
        MCRUserInformation userInformation = MCRSessionMgr.getCurrentSession().getUserInformation();
        String realname = userInformation.getUserAttribute(MCRUserInformation.ATT_REAL_NAME);
        if(realname != null && !"".equals(realname.trim())){
            return realname;
        }else{
            return userInformation.getUserID();
        }
    }
}
