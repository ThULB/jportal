package org.mycore.frontend.pagegeneration;


import junit.framework.TestCase;

import org.mycore.frontend.pagegeneration.JournalListXML.Section;

public class SectionTest extends TestCase {

    public void testSectionString() {
        Section section = new Section("A");
        assertNotNull(section);
    }

    public void testSetName() {
        JournalListXML.Section section = new JournalListXML.Section("A");
        assertEquals(section.getName(), "A");
        section.setName("B");
        assertEquals(section.getName(), "B");
    }

    public void testGetName() {
        JournalListXML.Section section = new JournalListXML.Section("A");
        assertEquals(section.getName(), "A");
    }

   /* public void testSetEntries() {
        JournalList.Section section = new JournalList.Section("A");
        TreeSet<Entry> entries = new TreeSet<Entry>();
        entries.add(new Entry("Erster", "1"));
        section.setEntries(entries);
        
        assertTrue(section.getEntries().size() == 1);
    }*/

    /*public void testGetEntries() {
        JournalList.Section section = new JournalList.Section("A");
        assertNotNull(section.getEntries());
    }*/

    public void testAddEntry() {
        JournalListXML.Section section = new JournalListXML.Section("A");
        section.add(new Entry("Der Fuchs", "0"));
        section.add(new Entry("Der Adler", "1"));
        
//        TreeSet<Entry> entries = section.getEntries();
//        assertEquals(entries.first().getTitle(), "Der Adler");
//        assertEquals(entries.last().getTitle(), "Der Fuchs");
        assertEquals(section.first().getTitle(), "Der Adler");
        assertEquals(section.last().getTitle(), "Der Fuchs");
    }

    public void testRemoveEntry() {
        JournalListXML.Section section = new JournalListXML.Section("A");
        Entry entry = new Entry("Der Fuchs", "0");
        
        section.add(entry);
        assertFalse(section.isEmpty());
        
        section.remove(entry);
        assertTrue(section.isEmpty());
        
        section.add(entry);
        assertFalse(section.isEmpty());
        Entry newEntry = new Entry("Der Fuchs", "0");
        assertNotSame(entry, newEntry);
        section.remove(newEntry);
        assertTrue(section.isEmpty());
    }

    public void testCompareTo(){
        JournalListXML.Section sectionA = new JournalListXML.Section("A");
        JournalListXML.Section sectionB = new JournalListXML.Section("B");
        JournalListXML.Section sectionb = new JournalListXML.Section("b");
        
        assertEquals(sectionA.compareTo(sectionB), -1);
        assertEquals(sectionB.compareTo(sectionb), 0);
        assertEquals(sectionB.compareTo(sectionA), 1);
    }
}
