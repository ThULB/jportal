package fsu.jportal.access.test;

import org.mycore.common.MCRUserInformation;

public class FakeUserInfo implements MCRUserInformation {

    private String userID;
    private String userRole;
    private String userAttributes;

    public FakeUserInfo(String userID, String userRole, String userAttributes) {
        this.userID = userID;
        this.userRole = userRole;
        this.userAttributes = userAttributes;
    }
    
    @Override
    public String getCurrentUserID() {
        return userID;
    }

    @Override
    public boolean isUserInRole(String role) {
        return userRole.equals(role);
    }

    @Override
    public String getUserAttribute(String attribute) {
        return userAttributes;
    }

}
