package org.mycore.frontend.pagegeneration;


import java.util.TreeSet;

import junit.framework.TestCase;

import org.mycore.frontend.pagegeneration.JournalList.Section;

public class SectionTest extends TestCase {

    public void testSectionString() {
        Section section = new Section("A");
        assertNotNull(section);
    }

    public void testSetName() {
        JournalList.Section section = new JournalList.Section("A");
        assertEquals(section.getName(), "A");
        section.setName("B");
        assertEquals(section.getName(), "B");
    }

    public void testGetName() {
        JournalList.Section section = new JournalList.Section("A");
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
        JournalList.Section section = new JournalList.Section("A");
        section.add(new Entry("Der Fuchs", "0"));
        section.add(new Entry("Der Adler", "1"));
        
//        TreeSet<Entry> entries = section.getEntries();
//        assertEquals(entries.first().getTitle(), "Der Adler");
//        assertEquals(entries.last().getTitle(), "Der Fuchs");
        assertEquals(section.first().getTitle(), "Der Adler");
        assertEquals(section.last().getTitle(), "Der Fuchs");
    }

    public void testRemoveEntry() {
        JournalList.Section section = new JournalList.Section("A");
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
        JournalList.Section sectionA = new JournalList.Section("A");
        JournalList.Section sectionB = new JournalList.Section("B");
        JournalList.Section sectionb = new JournalList.Section("b");
        
        assertEquals(sectionA.compareTo(sectionB), -1);
        assertEquals(sectionB.compareTo(sectionb), 0);
        assertEquals(sectionB.compareTo(sectionA), 1);
    }
}
