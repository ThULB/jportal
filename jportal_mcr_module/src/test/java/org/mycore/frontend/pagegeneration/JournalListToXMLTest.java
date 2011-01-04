package org.mycore.frontend.pagegeneration;

import static org.junit.Assert.*;

import java.io.IOException;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.junit.Test;

public class JournalListToXMLTest {

    @Test
    public void testJournalListXML() throws IOException {
        JournalListXML journalList = new JournalListXML();
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
        
        journalList.setType("foo");
        
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        outputter.output(new JournalListToXML(journalList), System.out);
    }
}
