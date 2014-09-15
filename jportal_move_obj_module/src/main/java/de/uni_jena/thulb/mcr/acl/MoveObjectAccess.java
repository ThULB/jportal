package de.uni_jena.thulb.mcr.acl;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

import com.sun.jersey.spi.container.ContainerRequest;

public class MoveObjectAccess implements MCRResourceAccessChecker {
    
    @Override
    public boolean isPermitted(ContainerRequest request) {
        return MCRAccessManager.getAccessImpl().checkPermission("move-objects");
    }
}
