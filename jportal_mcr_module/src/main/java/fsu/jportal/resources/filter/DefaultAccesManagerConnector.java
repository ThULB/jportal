package fsu.jportal.resources.filter;

import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRSession;

import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory.AccesManagerConnector;

public class DefaultAccesManagerConnector implements AccesManagerConnector {
    @Override
    public boolean checkPermission(String resourceName, String resourceOperation, MCRSession session) {
        boolean hasPermission = MCRAccessManager.checkPermission(resourceName, resourceOperation);
        return hasPermission;
    }
}