package fsu.jportal.resources.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.common.MCRLinkTableInterface;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sun.jersey.api.client.ClientResponse;

import fsu.jportal.metadata.RubricLabel;
import fsu.jportal.metadata.XMLMetaElement;
import fsu.jportal.resources.ClassificationResource;
import fsu.testcase.JerseyResourceTestCase;


public class ClassificationResourceTest extends JerseyResourceTestCase{
    public static class FakeLinkTable implements MCRLinkTableInterface{

        @Override
        public void create(String from, String to, String type, String attr) {
            
        }

        @Override
        public void delete(String from, String to, String type) {
            
        }

        @Override
        public int countTo(String fromtype, String to, String type, String restriction) {
            return 0;
        }

        @Override
        public Map<String, Number> getCountedMapOfMCRTO(String mcrtoPrefix) {
            return new HashMap<String, Number>();
        }

        @Override
        public Collection<String> getSourcesOf(String to, String type) {
            return new ArrayList<String>();
        }

        @Override
        public Collection<String> getDestinationsOf(String from, String type) {
            return new ArrayList<String>();
        }}
    
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("MCR.Metadata.Type.jpclassi", "true");
        mcrProperties.setProperty("MCR.Metadata.Store.BaseDir", "/tmp");
        mcrProperties.setProperty("MCR.Metadata.Store.SVNBase", "/tmp/versions");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.ForceXML", "true");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.BaseDir", "ram:///tmp");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.SlotLayout", "4-2-2");
        mcrProperties.setProperty("MCR.IFS2.Store.jportal_jpclassi.SVNRepositoryURL", "ram:///tmp");
        mcrProperties.setProperty("MCR.EventHandler.MCRObject.2.Class", "org.mycore.datamodel.common.MCRXMLMetadataEventHandler");
        mcrProperties.setProperty("MCR.Persistence.LinkTable.Store.Class", FakeLinkTable.class.getName());
        
        try {
            MCRStoreManager.createStore("jportal_jpclassi", MCRMetadataStore.class);
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @After
    public void cleanUp(){
        MCRStoreManager.removeStore("jportal_jpclassi");
    }
    
    @Test
    public void createClassification() throws Exception {
        XMLMetaElement<RubricLabel> rubric = new XMLMetaElement<RubricLabel>("rubric");
        rubric.addMetaElemEntry(new RubricLabel("de", "Rubriken Test fuer MyCoRe", "test de"));
        rubric.addMetaElemEntry(new RubricLabel("en", "Rubric test for MyCoRe", "test en"));
        
        Gson gson = new Gson();
        Type rubricType = new TypeToken<XMLMetaElement<RubricLabel>>() {}.getType();
        String metaElemAsJson = gson.toJson(rubric, rubricType);
        ClientResponse response = resource().path("/classifications").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, metaElemAsJson);
        
        URI location = response.getLocation();
        ClientResponse jsonResponse = resource().uri(location).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        String responseBody = jsonResponse.getEntity(String.class);
        assertEquals(metaElemAsJson, responseBody);

        String modifiedRubric = responseBody.replaceAll("MyCoRe", "Jportal");
        ClientResponse updateResponse = resource().uri(location).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, modifiedRubric);
        String modJsonResponse = resource().uri(location).type(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println(modJsonResponse);
        assertEquals(modifiedRubric, modJsonResponse);
        
        ClientResponse deleteResponse = resource().uri(location).delete(ClientResponse.class);
        assertEquals("Wrong HTTP status code,",Status.GONE.getStatusCode(), deleteResponse.getStatus());
        ClientResponse deletedJsonResponse = resource().uri(location).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(Status.NOT_FOUND.getStatusCode(), deletedJsonResponse.getStatus());
        
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{ClassificationResource.class.getPackage().getName()};
    }
}
