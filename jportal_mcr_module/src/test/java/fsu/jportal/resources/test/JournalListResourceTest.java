package fsu.jportal.resources.test;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.jportal.jaxb.JournalList.Section;
import fsu.jportal.resources.JournalListResource;
import fsu.testcase.JerseyResourceTestCase;
import fsu.thulb.jaxb.JaxbTools;

public class JournalListResourceTest extends JerseyResourceTestCase {
    public static class FakeJournalListBackend implements JournalListBackend{
        public static HashMap<String, JournalList> journals;
        
        @Override
        public JournalList getList(String type) {
            return journals.get(type);
        }
        
        public static void initJournalLists(){
            journals = new HashMap<String, JournalList>();
        }
        
        public static void addJournalList(JournalList journalList){
            journals.put(journalList.getType(), journalList);
        }

        @Override
        public JournalList createList(String type) {
            JournalList journalList = new JournalList();
            journalList.setType(type);
            journals.put(type, journalList);
            return journalList;
        }
    }
    
    @Before
    public void init() {
        FakeJournalListBackend.initJournalLists();
        System.setProperty(JournalListBackend.PROP_NAME, FakeJournalListBackend.class.getName());
    }
    
    @Test
    public void getJournalList() throws Exception {
        FakeJournalListBackend.addJournalList(getCalendarList("/testData/xml/journalLists/calendar.xml"));
        JournalList journalList = resource().path("journalList.xml").queryParam("type", "calendar").type(MediaType.APPLICATION_XML).get(JournalList.class);
        
        assertJournalList(journalList);
    }
    
    @Test
    public void addJournalIntoExistingList() throws Exception {
        FakeJournalListBackend.addJournalList(getCalendarList("/testData/xml/journalLists/calendar.xml"));
        
        Journal journal = new Journal();
        journal.setId("testID");
        journal.setTitle("Title");
        ClientResponse response = resource().path("journalList.xml/calendar").type(MediaType.APPLICATION_XML).post(ClientResponse.class, journal);
        
        assertEquals(Status.CREATED, response.getClientResponseStatus());
        
        JournalList calendarList = FakeJournalListBackend.journals.get("calendar");
        assertJournalList(calendarList);
        Section section = calendarList.getSection("T");
        assertNotNull("There should be a section 'T'.", section);
        assertNotNull("There should be a journal with ID 'testID'.", section.getJournal("testID"));
    }
    
    @Test
    public void addJournalIntoEmptyList() throws Exception {
        Journal journal = new Journal();
        journal.setId("testID");
        journal.setTitle("Title");
        ClientResponse response = resource().path("journalList.xml/calendar").type(MediaType.APPLICATION_XML).post(ClientResponse.class, journal);
        
        assertEquals(Status.CREATED, response.getClientResponseStatus());
        JournalList calendarList = FakeJournalListBackend.journals.get("calendar");
        Section section = calendarList.getSection("T");
        assertNotNull("There should be a section 'T'.", section);
        assertNotNull("There should be a journal with ID 'testID'.", section.getJournal("testID"));
    }
    
    @Test
    public void deleteJournal() throws Exception {
        FakeJournalListBackend.addJournalList(getCalendarList("/testData/xml/journalLists/calendar.xml"));
        String journalID = "book_001";
        ClientResponse response = resource().path("journalList.xml/calendar").path(journalID).type(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
        
        assertEquals(Status.OK, response.getClientResponseStatus());
        JournalList calendarList = FakeJournalListBackend.journals.get("calendar");
        Section section = calendarList.getSection("H");
        assertNotNull("There should be a section 'H'.", section);
        assertNull("There should be a journal with ID 'testID'.", section.getJournal(journalID));
    }
    
    @Test
    public void deleteLastJournalInSection() throws Exception {
        FakeJournalListBackend.addJournalList(getCalendarList("/testData/xml/journalLists/calendar.xml"));
        String journalID = "Value";
        ClientResponse response = resource().path("journalList.xml/calendar").path(journalID).type(MediaType.TEXT_PLAIN).delete(ClientResponse.class);
        
        assertEquals(Status.OK, response.getClientResponseStatus());
        JournalList calendarList = FakeJournalListBackend.journals.get("calendar");
        JaxbTools.marschall(calendarList, System.out);
        Section section = calendarList.getSection("A");
        assertNull("There should be no section 'A'.", section);
    }

    public JournalList getCalendarList(String journalListName) {
        InputStream testJournalList = getClass().getResourceAsStream(journalListName);
        try {
                return JaxbTools.unmarschall(testJournalList, JournalList.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Test
    public void journalListJaxb() throws Exception {
        InputStream journalListXMLStream = getClass().getResourceAsStream("/testData/xml/journalLists/calendar.xml");
        JournalList journalList = JaxbTools.unmarschall(journalListXMLStream, JournalList.class);

        assertJournalList(journalList);
    }

    public void assertJournalList(JournalList journalList) {
        assertNotNull("Journal list is null.", journalList);
        assertEquals("calendar", journalList.getType());
        assertEquals("alphabetical", journalList.getMode());
        List<Section> sectionList = journalList.getSectionList();
        assertNotNull(sectionList);
        Section section = journalList.getSection("A");
        assertNotNull("Could not found section 'A'.", section);
        assertEquals("A", section.getName());
        List<Journal> journals = section.getJournals();
        assertNotNull(journals);
        Journal journal = section.getJournal("Value");
        assertEquals("Title", journal.getTitle());
        assertEquals("Value", journal.getId());
    }

    @Override
    protected String[] getPackageName() {
        return new String[] { JournalListResource.class.getPackage().getName() };
    }

}
