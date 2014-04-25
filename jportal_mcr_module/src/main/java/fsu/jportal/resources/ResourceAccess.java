package fsu.jportal.resources;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

public class ResourceAccess implements MCRResourceAccessChecker{

    @Override
    public boolean isPermitted(ContainerRequest request) {
        String method = request.getMethod();
        
        if("GET".equals(method)){
            return true;
        }
        
        String path = request.getPath();
        return MCRAccessManager.checkPermission(path, "resourceEdit");
    }

}
