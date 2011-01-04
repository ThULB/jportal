package org.mycore.frontend.pagegeneration;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;


public class JournalListXML implements Collection<Entry> {

    private TreeSet<Section> sections;
    private String type;

    public JournalListXML() {
        this(null);
    }

    public JournalListXML(String type) {
        this.type = type;
        this.sections = new TreeSet<Section>();
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public String getType() {
        return type;
    }

    public void setSections(TreeSet<Section> sections) {
        this.sections = sections;
    }
    
    public TreeSet<Section> getSections() {
        return sections;
    }

    public boolean isEmpty() {
        return (size() <= 0) ? true : false;
    }

    public Section getSection(Entry entry) {
        String sectionName = Character.toString(entry.getTitle().charAt(0)).toUpperCase();
        Section tmpSection = new Section(sectionName);

        SortedSet<Section> tailSet = getSections().tailSet(tmpSection);

        if (tailSet.size() > 0 && tailSet.contains(tmpSection)) {
            return tailSet.first();
        }

        return tmpSection;
    }

    public boolean add(Entry entry) {
        boolean changeStatus = false;

        Section section = getSection(entry);
        changeStatus = section.add(entry);

        if (section.size() == 1) {
            return getSections().add(section);
        }

        return changeStatus;
    }

    public boolean addAll(Collection<? extends Entry> c) {
        Collection<Entry> tmpCollection = new LinkedList<Entry>();
        for (Entry entry : c) {
            if (!add(entry)) {
                removeAll(tmpCollection);
                return false;
            } else {
                tmpCollection.add(entry);
            }
        }

        return true;
    }

    public void clear() {
        getSections().clear();
    }

    public boolean contains(Object o) {
        Entry entry = (Entry) o;
        Section section = getSection(entry);

        return section.contains(entry);
    }

    public boolean containsAll(Collection<?> c) {
        for (Iterator iterator = c.iterator(); iterator.hasNext();) {
            Entry entry = (Entry) iterator.next();
            Section section = getSection(entry);

            if (!section.contains(entry)) {
                return false;
            }
        }
        return true;
    }

    public Iterator<Entry> iterator() {
        return groupSections().iterator();
    }

    private TreeSet<Entry> groupSections() {
        TreeSet<Entry> entries = new TreeSet<Entry>();
        
        for (Section section : getSections()) {
            entries.addAll(section);
        }
        return entries;
    }

    public boolean remove(Object o) {
        Entry entry = (Entry) o;
        Section section = getSection(entry);

        if (section.contains(entry)) {
            boolean changeStatus = section.remove(entry);
            return (section.isEmpty()) ? getSections().remove(section) : changeStatus;
        }

        return false;
    }

    public boolean removeAll(Collection<?> c) {
        boolean changeStatus = false;

        for (Object object : c) {
            if (remove(object)) {
                changeStatus = true;
            }
        }

        return changeStatus;
    }

    public boolean retainAll(Collection<?> c) {
        boolean changeStatus = false;
        
        for (Section section : getSections()) {
            changeStatus = section.retainAll(c);
            if(section.isEmpty()){
                getSections().remove(section);
            }
        }
        
        return changeStatus;
    }

    public int size() {
        return getSections().size();
    }

    public Object[] toArray() {
        return groupSections().toArray();
    }

    public <T> T[] toArray(T[] a) {
        return groupSections().toArray(a);
    }

    // inner classes
    
    public static class Section extends TreeSet<Entry> implements Comparable<Section> {
        private String name;
        
        protected Section() {
        }

        public Section(String name) {
            setName(name);
        }

        public int compareTo(Section o) {
            return this.name.compareToIgnoreCase(o.getName());
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }
}
