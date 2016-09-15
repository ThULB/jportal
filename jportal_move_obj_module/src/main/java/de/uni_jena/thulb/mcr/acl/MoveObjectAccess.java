package de.uni_jena.thulb.mcr.acl;

import javax.ws.rs.container.ContainerRequestContext;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;

public class MoveObjectAccess implements MCRResourceAccessChecker {
    
    @Override
    public boolean isPermitted(ContainerRequestContext request) {
        return MCRAccessManager.getAccessImpl().checkPermission("move-objects");
    }
}
