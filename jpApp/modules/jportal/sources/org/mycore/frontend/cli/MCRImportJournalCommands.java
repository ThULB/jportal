package org.mycore.frontend.cli;

import java.io.File;

import org.apache.log4j.Logger;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.frontend.MCRJPortalJournalContextForUserManagement;
import org.mycore.frontend.MCRJPortalJournalContextForWebpages;
import org.mycore.importer.event.MCRImportStatusAdapter;
import org.mycore.importer.event.MCRImportStatusEvent;
import org.mycore.importer.mcrimport.MCRImportImporter;

public class MCRImportJournalCommands {
    
    private static Logger LOGGER = Logger.getLogger(MCRImportJournalCommands.class);

    public static void importJournals(String mappingFile) throws Exception {
        MCRImportImporter importer = new MCRImportImporter(new File(mappingFile));
        importer.addStatusListener(new MyImportStatusListener());
        importer.startImport();
    }

    private static class MyImportStatusListener extends MCRImportStatusAdapter {
        @Override
        public void objectImported(MCRImportStatusEvent e) {
            String mcrId = e.getObjectName();

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
            uc.setUserListTOC(new String[] {""});
            uc.setUserListArt(new String[] {""});
            uc.setUserListTOCArt(new String[] {"administrator"});
            uc.setGroup(new String[] {""});
            uc.setup();
        }
    }

}