package fsu.jportal.resources.test;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import fsu.jportal.resources.auth.AuthorizationResource;
import fsu.jportal.resources.filter.BindMcrSessionFilter;
import fsu.jportal.resources.filter.MyCoReSecurityFilter;
import fsu.testcase.JerseyResourceTestCase;

public class AuthTest extends JerseyResourceTestCase{
    public class FooObject{
        private String value = "foo";

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
    
    public class MyFilter1 implements ContainerRequestFilter{
        @Context
        HttpServletRequest httprequest;
        @Override
        public ContainerRequest filter(ContainerRequest request) {
            System.out.println("Hello Filter1!");
            System.out.println("Request: " + httprequest);
            HttpSession session = httprequest.getSession(true);
            System.out.println("Session: " + session.getId());
            
            request.getProperties().put("foo.obj", new FooObject());
            return request;
        }
        
    }
    
    public class MyFilter2 implements ContainerRequestFilter{
        
        @Override
        public ContainerRequest filter(ContainerRequest request) {
            System.out.println("Hello Filter2!");
            FooObject object = (FooObject) request.getProperties().get("foo.obj");
            System.out.println("Foo value: " + object.getValue());
            return request;
        }
        
    }
    
    @Before
    public void init() {
        System.setProperty("MCR.Configuration.File", "config/test.properties");
    }
    
    @Test
    public void auth() throws Exception {
        String string = resource().path("/auth").get(String.class);
        System.out.println(string);
        string = resource().path("/auth/logout/foo").get(String.class);
        System.out.println(string);
    }

    @Override
    protected String[] getPackageName() {
        return new String[]{AuthorizationResource.class.getPackage().getName()};
    }

    @Override
    protected Map<String, String> getInitParams() {
        Map<String, String> initParams = new HashMap<String, String>();
//        initParams.put(ContainerRequestFilter.class.getName() + "s", BindMcrSessionFilter.class.getName());
        initParams.put(ResourceFilter.class.getName() + "s", MyCoReSecurityFilter.class.getName());
        return initParams;
    }

}
