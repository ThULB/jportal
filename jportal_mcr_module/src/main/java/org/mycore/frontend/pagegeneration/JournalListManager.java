package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

import fsu.jportal.backend.impl.JournalListInIFS;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;

public class JournalListManager {
    private static JournalListManager instance;

    private JournalListCfg journalListCfg;

    private String journalListLocation;

    private JournalListManager() {
    }

    public interface JournalListManagerCfg {
        public JournalListCfg getJournalListCfg();

        public String getJournalListLocation();
    }

    private static class DefaultJournalListManagerCfg implements JournalListManagerCfg {
        private String cfgFileLocation;

        private String journalListLocation;

        public DefaultJournalListManagerCfg() {
            MCRConfiguration mcrConfiguration = MCRConfiguration.instance();
            String baseDir = mcrConfiguration.getString("MCR.basedir");
            String webappDir = mcrConfiguration.getString("MCR.webappsDir", "build/webapps".replace("/", File.separator));
            cfgFileLocation = baseDir + "/build/webapps/config/journalList.cfg.xml".replace("/", File.separator);
            journalListLocation = webappDir + "/content/main/".replace("/", File.separator);
        }

        @Override
        public JournalListCfg getJournalListCfg() {
            SAXBuilder builder = new SAXBuilder();
            try {
                Document cfgXMl = builder.build(new File(cfgFileLocation));
                return new XMLToJournalListCfg(cfgXMl);
            } catch (JDOMException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        public String getJournalListLocation() {
            return journalListLocation;
        }

    }

    public static JournalListManager instance() {
        return instance(new DefaultJournalListManagerCfg());
    }

    public static JournalListManager instance(JournalListManagerCfg cfg) {
        if (instance == null) {
            instance = new JournalListManager();
            instance.setJournalListLocation(cfg.getJournalListLocation());
            instance.setJournalListCfg(cfg.getJournalListCfg());
        }

        return instance;
    }

    private void setJournalListCfg(JournalListCfg journalListCfg) {
        this.journalListCfg = journalListCfg;
    }

    public JournalListCfg getJournalListCfg() {
        return journalListCfg;
    }

    public void createJournalLists() throws JDOMException, FileNotFoundException, IOException {
        TreeSet<JournalListDef> listDefs = getJournalListCfg().getListDefs();
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());

        for (JournalListDef journalListDef : listDefs) {
            MCRResults results = MCRQueryManager.search(journalListDef.getQuery());
            JournalListXML journalList = new MCRResultsToJournalList(results, journalListDef.getType());
            Document journalListXML = new JournalListToXML(journalList);
            FileOutputStream fileOutputStream = new FileOutputStream(journalListLocation + journalListDef.getFileName());
            outputter.output(journalListXML, fileOutputStream);
        }
    }

    public void setJournalListLocation(String journalListLocation) {
        this.journalListLocation = journalListLocation;
    }

    public String getJournalListLocation() {
        return journalListLocation;
    }

    public void addToJournalLists(MCRObject obj) {
        addToJournalLists(obj.createXML());
    }

    public void addToJournalLists(Document xml) {
        try {
            XpathValueReader xpathValueReader = new XpathValueReader(xml);
            String id = xpathValueReader.getValueForPath("/mycoreobject/@ID", new AttributeValue());
            String mainTitle = xpathValueReader.getValueForPath("/mycoreobject/metadata/maintitles/maintitle", new ElementValue());
            String type = xpathValueReader.getValueForPath("/mycoreobject/metadata/contentClassis1/contentClassi1/@categid", new AttributeValue());
            
            if(type == null){
                type = defaultType();
            }
            
            if (mainTitle != null) {
                Journal journal = new Journal(id, mainTitle);
                new JournalListInIFS().addJournalToListOfType(type, journal);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }

    }
    
    private interface ValueRetrieval {
        String getValue(Object obj);
    }
    
    private class ElementValue implements ValueRetrieval{
        @Override
        public String getValue(Object obj) {
            return ((Element) obj).getText();
        }
    }
    
    private class AttributeValue implements ValueRetrieval{
        @Override
        public String getValue(Object obj) {
            return ((Attribute) obj).getValue();
        }
    }

    private class XpathValueReader{
        private Document xml;

        public XpathValueReader(Document xml) {
            this.xml = xml;
        }
        
        public String getValueForPath(String path, ValueRetrieval valueRetrieval) throws JDOMException{
            List nodes = XPath.selectNodes(xml, path);
            
            if (nodes.size() > 0) {
                return valueRetrieval.getValue(nodes.get(0));
            }
            
            return null;
        }
    }

    private String defaultType() {
        return MCRConfiguration.instance().getString("JP.config.journalList.defaultType", "journals");
    }

    public JournalList getJournalList(String type) {
        return new JournalListInIFS().getOrCreateJournalList(type);

    }

    public boolean deleteJournal(String id) {
        return new JournalListInIFS().deleteJournalInListOfType(id);
    }

    public void updateJournal(Document xml) {
        XpathValueReader xpathValueReader = new XpathValueReader(xml);
        try {
            String id = xpathValueReader.getValueForPath("/mycoreobject/@ID", new AttributeValue());
            deleteJournal(id);
            addToJournalLists(xml);
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void deleteJournal(MCRObject obj) {
        deleteJournal(obj.getId().toString());
    }

    public void updateJournal(MCRObject obj) {
        updateJournal(obj.createXML());
    }
}
