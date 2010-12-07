package fsu.jportal.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

@XmlRootElement
public class JournalList {
    public static class Journal {
        private String title;

        private String value;

        public void setTitle(String title) {
            this.title = title;
        }

        @XmlAttribute
        public String getTitle() {
            return title;
        }

        public void setValue(String value) {
            this.value = value;
        }

        @XmlValue
        public String getValue() {
            return value;
        }

    }

    public static class Section {
        private String name;

        private List<Journal> journalList;

        public void setName(String name) {
            this.name = name;
        }

        @XmlAttribute
        public String getName() {
            return name;
        }

        public void setJournalList(List<Journal> journal) {
            this.journalList = journal;
        }

        @XmlElement(name = "journal")
        public List<Journal> getJournalList() {
            return journalList;
        }

        public Journal newJournal() {
            Journal journal = new Journal();
            addJournal(journal);
            return journal;
        }

        public void addJournal(Journal journal) {
            if (this.journalList == null) {
                setJournalList(new ArrayList<JournalList.Journal>());
            }

            getJournalList().add(journal);
        }
    }

    private String mode;

    private String type;

    private List<Section> sectionList;

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

    public void setSectionList(List<Section> section) {
        this.sectionList = section;
    }

    @XmlElement(name = "section")
    public List<Section> getSectionList() {
        return sectionList;
    }

    public void addSection(Section section) {
        if (sectionList == null) {
            setSectionList(new ArrayList<JournalList.Section>());
        }

        getSectionList().add(section);
    }

    public Section newSection() {
        Section section = new Section();
        addSection(section);
        return section;
    }
}
