package fsu.jportal.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.xpath.XPathExpression;
import org.jdom2.xpath.XPathFactory;

import fsu.jportal.resolver.JournalFilesResolver;

public class JournalConfig {
    private String objID;

    public JournalConfig(String objID) {
        this.objID = objID;
    }
    
    public HashMap<String, String> getJournalConfKeys() {
        Document journalConfigXML;
        try {
            journalConfigXML = createXML(getJournalConf(objID));
            return extractConfKeys(journalConfigXML);
        } catch (JDOMException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    private HashMap<String, String> extractConfKeys(Document journalConfigXML) {
        XPathExpression<Object> xpath = XPathFactory.instance().compile("/journalConf/conf[@id='jportal_acl_ip_editor_module']/key");
        HashMap<String, String> confKeys = new HashMap<String, String>();

        List<Object> nodes = xpath.evaluate(journalConfigXML);

        for (Object node : nodes) {
            if (node instanceof Element) {
                Element key = (Element) node;
                String name = key.getAttributeValue("name");
                String value = key.getAttributeValue("value");
                confKeys.put(name, value);
            }
        }
        return confKeys;
    }

    private Document createXML(FileInputStream fileIS) throws JDOMException, IOException, FileNotFoundException {
        SAXBuilder saxBuilder = new SAXBuilder();
        Document journalConfigXML = saxBuilder.build(fileIS);
        return journalConfigXML;
    }

    private FileInputStream getJournalConf(String objid) throws FileNotFoundException {
        JournalFilesResolver journalFilesResolver = new JournalFilesResolver();
        FileInputStream journalConfig = journalFilesResolver.getJournalFile(objid + "/config.xml");
        return journalConfig;
    }
}