package fsu.jportal.resources.filter;

import javax.servlet.http.HttpServletRequest;

import org.mycore.common.MCRSession;
import org.mycore.frontend.servlets.MCRServlet;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

public class BindMcrSessionFilter implements ResourceFilter{
    private RequestFilter requestFilter;
    
    private class RequestFilter implements ContainerRequestFilter{
        private HttpServletRequest httprequest;
        private String[] roles;
        
        public RequestFilter(HttpServletRequest httprequest, String[] roles) {
            this.httprequest = httprequest;
            this.roles = roles;
        }
        
        @Override
        public ContainerRequest filter(ContainerRequest request) {
            if (httprequest != null) {
                MCRSession currentSession = MCRServlet.getSession(httprequest);
                printSessionInfo(currentSession);
            }
            return request;
        }
        
        private void printSessionInfo(MCRSession currentSession) {
            String currentIP = currentSession.getCurrentIP();
            String currentLanguage = currentSession.getCurrentLanguage();
            String id = currentSession.getID();
            String currentUserID = currentSession.getUserInformation().getCurrentUserID();
            long lastAccessedTime = currentSession.getLastAccessedTime();
            long loginTime = currentSession.getLoginTime();

            System.out.println();
            System.out.println("IP: " + currentIP);
            System.out.println("lang: " + currentLanguage);
            System.out.println("ID: " + id);
            System.out.println("user: " + currentUserID);
            System.out.println("last time: " + lastAccessedTime);
            System.out.println("login time: " + loginTime);
            System.out.println();
        }
    }
    
    public BindMcrSessionFilter(HttpServletRequest httprequest, String[] roles, AbstractMethod am) {
        this.requestFilter = new RequestFilter(httprequest,roles);
    }
    
    @Override
    public ContainerRequestFilter getRequestFilter() {
        return requestFilter;
    }

    @Override
    public ContainerResponseFilter getResponseFilter() {
        // TODO Auto-generated method stub
        return null;
    }
}
