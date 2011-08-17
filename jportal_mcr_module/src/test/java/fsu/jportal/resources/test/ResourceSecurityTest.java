package fsu.jportal.resources.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.Response.Status;

import org.junit.Before;
import org.junit.Test;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.spi.container.ResourceFilter;

import fsu.jportal.config.ResourceSercurityConf;
import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory;
import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory.AccesManagerConnector;
import fsu.testcase.JerseyResourceTestCase;

public class ResourceSecurityTest extends JerseyResourceTestCase{
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
    
    @Test
    public void testResourceSecurity() throws Exception {
        ClientResponse response = resource().path("/auth").get(ClientResponse.class);
        
        assertEquals(Status.OK.getStatusCode(), response.getStatus());
        response = resource().path("/auth/logout/foo").get(ClientResponse.class);
        assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
        assertEquals(1, ResourceSercurityConf.instance().getResourceRegister().size());
        List<String> testResourceEntry = ResourceSercurityConf.instance().getResourceRegister().get(TestResource.class.getName());
        assertNotNull(TestResource.class.getName() + " should has been registered", testResourceEntry);
        assertEquals(2, testResourceEntry.size());
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{TestResource.class.getPackage().getName()};
    }

    @Override
    protected Map<String, String> getInitParams() {
        Map<String, String> initParams = new HashMap<String, String>();
        initParams.put(ResourceFilter.class.getName() + "s", MyCoReSecurityFilterFactory.class.getName());
        return initParams;
    }

}
