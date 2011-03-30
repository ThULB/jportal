package fsu.jportal.jaxb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class JournalList {
    

    public interface KeyAble {
        public String getKey();
    }

    @XmlRootElement
    public static class Journal implements KeyAble{
        private String title;

        private String id;
        
        public Journal() {
        }
        
        public Journal(String id, String title) {
            setId(id);
            setTitle(title);
        }

        public void setTitle(String title) {
            this.title = title;
        }

        @XmlAttribute
        public String getTitle() {
            return title;
        }

        public void setId(String value) {
            this.id = value;
        }

        @XmlValue
        public String getId() {
            return id;
        }

        @Override
        public String getKey() {
            return getId();
        }

    }

    public static class Section implements KeyAble{
        private String name;

        private Map<String, Journal> journals;
        
        public Section() {
            
        }

        public Section(String name) {
            setName(name);
        }

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setJournals(List<Journal> journals) {
            for (Journal journal : journals) {
                addJournal(journal);
            }
        }

        @XmlElement(name = "journal")
        public List<Journal> getJournals() {
            ArrayList<Journal> journalList = new ArrayList<Journal>();
            if(journals != null){
                journalList.addAll(journals.values());
            }
            Comparator<? super Journal> comparator = new Comparator<Journal>() {

                @Override
                public int compare(Journal o1, Journal o2) {
                    return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                }
            };
            Collections.sort(journalList, comparator);
            return journalList;
        }

        public Journal newJournal() {
            Journal journal = new Journal();
            addJournal(journal);
            return journal;
        }

        public void addJournal(Journal journal) {
            if (journals == null) {
                journals = new TreeMap<String, JournalList.Journal>();
            }

            journals.put(journal.getId(), journal);
        }

        public Journal getJournal(String id) {
            return journals.get(id);
        }

        @Override
        public String getKey() {
            return getName();
        }

        public boolean delJournal(String journalID) {
            Journal remove = journals.remove(journalID);

            if(remove != null){
                return true;
            }
            
            return false;
        }

        public Journal newJournal(String id, String title) {
            Journal journal = new Journal(id, title);
            addJournal(journal);
            return journal;
        }
    }

    private String mode;

    private String type;
    
    private String url;

    private Map<String, Section> sections;

    public void setMode(String mode) {
        this.mode = mode;
    }

    @XmlAttribute
    public String getMode() {
        return mode;
    }

    public void setType(String type) {
        this.type = type;
    }

    @XmlAttribute
    public String getType() {
        return type;
    }

    public void setSectionList(List<Section> sectionList) {
        for (Section section : sectionList) {
            addSection(section);
        }
    }

    @XmlElement(name = "section")
    public List<Section> getSectionList() {
        ArrayList<Section> sectionList = new ArrayList<Section>();
        if(getSections() != null){
            sectionList.addAll(getSections().values());
        }
        
        return sectionList;
    }
    
    public void addSection(Section section) {
        if (getSections() == null) {
            setSections(new TreeMap<String, JournalList.Section>());
        }
        
        getSections().put(section.getName(), section);
    }

    public Section newSection(String name) {
        Section section = new Section(name);
        addSection(section);
        return section;
    }

    public Section getSection(String key) {
        if(getSections() == null){
            return null;
        }
        
        return getSections().get(key);
    }

    public boolean delJournal(String journalID) {
        if (getSections() == null) {
            return false;
        }

        Collection<Section> values = getSections().values();
        for (Section section : values) {
            if(section.delJournal(journalID)){
                if(section.getJournals().isEmpty()){
                    values.remove(section);
                }
                return true;
            }
        }
        
        return false;
    }
    
    public Journal newJournal(String id, String title){
        String sectionName = getSectionName(title);
        Section section = getOrCreateSection(sectionName);
        return section.newJournal(id,title);
    }
    
    public void addJournal(Journal journal){
        String sectionName = getSectionName(journal);
        Section section = getOrCreateSection(sectionName);

        section.addJournal(journal);
    }
    
    private String getSectionName(Journal journal) {
        return getSectionName(journal.getTitle());
    }

    private String getSectionName(String title) {
        return title.toUpperCase().substring(0, 1);
    }
    
    private Section getOrCreateSection(String sectionName) {
        Section section = getSection(sectionName);
        if (section == null) {
            section = newSection(sectionName);
        }
        return section;
    }
    
    public void setSections(Map<String, Section> sections) {
        this.sections = sections;
    }

    public Map<String, Section> getSections() {
        return sections;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute
    public String getUrl() {
        return url;
    }
}
