package fsu.jportal.resources.test;

import static org.junit.Assert.*;

import javax.ws.rs.core.MediaType;

import org.junit.Test;

import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.jportal.jaxb.JournalList.Section;
import fsu.jportal.resources.JournalListResource;
import fsu.testcase.JerseyResourceTestCase;
import fsu.thulb.jaxb.JaxbTools;

public class JournalListResourceTest extends JerseyResourceTestCase {
    @Test
    public void getJournalList() throws Exception {
//        resource().path("journalList.xml").type(MediaType.APPLICATION_XML).get(JournalList.class);
        
        JournalList journalList = new JournalList();
        journalList.setMode("alphabetical");
        journalList.setType("calendar");
        Section section = journalList.newSection();
        section.setName("A");
        Journal journal = section.newJournal();
        journal.setTitle("Title");
        journal.setValue("Value");
        JaxbTools.marschall(journalList, System.out);
        
    }
    
    @Test
    public void journalListJaxb() throws Exception {
        JournalList journalList = new JournalList();
        journalList.setMode("alphabetical");
        journalList.setType("calendar");
        Section section = journalList.newSection();
        section.setName("A");
        Journal journal = section.newJournal();
        journal.setTitle("Title");
        journal.setValue("Value");
        JaxbTools.marschall(journalList, System.out);
    }

    @Override
    protected String[] getPackageName() {
        return new String[] { JournalListResource.class.getPackage().getName() };
    }

}
