package fsu.jportal.jaxb;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
public class JournalList {
    public interface KeyAble {
        public String getKey();
    }

    @XmlRootElement
    public static class Journal implements KeyAble{
        private String title;

        private String id;

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
            return journalList;
        }

        public Journal newJournal() {
            Journal journal = new Journal();
            addJournal(journal);
            return journal;
        }

        public void addJournal(Journal journal) {
            if (journals == null) {
                journals = new HashMap<String, JournalList.Journal>();
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
    }

    private String mode;

    private String type;

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
        if(sections != null){
            sectionList.addAll(sections.values());
        }
        
        return sectionList;
    }
    
    public void addSection(Section section) {
        if (sections == null) {
            sections = new HashMap<String, JournalList.Section>();
        }
        
        sections.put(section.getName(), section);
    }

    public Section newSection(String name) {
        Section section = new Section(name);
        addSection(section);
        return section;
    }

    public Section getSection(String key) {
        return sections.get(key);
    }
}
