package fsu.jportal.resources.test;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Properties;

import org.custommonkey.xmlunit.XMLUnit;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.content.MCRContent;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;
import org.mycore.datamodel.ifs2.MCRStoredMetadata;
import org.mycore.frontend.jersey.MCRJerseyResourceTest;

import fsu.jportal.mocks.LinkTableStoreMock;
import fsu.jportal.resources.MODSLogoResource;


public class MODSLogoResourceTest extends MCRJerseyResourceTest {
    public static class MyDataStore extends MCRMetadataStore{
        private HashMap<Integer, MCRStoredMetadata> storedMetadata;
        public MyDataStore() throws IOException, JDOMException {
            storedMetadata = new HashMap<Integer, MCRStoredMetadata>();
            createMetadataMock("journal", 2);
            createMetadataMock("inst", 30);
        }

        protected MCRStoredMetadata createMetadataMock(String metadataType, int id) throws IOException, JDOMException {
            String objID = metadataType + id;
            MCRStoredMetadata journalMetadataMock = createMock(objID + "MetadataMock", MCRStoredMetadata.class);
            MCRContent contentMock = createMock(objID + "ContentMock", MCRContent.class);
            expect(journalMetadataMock.getMetadata()).andReturn(contentMock);
            try {
                expect(contentMock.asXML()).andReturn(createObjXML(objID));
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            replay(journalMetadataMock, contentMock);
            
            storedMetadata.put(id, journalMetadataMock);
            return journalMetadataMock;
        }

        protected Document createObjXML(String id) throws JDOMException, IOException {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document journalXML = saxBuilder.build(getClass().getResourceAsStream("/logoRescourceTest/" + id + ".xml"));
            return journalXML;
        }
        
        @Override
        public MCRStoredMetadata retrieve(int id) throws IOException {
            return storedMetadata.get(id);
        }
    }
    
    String jpjournal = "jportal_jpjournal";
    String jpinst = "jportal_jpinst";
    
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Metadata.Type.jpjournal", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jpvolume", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.jpinst", "true");
        mcrProperties.setProperty("MCR.Metadata.Type.person", "true");
        mcrProperties.setProperty("MCR.Metadata.Store.BaseDir", "/tmp");
        mcrProperties.setProperty("MCR.Metadata.Store.SVNBase", "/tmp/versions");
        setupIFS2ForType(mcrProperties, jpjournal);
        setupIFS2ForType(mcrProperties, jpinst);
        mcrProperties.setProperty("MCR.EventHandler.MCRObject.2.Class", "org.mycore.datamodel.common.MCRXMLMetadataEventHandler");
        mcrProperties.setProperty("MCR.Persistence.LinkTable.Store.Class", LinkTableStoreMock.class.getName());
        
        try {
            MCRStoreManager.createStore(jpjournal, MyDataStore.class);
            MCRStoreManager.createStore(jpinst, MyDataStore.class);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    protected void setupIFS2ForType(Properties mcrProperties, String type) {
        mcrProperties.setProperty("MCR.IFS2.Store." + type + ".ForceXML", "true");
        mcrProperties.setProperty("MCR.IFS2.Store." + type + ".BaseDir", "ram:///tmp");
        mcrProperties.setProperty("MCR.IFS2.Store." + type + ".SlotLayout", "4-2-2");
        mcrProperties.setProperty("MCR.IFS2.Store." + type + ".SVNRepositoryURL", "ram:///tmp");
    }
    
    @After
    public void cleanUp(){
        MCRStoreManager.removeStore(jpjournal);
        MCRStoreManager.removeStore(jpinst);
    }
    
    @Test
    public void getLogo() throws Exception {
        String response = resource().path("/modslogos/jportal_jpjournal_00000002").get(String.class);
        
//        System.out.println("response: " + response);
        XMLUnit.setIgnoreWhitespace(true);
        Reader controlDoc = new InputStreamReader(getClass().getResourceAsStream("/logoRescourceTest/result.xml"));
        Reader testDoc = new StringReader(response);
        assertTrue(XMLUnit.compareXML(controlDoc, testDoc).identical());
    }
    
    @Override
    protected String[] getPackageName() {
        return new String[]{MODSLogoResource.class.getPackage().getName()};
    }

}
