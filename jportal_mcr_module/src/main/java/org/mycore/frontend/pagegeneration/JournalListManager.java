package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.TreeSet;

import org.dom4j.Node;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRMetaInterface;
import org.mycore.datamodel.metadata.MCRMetaLangText;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;
import org.mycore.services.fieldquery.MCRQueryManager;
import org.mycore.services.fieldquery.MCRResults;

import fsu.jportal.backend.impl.JournalListInIFS;
import fsu.jportal.jaxb.JournalList.Journal;

public class JournalListManager {
    private static JournalListManager instance;

    private JournalListCfg journalListCfg;

    private String journalListLocation;

    private JournalListManager() {
        MCRConfiguration mcrConfiguration = MCRConfiguration.instance();
        String baseDir = mcrConfiguration.getString("MCR.basedir");
        String webappDir = mcrConfiguration.getString("MCR.webappsDir", "build/webapps".replace("/", File.separator));
        String cfgFileLocation = baseDir + "/build/webapps/config/journalList.cfg.xml".replace("/", File.separator);
        String journalListLocation = webappDir + "/content/main/".replace("/", File.separator);
        setJournalListLocation(journalListLocation);

        SAXBuilder builder = new SAXBuilder();
        try {
            Document cfgXMl = builder.build(new File(cfgFileLocation));
            setJournalListCfg(new XMLToJournalListCfg(cfgXMl));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JournalListManager instance() {
        if (instance == null) {
            instance = new JournalListManager();
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
            JournalList journalList = new MCRResultsToJournalList(results, journalListDef.getType());
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
        Document xml = obj.createXML();
        XPath maintitleXPath;
        try {
            maintitleXPath = XPath.newInstance("/mycoreobject/metadata/maintitles/maintitle");
            List maintitleNodes = maintitleXPath.selectNodes(xml);
            
            if(maintitleNodes.size() > 0){
                Journal journal = new Journal(obj.getId().toString(), ((Element)maintitleNodes.get(0)).getText());
                new JournalListInIFS().addJournalToListOfType("journals", journal);
            }
        } catch (JDOMException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}
