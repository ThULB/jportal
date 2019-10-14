package fsu.jportal.domain.model;

import org.mycore.common.MCRSessionMgr;
import org.mycore.common.MCRUserInformation;

public class JPUser {
    private String realname;
    private String group;
    private String email;
    private String userID;

    public JPUser() {
    }

    public JPUser(String userID, String realname, String group, String email) {
        setUserID(userID);
        setRealname(realname);
        setGroup(group);
        setEmail(email);
    }

    public static JPUser info(){
        MCRUserInformation userInformation = MCRSessionMgr.getCurrentSession().getUserInformation();
        String realname = userInformation.getUserAttribute(MCRUserInformation.ATT_REAL_NAME);
        String group = userInformation.getUserAttribute(MCRUserInformation.ATT_PRIMARY_GROUP);
        String email = userInformation.getUserAttribute(MCRUserInformation.ATT_EMAIL);
        String userID = userInformation.getUserID();

        return new JPUser(userID, realname, group, email);
    }

    public static boolean isUserInRole(String role){
        return MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole(role);
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
}
