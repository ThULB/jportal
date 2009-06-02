package org.mycore.frontend.servlets;

import java.io.File;

import org.jdom.Document;
import org.jdom.Element;
import org.mycore.frontend.pagegeneration.MCRJPortalAtoZListPageGenerator;

public class MCRJPortalAtoZListServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        MCRJPortalAtoZListPageGenerator gen = new MCRJPortalAtoZListPageGenerator();
        File journalListFile = gen.getJournalXmlFile();
        Element journalListElement = null;
        // if journalList.xml not exists -> create it
        if(!journalListFile.exists()) {
            gen.createJournalList();
            gen.saveJournalList();
        } else {
            // load from file
            gen.loadJournalList();
        }
        journalListElement = gen.getJournalListElement(); 
        getLayoutService().sendXML(job.getRequest(), job.getResponse(), new Document(journalListElement));
    }

}