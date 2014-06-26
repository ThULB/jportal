package fsu.jportal.resources;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

public class ResourceAccess implements MCRResourceAccessChecker{

    private static final String RESOURCE_EDIT = "resourceEdit";

    @Override
    public boolean isPermitted(ContainerRequest request) {
        String method = request.getMethod();
        String path = request.getPath();
        
        if("GET".equals(method) && !MCRAccessManager.hasRule(path, RESOURCE_EDIT)){
            return true;
        }
        
        return MCRAccessManager.checkPermission(path, RESOURCE_EDIT);
    }

}
