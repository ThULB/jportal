package org.mycore.frontend.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.JDOMException;
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
}
