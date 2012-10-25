package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.xpath.XPath;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRUtils;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.cli.annotation.MCRCommand;
import org.mycore.frontend.cli.annotation.MCRCommandGroup;

@MCRCommandGroup(name = "JP Migrating Commands")
public class MigratingCMDs {
    private static Logger LOGGER = Logger.getLogger(MigratingCMDs.class);

    @MCRCommand(helpKey = "Move intro xml from webapp into data folder.", syntax = "migrate intro xml")
    public static void migrateIntroXML() throws JDOMException, IOException {
        List<String> journalIDs = (List<String>) MCRXMLMetadataManager.instance().listIDsOfType("jpjournal");
        XPath hiddenWebContextXpath = XPath.newInstance("/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext");
        String mcrBaseDir = MCRConfiguration.instance().getString("MCR.basedir");
        String webappDir = mcrBaseDir + "/build/webapps";
        String journalFileBase = MCRConfiguration.instance().getString("JournalFileFolder");

        for (String journalID : journalIDs) {
            Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
            String journalContextPath = hiddenWebContextXpath.valueOf(journalXML);

            if (!journalContextPath.equals("") && journalContextPath != null) {
                String pathToJournalXML = webappDir + journalContextPath;
                File journalXMLFile = new File(pathToJournalXML);
                LOGGER.info("Move " + pathToJournalXML + " for journal " + journalID);

                if (journalXMLFile.exists()) {
                    String journalFilePath = journalFileBase + File.separator + journalID;
                    File journalFile = new File(journalFilePath);
                    if (!journalFile.exists()) {
                        journalFile.mkdirs();
                    }

                    String introXMLPath = journalFilePath + File.separator + "intro.xml";
                    File introXMLFile = new File(introXMLPath);
                    FileInputStream journalXMLInputStream = new FileInputStream(journalXMLFile);
                    FileOutputStream introXMLOutputStream = new FileOutputStream(introXMLFile);
                    MCRUtils.copyStream(journalXMLInputStream, introXMLOutputStream);
                    introXMLOutputStream.close();
                    journalXMLInputStream.close();
                    LOGGER.info("Successful moved " + pathToJournalXML + " to " + introXMLPath);
                }
            } else {
                LOGGER.info("Journal " + journalID + " has no journal context!");
            }
        }
    }
    
    @MCRCommand(helpKey="Migrate template name from navigation.xml", syntax="migrate template")
    public static void migrateTemplate() throws JDOMException, IOException{
        List<String> journalIDs = (List<String>) MCRXMLMetadataManager.instance().listIDsOfType("jpjournal");
        XPath hiddenWebContextXpath = XPath.newInstance("/mycoreobject/metadata/hidden_websitecontexts/hidden_websitecontext/text()");
        
        //load navigation.xml
        String mcrBaseDir = MCRConfiguration.instance().getString("MCR.basedir");
        String navigationDir = mcrBaseDir + "/build/webapps/config/navigation.xml";
        File navigationXMLFile = new File(navigationDir);
        SAXBuilder builder = new SAXBuilder();
        Document navigationXML = (Document) builder.build(navigationXMLFile);

        for (String journalID : journalIDs) {
            Document journalXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(journalID));
            String journalContextPath = hiddenWebContextXpath.valueOf(journalXML).trim();
            
            if(journalContextPath!= null && !journalContextPath.equals("")){
                LOGGER.info("Add Termplate a Template  to " + journalContextPath);
                XPath templateXpath = XPath.newInstance("/navigation/navi-main/item[@href='/content/main/journalList.xml']/item[@href='" + journalContextPath +  "']/@template");
                String journalTemplate = templateXpath.valueOf(navigationXML);
                
                if (journalTemplate != null && !journalTemplate.equals("")){
                    LOGGER.info("Template: " + journalTemplate);
                    Element journalMetadata = (Element) XPath.selectSingleNode(journalXML, "/mycoreobject/metadata");
                    
                    //create hidden_template            
                    Element template = new Element("hidden_template");
                    template.setText(journalTemplate);
                    template.setAttribute("inherited", "0");
                    template.setAttribute("form", "plain");
                    
                    //create hidden_templates  
                    Element templates = new Element("hidden_templates");
                    templates.setAttribute("class", "MCRMetaLangText");
                    templates.setAttribute("heritable", "false");
                    templates.setAttribute("notinherit", "false");
                    templates.addContent(template);            
                    
                    journalMetadata.addContent(templates);
      
                    Date date = new Date();
                    MCRXMLMetadataManager.instance().update(MCRObjectID.getInstance(journalID), journalXML, date);
                    LOGGER.info("Successful add " + journalTemplate + " to " + journalContextPath);
                }
                else{
                    LOGGER.info("No Template found for " + journalID);
                }
            }
        }        
    }
}
