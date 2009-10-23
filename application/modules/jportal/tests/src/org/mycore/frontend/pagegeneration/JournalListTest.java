package org.mycore.frontend.pagegeneration;


import java.util.LinkedList;

import junit.framework.TestCase;

public class JournalListTest extends TestCase {

    public void testAdd() {
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Adler", "1");
        Entry entry2 = new Entry("das Auto", "2");
        Entry entry3 = new Entry("Am Ende", "3");
        
        assertTrue(journalList.add(entry0));
        assertTrue(journalList.add(entry1));
        assertTrue(journalList.add(entry2));
        assertTrue(journalList.add(entry3));
        
        assertFalse(journalList.isEmpty());
        assertEquals(journalList.getSections().first().getName(), "A");
        assertEquals(journalList.getSections().first().size(), 1);
        assertEquals(journalList.getSections().last().getName(), "D");
        assertEquals(journalList.getSections().last().size(), 3);
        assertEquals(journalList.getSections().size(), 2);
    }
    
    public void testAddAll(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Adler", "1");
        Entry entry2 = new Entry("das Auto", "2");
        Entry entry3 = new Entry("Am Ende", "3");
        
        LinkedList<Entry> list = new LinkedList<Entry>();
        list.add(entry0);
        list.add(entry1);
        list.add(entry2);
        list.add(entry3);
        
        assertTrue(journalList.addAll(list));
        
        assertTrue(journalList.contains(entry0));
        assertTrue(journalList.contains(entry1));
        assertTrue(journalList.contains(entry2));
        assertTrue(journalList.contains(entry3));
    }
    
    public void testContainsAll(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Adler", "1");
        Entry entry2 = new Entry("das Auto", "2");
        Entry entry3 = new Entry("Am Ende", "3");
        
        LinkedList<Entry> list = new LinkedList<Entry>();
        list.add(entry0);
        list.add(entry1);
        list.add(entry2);
        list.add(entry3);
        
        assertTrue(journalList.addAll(list));
        assertTrue(journalList.containsAll(list));
        
        list.add(new Entry("Test", "4"));
        
        assertFalse(journalList.containsAll(list));
    }

    public void testRemove(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        
        journalList.add(entry0);
        
        assertTrue(journalList.size() > 0);
        
        assertTrue(journalList.remove(entry0));
        assertFalse(journalList.size() > 0);
    }
    
    public void testContains(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Foo", "0");
        
        journalList.add(entry0);
        
        assertTrue(journalList.contains(entry0));
        assertFalse(journalList.contains(entry1));
    }
    
    public void testIterator(){
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
        
        assertTrue(journalList.size() == 3);
        
        for (Entry entry : journalList) {
            System.out.println(entry.getTitle());
        }
    }
    
    public void testRetainAll(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Adler", "1");
        Entry entry2 = new Entry("das Auto", "2");
        Entry entry3 = new Entry("Zug", "3");
        Entry entry4 = new Entry("Am Ende", "4");
        
        LinkedList<Entry> list = new LinkedList<Entry>();
        list.add(entry0);
        list.add(entry1);
        list.add(entry2);
        list.add(entry3);
        
        assertTrue(journalList.addAll(list));
        
        LinkedList<Entry> retainList = new LinkedList<Entry>();
        retainList.add(entry0);
        
        assertTrue(journalList.retainAll(retainList));
        for (Entry entry : journalList) {
            System.out.println("retainAll: " + entry.getTitle());
        }
        assertEquals(journalList.size(), 1);
        
    }
    
    public void testAdd2EntriesSameTile(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        
        journalList.add(entry0);
        journalList.add(entry0);
        
        assertEquals(journalList.getSection(entry0).size(), 1);
    }
    
    public void testAdd2EntriesSameTileDiffID(){
        JournalList journalList = new JournalList();
        Entry entry0 = new Entry("Der Fuchs", "0");
        Entry entry1 = new Entry("Der Fuchs", "1");
        
        journalList.add(entry0);
        journalList.add(entry1);
        
        assertEquals(journalList.getSection(entry0).size(), 2);
    }
}
