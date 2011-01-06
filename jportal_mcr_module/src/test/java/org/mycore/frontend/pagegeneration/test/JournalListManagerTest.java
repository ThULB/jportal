package org.mycore.frontend.pagegeneration.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.ifs2.MCRStore.MCRStoreConfig;
import org.mycore.frontend.pagegeneration.JournalListCfg;
import org.mycore.frontend.pagegeneration.JournalListManager;
import org.mycore.frontend.pagegeneration.JournalListManager.JournalListManagerCfg;

import fsu.jportal.jaxb.JournalList;
import fsu.jportal.jaxb.JournalList.Journal;
import fsu.jportal.jaxb.JournalList.Section;

public class JournalListManagerTest {
    private JournalListManager journalListManager;

    @Before
    public void init(){
        MCRConfiguration.instance().getProperties().setProperty("JP.JournalList.IFS.Backend", FakeStoreCfg.class.getName());
        journalListManager = JournalListManager.instance(new FakeJournalListManagerCfg());
    }
    
    @Test
    public void addUpdateDelete() throws Exception {
        String testId = "jportal_jpjournal_00000250";
        add(testId);
        addNoType();
        update(testId);
        
        delete(testId);
    }

    private void update(String testId) throws JDOMException, IOException {
        InputStream testObjXMLUpdate = getClass().getResourceAsStream("/testData/xml/mcrObj/calendarObjUpdate.xml");
        Document xmlUpdate = new SAXBuilder().build(testObjXMLUpdate);
        journalListManager.updateJournal(xmlUpdate);
        
        JournalList journalList = journalListManager.getJournalList("calendar");
        Section sectionA = journalList.getSection("A");
        assertNull("There should be no section A", sectionA);
        Section sectionS = journalList.getSection("S");
        assertNotNull("There should be a section S", sectionS);
        Journal journal = sectionS.getJournal(testId);
        assertNotNull("There should be a journal with id " + testId, journal);
        assertEquals("Strassenbau", journal.getTitle());
    }

    private void delete(String testId) throws JAXBException {
        assertTrue("Could not delete journal " + testId, journalListManager.deleteJournal(testId));
        JournalList journalList1 = journalListManager.getJournalList("calendar");
        assertNull(journalList1.getSection("A"));
    }

    private void add(String testId) throws JDOMException, IOException {
        InputStream testObjXML = getClass().getResourceAsStream("/testData/xml/mcrObj/calendarObj.xml");
        Document xml = new SAXBuilder().build(testObjXML);
        journalListManager.addToJournalLists(xml);
        JournalList journalList = journalListManager.getJournalList("calendar");
        Section sectionA = journalList.getSection("A");
        assertNotNull("There should be a section A", sectionA);
        assertNotNull("There should be a journal with id jportal_jpjournal_00000250", sectionA.getJournal("jportal_jpjournal_00000250"));
    }
    
    private void addNoType() throws JDOMException, IOException {
        String testId = "jportal_jpjournal_00000251";
        InputStream testObjXML = getClass().getResourceAsStream("/testData/xml/mcrObj/calendarObjNoType.xml");
        Document xml = new SAXBuilder().build(testObjXML);
        journalListManager.addToJournalLists(xml);
        JournalList journalList = journalListManager.getJournalList("journal");
        Section sectionB = journalList.getSection("B");
        assertNotNull("There should be a section A", sectionB);
        assertNotNull("There should be a journal with id " + testId, sectionB.getJournal(testId));
    }

    class FakeJournalListManagerCfg implements JournalListManagerCfg{

        @Override
        public JournalListCfg getJournalListCfg() {
            return null;
        }

        @Override
        public String getJournalListLocation() {
            return null;
        }
        
    }
    
    public static class FakeStoreCfg implements MCRStoreConfig{

        @Override
        public String getID() {
            return "fakeStore";
        }

        @Override
        public String getBaseDir() {
            return "ram://"+getID();
        }

        @Override
        public String getSlotLayout() {
            return "4-2-2";
        }
        
    }
}
