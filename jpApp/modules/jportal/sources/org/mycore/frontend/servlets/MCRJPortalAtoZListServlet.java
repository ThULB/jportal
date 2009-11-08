package org.mycore.frontend.servlets;

import org.jdom.Document;
import org.mycore.frontend.pagegeneration.MCRJPortalAtoZListPageGenerator;

public class MCRJPortalAtoZListServlet extends MCRServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGetPost(MCRServletJob job) throws Exception {
        MCRJPortalAtoZListPageGenerator gen = new MCRJPortalAtoZListPageGenerator();
        // if journalList.xml not exists -> create it
        if(!gen.journalListExists()) {
            gen.createJournalList();
            gen.saveJournalList();
        } else {
            // load from file
            gen.loadJournalList();
        }
        Document journalListDocument = gen.getJournalList(); 
        getLayoutService().doLayout(job.getRequest(), job.getResponse(), journalListDocument);
    }

}