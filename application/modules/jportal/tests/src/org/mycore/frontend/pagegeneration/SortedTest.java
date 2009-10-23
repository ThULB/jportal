package org.mycore.frontend.pagegeneration;

import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.mycore.frontend.pagegeneration.JournalList.Section;

import junit.framework.TestCase;


public class SortedTest extends TestCase {
    public void testTreeSet(){
        TreeSet<String> set = new TreeSet<String>();
        
        set.add(new String("hallo"));
        set.add(new String("hallo"));
        
        assertTrue(set.size() == 1);
    }
    
    public void testTreeMap(){
        TreeMap<String, String> map = new TreeMap<String, String>();
        
        map.put(new String("hallo"), "du");
        map.put(new String("hallo"), "world");
        
        assertTrue(map.size() == 1);
    }
    
    public void testTreeSet_Entry(){
        TreeSet<Entry> set = new TreeSet<Entry>();
        
        set.add(new Entry("Der Fuchs", "0"));
        set.add(new Entry("Der Adler", "1"));
        
        assertTrue(set.size() == 2);
        assertEquals(set.first().getTitle(), "Der Adler");
    }
    
    public void testTreeSet_Section(){
        TreeSet<Section> set = new TreeSet<Section>();
        
        Section sectionA = new Section("A");
        Section sectionB = new Section("B");
        Section sectionB_ = new Section("B");
        Section sectionC = new Section("C");
        Section sectionD = new Section("D");
        
        set.add(sectionA);
        set.add(sectionB);
        set.add(sectionC);
        
        Section first = set.tailSet(sectionB_).first();
        assertSame(first, sectionB);
        assertNotSame(first, sectionB_);
        
        String name = first.getName();
        assertEquals(name, "B");
        
        SortedSet<Section> tailSet = set.tailSet(sectionD);
        assertNotNull(tailSet);
        assertFalse(tailSet.size() > 0);
    }
    
    public void testRetainAll(){
        TreeSet<String> set = new TreeSet<String>();
        
        LinkedList<String> list = new LinkedList<String>();
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("4");
        
        LinkedList<String> retainList = new LinkedList<String>();
        retainList.add("1");
        
        set.addAll(list);
        for (String string : set) {
            System.out.println("before: " + string);
        }
        
        set.retainAll(retainList);
        for (String string : set) {
            System.out.println("after: " + string);
        }
    }
}
