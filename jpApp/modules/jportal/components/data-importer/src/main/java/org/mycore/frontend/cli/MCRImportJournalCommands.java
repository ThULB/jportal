package org.mycore.frontend.cli;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRJPortalJournalContextForUserManagement;
import org.mycore.frontend.MCRJPortalJournalContextForWebpages;
import org.mycore.importer.event.MCRImportStatusAdapter;
import org.mycore.importer.event.MCRImportStatusEvent;
import org.mycore.importer.mcrimport.MCRImportImporter;

public class MCRImportJournalCommands {
    
    private static Logger LOGGER = Logger.getLogger(MCRImportJournalCommands.class);

    public static List<String> importJournals(String mappingFile) throws Exception {
        MCRImportImporter importer = new MCRImportImporter(new File(mappingFile));
        importer.addStatusListener(new JournalImportStatusListener(importer));
        importer.generateMyCoReFiles();
        return importer.getCommandList();
    }

    public static void createContext(String mcrId) throws Exception {
//        MCRObjectID mcrId = new MCRObjectID(mcrId);
//        // wait until mcr object is in db
//        MCRObject.ex
//        
        // setup context
        String precHref = "/content/main/journalList/dummy.xml";
        String layoutTemplate = "template_DynamicLayoutTemplates";
        int number = new MCRObjectID(mcrId).getNumberAsInteger();
        String shortCut = new StringBuffer("journal_").append(String.valueOf(number)).toString();
        MCRJPortalJournalContextForWebpages wc = new MCRJPortalJournalContextForWebpages(mcrId, precHref, layoutTemplate, shortCut);
        try {
            wc.create();
        } catch (MCRException exception) {
            LOGGER.error(exception);
            return;
        }
        // setup user, groups and assign acl's
        MCRJPortalJournalContextForUserManagement uc = new MCRJPortalJournalContextForUserManagement(mcrId, shortCut);
        uc.setUserListTOC(new String[] { "" });
        uc.setUserListArt(new String[] { "" });
        uc.setUserListTOCArt(new String[] { "administrator" });
        uc.setGroup(new String[] { "" });
        uc.setup();
    }

    private static class JournalImportStatusListener extends MCRImportStatusAdapter {
        private MCRImportImporter importer;
        public JournalImportStatusListener(MCRImportImporter importer) {
            this.importer = importer;
        }
        @Override
        public void objectGenerated(MCRImportStatusEvent e) {
            StringBuffer command = new StringBuffer("internal create default context ");
            command.append(e.getObjectName());
            importer.getCommandList().add(command.toString());
        }
    }

}