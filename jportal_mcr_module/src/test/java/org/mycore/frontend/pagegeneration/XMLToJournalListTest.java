package org.mycore.frontend.pagegeneration;

import java.io.IOException;

import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLToJournalListTest extends TestCase {

    public void testXMLToJournalList() throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document xml = builder.build(this.getClass().getResourceAsStream("/journalList.xml"));
        assertNotNull(xml);
        
        XMLToJournalList journalList = new XMLToJournalList(xml);
        
        for (Entry entry : journalList) {
            System.out.println(entry.getTitle());
        }
    }

}
