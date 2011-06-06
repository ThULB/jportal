package fsu.jportal.resources.test;

import static org.junit.Assert.assertEquals;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.classifications2.MCRCategory;
import org.mycore.datamodel.classifications2.MCRCategoryDAOFactory;
import org.mycore.datamodel.classifications2.MCRCategoryID;
import org.mycore.datamodel.classifications2.MCRLabel;
import org.mycore.datamodel.classifications2.impl.MCRCategoryImpl;
import org.mycore.datamodel.common.MCRLinkTableInterface;
import org.mycore.datamodel.ifs2.MCRMetadataStore;
import org.mycore.datamodel.ifs2.MCRStoreManager;

import com.google.gson.Gson;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import fsu.jportal.gson.GsonManager;
import fsu.jportal.mocks.FakeCategoryDAO;
import fsu.jportal.resources.ClassificationResource;
import fsu.jportal.utils.MCRCategUtils;
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
        mcrProperties.setProperty("MCR.Category.DAO", FakeCategoryDAO.class.getName());
        mcrProperties.setProperty("ClassificationResouce.useSession", "false");
        
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
        MCRConfiguration.instance().set("MCR.Category.DAO", null);
    }
    
    @Test
    public void createClassification() throws Exception {
//        String serializedRubric = rubricJsonStr();
//        URI rubricLocation = assertCreateRubric(serializedRubric);
//        assertRootCategs();
//        
//        assertGetRubric(rubricLocation, serializedRubric);
//        assertUpdateRubric(rubricLocation);
//        URI subRubricLocation = assertCreateSubRubric(rubricLocation);
//        String rootID = getQueryMap(rubricLocation).get("rootID");
//        String responseStr = resource().path("/classifications/children").queryParam("rootID", rootID).type(MediaType.APPLICATION_JSON).get(String.class);
//        assertDeleteRubric(rubricLocation, subRubricLocation);
    }

    private void assertRootCategs() {
        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        labels.add(new MCRLabel("de", "Rubriken Test 2 fuer MyCoRe", "test de"));
        labels.add(new MCRLabel("en", "Rubric test 2 for MyCoRe", "test en"));
        MCRCategory category = MCRCategUtils.newCategory(null, labels, null);
        Gson gson = GsonManager.instance().createGson();
        String serializedRubric = gson.toJson(category);
        ClientResponse response = resource().path("/classifications").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, serializedRubric);
        assertEquals("could not create rubric: ", Status.CREATED.getStatusCode(), response.getClientResponseStatus().getStatusCode());
        String responseStr = resource().path("/classifications").type(MediaType.APPLICATION_JSON).get(String.class);
        List<MCRCategory> rootCategories = MCRCategoryDAOFactory.getInstance().getRootCategories();
        String rootsCategsStr = gson.toJson(rootCategories);
        assertEquals(rootsCategsStr, responseStr);
    }

    private void assertDeleteRubric(URI rubricLocation, URI subRubricLocation) {
        ClientResponse deleteResponse = resource().uri(rubricLocation).delete(ClientResponse.class);
        assertEquals("Wrong HTTP status code,",Status.GONE.getStatusCode(), deleteResponse.getStatus());
        ClientResponse delRubricResponse = resource().uri(rubricLocation).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(Status.NOT_FOUND.getStatusCode(), delRubricResponse.getStatus());
        ClientResponse delSubRubricResponse = resource().uri(subRubricLocation).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        assertEquals(Status.NOT_FOUND.getStatusCode(), delSubRubricResponse.getStatus());
    }

    private void assertUpdateRubric(URI rubricLocation) {
        ClientResponse jsonResponse = resource().uri(rubricLocation).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        String serializedRubric = jsonResponse.getEntity(String.class);
        String modifiedRubric = serializedRubric.replaceAll("MyCoRe", "Jportal");
        ClientResponse updateResponse = resource().uri(rubricLocation).type(MediaType.APPLICATION_JSON).put(ClientResponse.class, modifiedRubric);
        assertEquals(Status.OK.getStatusCode(), updateResponse.getStatus());
        String modJsonResponse = resource().uri(rubricLocation).type(MediaType.APPLICATION_JSON).get(String.class);
        assertEquals(modifiedRubric, modJsonResponse);
    }

    private void assertGetRubric(URI rubricLocation, String serializedRubric) {
        ClientResponse jsonResponse = resource().uri(rubricLocation).type(MediaType.APPLICATION_JSON).get(ClientResponse.class);
        String categoryJsonStr = jsonResponse.getEntity(String.class);
        assertEquals(serializedRubric, removeCategID(categoryJsonStr));
    }

    private String removeCategID(String categoryJsonStr) {
        Gson gson = GsonManager.instance().createGson();
        MCRCategoryImpl receivedCategory = gson.fromJson(categoryJsonStr, MCRCategoryImpl.class);
        receivedCategory.setId(null);
        String responseBody = gson.toJson(receivedCategory);
        return responseBody;
    }

    private String rubricJsonStr() {
        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        labels.add(new MCRLabel("de", "Rubriken Test fuer MyCoRe", "test de"));
        labels.add(new MCRLabel("en", "Rubric test for MyCoRe", "test en"));
        MCRCategory category = MCRCategUtils.newCategory(null, labels, null);
        Gson gson = GsonManager.instance().createGson();
        String serializedRubric = gson.toJson(category);
        return serializedRubric;
    }
    
    private URI assertCreateRubric(String serializedRubric){
        ClientResponse response = resource().path("/classifications").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, serializedRubric);
        assertEquals("could not create rubric: ", Status.CREATED.getStatusCode(), response.getClientResponseStatus().getStatusCode());
        return response.getLocation();
    }
    
    private URI assertCreateSubRubric(URI parentURI){
        ClientResponse response = resource().path("/classifications").type(MediaType.APPLICATION_JSON).post(ClientResponse.class, subRubricStr(parentURI));
        assertEquals("could not create sub rubric: ", Status.CREATED.getStatusCode(), response.getClientResponseStatus().getStatusCode());
        return response.getLocation();
    }

    private String subRubricStr(URI parentURI) {
        Map<String, String> queryMap = getQueryMap(parentURI);
        
        String rootID = queryMap.get("rootID");
        String categID = queryMap.get("categID");
        
        MCRCategory parentCateg = MCRCategoryDAOFactory.getInstance().getCategory(new MCRCategoryID(rootID, categID), 0);
        
        Set<MCRLabel> labels = new HashSet<MCRLabel>();
        labels.add(new MCRLabel("de", "Unterrubriken Test fuer MyCoRe", "untertest de"));
        labels.add(new MCRLabel("en", "Subrubric test for MyCoRe", "subtest en"));
        MCRCategory subCateg = MCRCategUtils.newCategory(MCRCategoryID.rootID(rootID), labels, parentCateg);
        Gson gson = GsonManager.instance().createGson();
        String serializedRubric = gson.toJson(subCateg);
        return serializedRubric;
    }

    private Map<String, String> getQueryMap(URI parentURI) {
        HashMap<String, String> queryMap = new HashMap<String, String>();
        String queryParams = parentURI.getQuery();
        
        for (String queryParam : queryParams.split("&")) {
            String[] queryPair = queryParam.split("=");
            queryMap.put(queryPair[0], queryPair[1]);
        }
        
        return queryMap;
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{ClassificationResource.class.getPackage().getName()};
    }
}
