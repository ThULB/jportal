package fsu.jportal.backend.impl.test;

import static org.junit.Assert.*;

import org.junit.Test;
import org.mycore.datamodel.ifs2.MCRDirectory;
import org.mycore.datamodel.ifs2.MCRFile;
import org.mycore.datamodel.ifs2.MCRFileCollection;
import org.mycore.datamodel.ifs2.MCRFileStore;
import org.mycore.datamodel.ifs2.MCRNode;
import org.mycore.datamodel.ifs2.MCRStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;

public class JournalListBackendImplTest {
    @Test
    public void getList() throws Exception {
        MCRStoreManager.createStore("fake", FakeStore.class);
    }
    
    class FakeStore extends MCRFileStore{
        
    }
}
