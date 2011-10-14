package fsu.jportal.resources.filter;

import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.frontend.servlets.MCRServlet;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.PathValue;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import fsu.jportal.config.ResourceSercurityConf;

public class MyCoReSecurityFilterFactory implements ResourceFilterFactory {
    private Logger logger;
    @Context
    HttpServletRequest httpRequest;
    
    public interface AccesManagerConnector {
        boolean checkPermission(String resourceName, String resourceOperation, MCRSession session);
    }
    
    public MyCoReSecurityFilterFactory() {
        logger = Logger.getLogger(this.getClass());
    }

    public static class DefaultAccesManagerConnector implements AccesManagerConnector {
        @Override
        public boolean checkPermission(String resourceName, String resourceOperation, MCRSession session) {
            session.beginTransaction();
            boolean hasPermission = MCRAccessManager.checkPermission(resourceName, resourceOperation);
            session.commitTransaction();
            return hasPermission;
        }
    }
    
    private class Filter implements ResourceFilter, ContainerRequestFilter,ContainerResponseFilter {

        private String resourceName;
        private String resourceOperation;

        public Filter(AbstractMethod am) {
            this.resourceName = am.getResource().getResourceClass().getName();
            this.resourceOperation = getPath(am) + "_" + getHttpMethod(am);
            ResourceSercurityConf.instance().registerResource(resourceName, resourceOperation);
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            MCRSession session = MCRServlet.getSession(httpRequest);
            MCRSessionMgr.setCurrentSession(session);
            
            boolean hasPermission = getAccessManagerConnector().checkPermission(resourceName, resourceOperation, session);
            
            logger.debug("current user ID: " + session.getUserInformation().getCurrentUserID());
            logger.debug("resource name: " + resourceName);
            logger.debug("resource operation: " + resourceOperation);
            logger.debug("has permission: " + hasPermission);
            
            if (!hasPermission) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
            return request;
        }

        @Override
        public ContainerRequestFilter getRequestFilter() {
            return this;
        }

        @Override
        public ContainerResponseFilter getResponseFilter() {
            return this;
        }

        @Override
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            MCRSessionMgr.releaseCurrentSession();
            return response;
        }

    }
    
    private String getHttpMethod(AbstractMethod am) {
        Class[] httpMethods = { POST.class, GET.class, PUT.class, DELETE.class };
        for (Class httpMethod : httpMethods) {
            if (am.isAnnotationPresent(httpMethod)) {
                return httpMethod.getSimpleName();
            }
        }

        return null;
    }

    private String getPath(AbstractMethod am) {
        PathValue resourcePath = am.getResource().getPath();
        if (resourcePath == null) {
            return null;
        }

        String path = "/" + resourcePath.getValue();
        Path methodPath = am.getAnnotation(Path.class);
        if (methodPath != null) {
            return path + "/" + methodPath.value();
        }

        return path;
    }
    
    private AccesManagerConnector getAccessManagerConnector() {
        MCRConfiguration instance = MCRConfiguration.instance();
        String defaultConnector = DefaultAccesManagerConnector.class.getName();
        return (AccesManagerConnector) instance.getInstanceOf("McrSessionSecurityFilter.MCRAccessManager.Connector", defaultConnector);
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            return Collections.<ResourceFilter> singletonList(new Filter(am));
        }
        return null;
    }
}
