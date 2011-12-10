package fsu.jportal.resources.filter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;


public class MyCoReSecurityFilterFactory implements ResourceFilterFactory {
    Logger logger;

    @Context
    HttpServletRequest httpRequest;

    public interface AccesManagerConnector {
        boolean checkPermission(String resourceName, String resourceOperation, MCRSession session);
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    public @interface MCRDBAccess{}

    public MyCoReSecurityFilterFactory() {
        logger = Logger.getLogger(this.getClass());
    }
    
    class MCRDBTransactionFilter implements ResourceFilter, ContainerRequestFilter, ContainerResponseFilter {

        @Override
        public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
            MCRSessionMgr.getCurrentSession().commitTransaction();
            return response;
        }

        @Override
        public ContainerRequest filter(ContainerRequest request) {
            MCRSessionMgr.getCurrentSession().beginTransaction();
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
        
    }

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        List<ResourceFilter> filters = new ArrayList<ResourceFilter>();
        filters.add(new MCRSessionHookFilter(httpRequest));
        MCRDBAccess dbAccessMethod = am.getAnnotation(MCRDBAccess.class);
        MCRDBAccess dbAccessClass = am.getResource().getAnnotation(MCRDBAccess.class);
        if(dbAccessMethod != null || dbAccessClass != null){
            filters.add(new MCRDBTransactionFilter());
        }
        
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            filters.add(new MCRCheckAccessFilter(this, am));
        }
        return filters;
    }
}
