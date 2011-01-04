package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

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
        addToJournalLists(obj.getId().toString(), obj.createXML());
    }

    public void addToJournalLists(String id, Document xml) {
        try {
            String mainTitle = getMainTitle(xml);
            if (mainTitle != null) {
                Journal journal = new Journal(id, mainTitle);
                new JournalListInIFS().addJournalToListOfType(defaultType(), journal);
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }

    }

    private String getMainTitle(Document xml) throws JDOMException {
        XPath maintitleXPath = XPath.newInstance("/mycoreobject/metadata/maintitles/maintitle");
        List maintitleNodes = maintitleXPath.selectNodes(xml);

        if (maintitleNodes.size() > 0) {
            return ((Element) maintitleNodes.get(0)).getText();
        }

        return null;
    }

    private String defaultType() {
        return MCRConfiguration.instance().getString("JP.config.journalList.defaultType", "journals");
    }

    public JournalList getJournalList(String type) {
        return new JournalListInIFS().getOrCreateJournalList(type);

    }

    public boolean deleteJournal(String id) {
        return new JournalListInIFS().deleteJournalInListOfType(defaultType(), id);
    }

    public void updateJournal(String id, Document xml) {
        deleteJournal(id);
        addToJournalLists(id, xml);
    }

    public void deleteJournal(MCRObject obj) {
        deleteJournal(obj.getId().toString());
    }

    public void updateJournal(MCRObject obj) {
        updateJournal(obj.getId().toString(), obj.createXML());
    }
}
