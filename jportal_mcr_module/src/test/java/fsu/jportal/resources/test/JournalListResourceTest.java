package fsu.jportal.resources.test;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

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
        public static HashMap<String, JournalList> journals = new HashMap<String, JournalList>();

        @Override
        public JournalList getList(String type) {
            return journals.get(type);
        }
        
    }
    
    @Test
    public void getJournalList() throws Exception {
        JournalList calendarList = getCalendarList();
        HashMap<String, JournalList> journals = new HashMap<String, JournalList>();
        journals.put(calendarList.getType(), calendarList);
        FakeJournalListBackend.journals = journals;
        
        System.setProperty(JournalListBackend.PROP_NAME, FakeJournalListBackend.class.getName());
        JournalList journalList = resource().path("journalList.xml").queryParam("type", "calendar").type(MediaType.APPLICATION_XML).get(JournalList.class);
        Journal journal = new Journal();
        journal.setId("testID");
        journal.setTitle("Title");
        ClientResponse response = resource().path("journalList.xml/add/calendar").type(MediaType.APPLICATION_XML).put(ClientResponse.class, journal);
        
        assertEquals(Status.CREATED, response.getClientResponseStatus());
        JaxbTools.marschall(FakeJournalListBackend.journals.get("calendar"), System.out);
        assertNotNull(FakeJournalListBackend.journals.get("calendar").getSection("T"));

        assertJournalList(journalList);
    }

    public JournalList getCalendarList() {
        InputStream testJournalList = getClass().getResourceAsStream("/testData/xml/journalLists/calendar.xml");
        try {
                return JaxbTools.unmarschall(testJournalList, JournalList.class);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    @Test
    public void journalListJaxb() throws Exception {
        InputStream journalListXMLStream = getClass().getResourceAsStream("/testData/xml/journalList.xml");
        JournalList journalList = JaxbTools.unmarschall(journalListXMLStream, JournalList.class);

        assertJournalList(journalList);
    }

    public void assertJournalList(JournalList journalList) {
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
