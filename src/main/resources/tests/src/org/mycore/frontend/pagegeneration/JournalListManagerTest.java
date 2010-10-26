package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.mycore.common.MCRTestCase;

public class JournalListManagerTest extends MCRTestCase {
    @Override
    protected void setUp() throws Exception {
        System.getProperties().setProperty("MCR.basedir", "resources");
        super.setUp();
    }

    public void testInstance() throws JDOMException, IOException {
        JournalListManager journalListManager = JournalListManager.instance();
        
        assertNotNull(journalListManager);
        
        JournalListCfg journalListCfg = journalListManager.getJournalListCfg();
        
        SAXBuilder builder = new SAXBuilder();
        Document cfgXML = builder.build(new File("resources/build/webapps/config/journalList.cfg.xml"));
        assertEquals(new JournalListCfgToXML(journalListCfg).toString(), cfgXML.toString());
    }

}
