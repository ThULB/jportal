package fsu.jportal.backend.impl.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mycore.datamodel.ifs2.MCRContent;
import org.mycore.datamodel.ifs2.MCRFile;
import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;

import fsu.jportal.backend.impl.JournalListBackendImpl;
import fsu.jportal.jaxb.JournalList;

public class JournalListBackendImplTest {
    @Test
    public void getList() throws Exception {
        System.setProperty("MCR.IFS2.Store.fake.BaseDir", "fake");
        System.setProperty("MCR.IFS2.Store.fake.SlotLayout", "4-2-2");
        MCRFileStore fakeStore = MCRStoreManager.createStore("fake", MCRFileStore.class);
        MCRFileCollection fileCollection = fakeStore.create(1);
        MCRFile calenderListFile = fileCollection.createFile("calenderList");
        
        
//        calenderListFile.setContent(MCRContent.readFrom(calenderListStream));
        JournalListBackendImpl journalListBackendImpl = new JournalListBackendImpl(fakeStore);
        String listType = "calendar";
        JournalList journalList = journalListBackendImpl.getList(listType);
        
        assertNotNull("Journal list " + listType + " should not be null",journalList);
    }
}
