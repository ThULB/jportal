package fsu.jportal.resources.test;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
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
        System.out.println("URI: " + location);
        String jsonResponse = resource().uri(location).type(MediaType.APPLICATION_JSON).get(String.class);
        assertEquals(metaElemAsJson, jsonResponse);
        
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{ClassificationResource.class.getPackage().getName()};
    }
}
