package org.mycore.frontend.pagegeneration;

import java.io.File;
import java.io.IOException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.common.MCRTestCase;
import org.mycore.frontend.pagegeneration.JournalListCfg.JournalListDef;

public class XMLToJournalListCfgTest extends MCRTestCase {

    public void testXMLToJournalListCfg() throws JDOMException, IOException {
        String cfgFileName = "resources/build/webapps/config/journalList.cfg.xml";
        SAXBuilder builder = new SAXBuilder();
        Document xml = builder.build(new File(cfgFileName));
        
        XMLToJournalListCfg journalListCfg = new XMLToJournalListCfg(xml);
        assertNotNull(journalListCfg);
        assertEquals(journalListCfg.getListDefs().size(), 2);
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        for (JournalListDef listDef : journalListCfg.getListDefs()) {
            System.out.println(listDef.getFileName());
            outputter.output(listDef.getQuery().buildXML(), System.out);
        }
    }

}
