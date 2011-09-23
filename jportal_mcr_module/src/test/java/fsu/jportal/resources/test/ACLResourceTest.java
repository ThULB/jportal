package fsu.jportal.resources.test;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.spi.container.ResourceFilter;

import fsu.jportal.config.ResourceSercurityConf;
import fsu.jportal.gson.GsonManager;
import fsu.jportal.gson.RegResourceCollection;
import fsu.jportal.gson.RegResourceCollectionTypeAdapter;
import fsu.jportal.resources.ClassificationResource;
import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory;
import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory.AccesManagerConnector;
import fsu.testcase.JerseyResourceTestCase;

public class ACLResourceTest extends JerseyResourceTestCase{
    public static class MyAccessManagerConnector implements AccesManagerConnector{
        private HashMap<String, Boolean> permissions = new HashMap<String, Boolean>();
        
        public MyAccessManagerConnector() {
            permissions.put(decodeRule(TestResource.class.getName(), "/auth_GET"), true);
            permissions.put(decodeRule(TestResource.class.getName(), "/auth/logout/{id}_GET"), false);
        }
        
        private String decodeRule(String id, String permission){
            return id + "::" + permission;
        }

        @Override
        public boolean checkPermission(String id, String permission, MCRSession session) {
            Boolean perm = permissions.get(decodeRule(id, permission));
            if(perm == null){
                throw new RuntimeException("could not find permisson for: " + id + " # " +permission);
            }
            
            return perm;
        }
        
    }
    
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
        Properties mcrProperties = MCRConfiguration.instance().getProperties();
        mcrProperties.setProperty("McrSessionSecurityFilter.MCRAccessManager.Connector", MyAccessManagerConnector.class.getName());
    }
    
    //@Test
    public void acl() throws Exception {
        String response = resource().path("/acl/rsc").type(MediaType.APPLICATION_JSON).get(String.class);
        System.out.println("response: " + response);
        
        for (Entry<String, JsonElement> entry : parseJson(response).entrySet()) {
            System.out.println("parsed : " + entry.getKey());
            String uri = entry.getValue().getAsString();
            System.out.println("URI: " + uri);
            String uriresponse = resource().uri(URI.create(uri)).type(MediaType.APPLICATION_JSON).get(String.class);
            System.out.println("URI: " + uriresponse);
            for (Entry<String, JsonElement> entry2 : parseJson(uriresponse).entrySet()) {
                String uri2 = entry2.getValue().getAsString();
                System.out.println("URI2: " + uri2);
                String uriresponse2 = resource().uri(URI.create(uri2)).type(MediaType.APPLICATION_JSON).get(String.class);
                System.out.println("URI2: " + uriresponse2);
            }
        }
        
    }

    protected JsonObject parseJson(String response) {
        JsonParser jsonParser = new JsonParser();
        JsonElement jsonElement = jsonParser.parse(response);
        JsonObject asJsonObject = jsonElement.getAsJsonObject();
        return asJsonObject;
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{ClassificationResource.class.getPackage().getName()};
    }

    @Override
    protected Map<String, String> getInitParams() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ResourceFilter.class.getName() + "s", MyCoReSecurityFilterFactory.class.getName());
        return initParams;
    }

}
