package org.mycore.frontend.pagegeneration;

import java.io.IOException;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import junit.framework.TestCase;

public class JournalListXMLTest extends TestCase {

    public void testJournalListXML() throws IOException {
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Adler", "1");
        Entry entry2 = new Entry("das Auto", "2");
        Entry entry3 = new Entry("Zug", "3");
        Entry entry4 = new Entry("Am Ende", "4");
        
        assertTrue(journalList.add(entry0));
        assertTrue(journalList.add(entry1));
        assertTrue(journalList.add(entry2));
        assertTrue(journalList.add(entry4));
        assertTrue(journalList.add(entry3));
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(new JournalListToXML(journalList), System.out);
    }

}
