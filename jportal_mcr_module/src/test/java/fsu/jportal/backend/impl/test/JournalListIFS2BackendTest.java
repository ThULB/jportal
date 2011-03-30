package fsu.jportal.backend.impl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRStore.MCRStoreConfig;
import org.mycore.datamodel.ifs2.MCRStoreManager;

import fsu.jportal.backend.api.JournalListBackend;
import fsu.jportal.backend.impl.JournalListIFS2Backend;
import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.jportal.jaxb.JournalList.Section;

public class JournalListIFS2BackendTest {
    private MCRFileStore fakeStore;
    private MCRStoreConfig storeConfig;

    @Before
    public void init() {
        storeConfig = new FakeStoreConfig();
        MCRStoreManager.removeStore(storeConfig.getID());
        try {
            fakeStore = MCRStoreManager.createStore(storeConfig, MCRFileStore.class);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    class FakeStoreConfig implements MCRStoreConfig {

        @Override
        public String getID() {
            return "fakeJournalList";
        }

        @Override
        public String getBaseDir() {
            return "ram://" + getID();
        }

        @Override
        public String getSlotLayout() {
            return "4-2-2";
        }

    }

    @Test
    public void getList() throws Exception {
        String listType = "calendar";
        
        JournalListBackend journalListIFS2Backend = new JournalListIFS2Backend(storeConfig);
        JournalList journalList = new JournalList();
        journalList.setType(listType);
        String testID = "test_id1";
        Journal journal = journalList.newJournal(testID, "Java Programming");
        journalListIFS2Backend.saveList(journalList);
        
        assertNotNull("Could not create new Journal", journal);

        JournalList retrievedJournalList = journalListIFS2Backend.getList(listType);

        assertNotNull("Journal list " + listType + " should not be null", retrievedJournalList);
        Section retrievedSection = retrievedJournalList.getSection("J");
        assertNotNull("Missing section 'J'", retrievedSection);
        assertNotNull("No journal with id " + testID, retrievedSection.getJournal(testID));
        assertEquals(1, retrievedSection.getJournals().size());
        
        retrievedJournalList.newJournal("test_id2", "C Programming");
        retrievedJournalList.newJournal("test_id3", "Jetty, the definite guide");
        retrievedJournalList.newJournal("test_id4", "Jebby, the fake guide");
        journalListIFS2Backend.saveList(retrievedJournalList);
        
        JournalList retrievedJournalListAgain = journalListIFS2Backend.getList(listType);
        Section sectionC = retrievedJournalListAgain.getSection("C");
        Section sectionJ = retrievedJournalListAgain.getSection("J");
        assertNotNull("Missing section 'C'", sectionC);
        assertEquals(3, sectionJ.getJournals().size());
        assertEquals("Java Programming", sectionJ.getJournals().get(0).getTitle());
        assertEquals("Jebby, the fake guide", sectionJ.getJournals().get(1).getTitle());
        assertEquals("Jetty, the definite guide", sectionJ.getJournals().get(2).getTitle());
    }
    
}
