package org.mycore.frontend.cli;

import org.mycore.frontend.pagegeneration.JournalListManager;


public class MCRJPortalPageGenCommands extends MCRAbstractCommands {
    
    public MCRJPortalPageGenCommands() {
        super();
        addCommand(new MCRCommand("generate page A to Z list", "org.mycore.frontend.cli.MCRJPortalPageGenCommands.pgAtoZList", "generates the journalList.xml in the webapps folder"));
    }

    public static void pgAtoZList() throws Exception {
        JournalListManager.instance().createJournalLists();
    }
}