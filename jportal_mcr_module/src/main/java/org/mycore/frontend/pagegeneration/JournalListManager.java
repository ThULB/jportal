package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetadataManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;
import org.mycore.frontend.servlets.MCRServlet;
import org.mycore.services.fieldquery.MCRHit;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

import com.sun.xml.txw2.annotation.XmlElement;

import fsu.jportal.backend.impl.JournalListInIFS;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.thulb.jaxb.JaxbTools;

public class JournalListManager {
    private static JournalListManager instance;

    private JournalListCfg journalListCfg;

    private String journalListLocation;

    private String journalTypeMapLocation;

    private JournalListManager() {
    }

    public interface JournalListManagerCfg {
        public JournalListCfg getJournalListCfg();

        public String getJournalListLocation();

        public String getJournalTypeMapLocation();
    }

    @XmlRootElement
    public static class JournalListTypeMap {
        private Map<String, String> typeMap;

        public void setTypeMap(Map<String, String> typeMap) {
            this.typeMap = typeMap;
        }

        @XmlElement
        public Map<String, String> getTypeMap() {
            return typeMap;
        }

        public void addMap(String key, String val) {
            if (typeMap == null) {
                typeMap = new HashMap<String, String>();
            }
            typeMap.put(key, val);
        }

        public String getMap(String key) {
            return typeMap.get(key);
        }
    }

    private static class DefaultJournalListManagerCfg implements JournalListManagerCfg {
        private String cfgFileLocation;

        private String typeMapFileLocation;

        private String journalListLocation;

        public DefaultJournalListManagerCfg() {
            MCRConfiguration mcrConfiguration = MCRConfiguration.instance();
            String baseDir = mcrConfiguration.getString("MCR.basedir");
            String webappDir = mcrConfiguration.getString("MCR.webappsDir", "build/webapps".replace("/", File.separator));
            cfgFileLocation = baseDir + "/build/webapps/config/journalList.cfg.xml".replace("/", File.separator);
            typeMapFileLocation = baseDir + "/build/webapps/config/journalTypeMap.cfg.xml".replace("/", File.separator);
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

        @Override
        public String getJournalTypeMapLocation() {
            return typeMapFileLocation;
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
            instance.setJournalTypeMapLocation(cfg.getJournalTypeMapLocation());
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
            writeJournalList(journalListDef);
            for (MCRHit mcrHit : results) {
                MCRObject mcrObj = MCRMetadataManager.retrieveMCRObject(MCRObjectID.getInstance(mcrHit.getID()));
                updateJournal(mcrObj);
            }
        }
    }

    private void writeJournalList(JournalListDef journalListDef) throws FileNotFoundException {
        JournalList journalList = new JournalList();
        String type = journalListDef.getType();
        journalList.setType(type);
        journalList.setUrl(MCRServlet.getBaseURL() + "rsc/journalList/" + type);
        try {
            JaxbTools.marschall(journalList, new FileOutputStream(journalListLocation + journalListDef.getFileName()));
        } catch (JAXBException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            String type = xpathValueReader.getValueForPath("/mycoreobject/metadata/contentClassis1/contentClassi1/@categid",
                    new AttributeValue());

            File typeMapFile = new File(getJournalTypeMapLocation());
            if (typeMapFile.exists()) {
                try {
                    JournalListTypeMap typeMap = JaxbTools.unmarschall(new FileInputStream(typeMapFile),
                            JournalListTypeMap.class);
                    String mapToType = typeMap.getMap(type);
                    
                    if(mapToType != null){
                        type = mapToType;
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (JAXBException e) {
                    e.printStackTrace();
                }
            }

            if (type == null) {
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

    private class ElementValue implements ValueRetrieval {
        @Override
        public String getValue(Object obj) {
            return ((Element) obj).getText();
        }
    }

    private class AttributeValue implements ValueRetrieval {
        @Override
        public String getValue(Object obj) {
            return ((Attribute) obj).getValue();
        }
    }

    private class XpathValueReader {
        private Document xml;

        public XpathValueReader(Document xml) {
            this.xml = xml;
        }

        public String getValueForPath(String path, ValueRetrieval valueRetrieval) throws JDOMException {
            List nodes = XPath.selectNodes(xml, path);

            if (nodes.size() > 0) {
                return valueRetrieval.getValue(nodes.get(0));
            }

            return null;
        }
    }

    private String defaultType() {
        return MCRConfiguration.instance().getString("JP.config.journalList.defaultType", "journal");
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

    public void setJournalTypeMapLocation(String journalTypeMapLocation) {
        this.journalTypeMapLocation = journalTypeMapLocation;
    }

    public String getJournalTypeMapLocation() {
        return journalTypeMapLocation;
    }
}
