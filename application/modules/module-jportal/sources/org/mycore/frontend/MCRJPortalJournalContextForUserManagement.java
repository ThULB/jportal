package org.mycore.frontend;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.datamodel.metadata.MCRObjectID;
import org.mycore.user2.MCRGroup;
import org.mycore.user2.MCRUserMgr;

public class MCRJPortalJournalContextForUserManagement {

    private String shortCut;

    private String journalID;

    private String userListTOC[];

    private String userListArt[];

    private String userListTOCArt[];

    private static final MCRConfiguration PROPS = MCRConfiguration.instance();

    private static Logger LOGGER = Logger.getLogger(MCRJPortalJournalContextForUserManagement.class);;

    public MCRJPortalJournalContextForUserManagement(String journalID, String shortCut) {
        this.journalID = journalID;
        this.shortCut = shortCut;
    }

    public void setup() {
        createGroups();
        assignUsers();
        setupACLS();
    }

    private void setupACLS() {
        // build rule as XML
        Element rule = new Element("condition");
        rule.addContent(new Element("boolean").setAttribute("operator", "or"));
        Element cond = new Element("condition");
        cond.setAttribute("field", "group");
        cond.setAttribute("operator", "=");
        cond.setAttribute("value", getVolGRID());
        Element cond2 = new Element("condition");
        cond2.setAttribute("field", "group");
        cond2.setAttribute("operator", "=");
        cond2.setAttribute("value", getArtGRID());
        rule.getChild("boolean").addContent(cond);
        rule.getChild("boolean").addContent(cond2);

        try {
            LOGGER.debug("generated ACL-XML=");
            XMLOutputter xo = new XMLOutputter();
            xo.setFormat(Format.getPrettyFormat());
            xo.output(rule, System.out);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // save
        MCRAccessManager.addRule(new MCRObjectID(this.journalID), "writedb", rule, "Write permission for " + this.journalID);
        MCRAccessManager.addRule(new MCRObjectID(this.journalID), "deletedb", rule, "Delete permission for " + this.journalID);

        LOGGER.info("ACL's assigned for journal=" + this.journalID);
    }

    private void assignUsers() {
        MCRUserMgr uMan = MCRUserMgr.instance();
        // TOC
        if (getUserListTOC() != null && getUserListTOC().length > 0) {
            MCRGroup volGR = uMan.retrieveGroup(getVolGRID());
            MCRGroup volGRGeneral = uMan.retrieveGroup("volumegroup");
            for (int i = 0; i < getUserListTOC().length; i++) {
                String userID = getUserListTOC()[i].trim();
                if (!userID.equals("")) {
                    volGR.addMemberUserID(userID);
                    uMan.updateGroup(volGR);
                    LOGGER.debug("added user=" + userID + " as member to group=" + getVolGRID());
                    volGRGeneral.addMemberUserID(userID);
                    uMan.updateGroup(volGRGeneral);
                    LOGGER.debug("added user=" + userID + " as member to group=volumegroup");
                }
            }
        }
        // Art
        if (getUserListArt() != null && getUserListArt().length > 0) {
            MCRGroup artGR = uMan.retrieveGroup(getArtGRID());
            MCRGroup artGRGeneral = uMan.retrieveGroup("editorsgroup");
            for (int i = 0; i < getUserListArt().length; i++) {
                String userID = getUserListArt()[i].trim();
                if (!userID.equals("")) {
                    artGR.addMemberUserID(userID);
                    uMan.updateGroup(artGR);
                    LOGGER.debug("added user=" + userID + " as member to group=" + getArtGRID());
                    artGRGeneral.addMemberUserID(userID);
                    uMan.updateGroup(artGRGeneral);
                    LOGGER.debug("added user=" + userID + " as member to group=editorsgroup");
                }
            }
        }
        // ALL
        if (getUserListTOCArt() != null && getUserListTOCArt().length > 0) {
            MCRGroup volGR = uMan.retrieveGroup(getVolGRID());
            MCRGroup artGR = uMan.retrieveGroup(getArtGRID());
            MCRGroup volGRGeneral = uMan.retrieveGroup("volumegroup");
            MCRGroup artGRGeneral = uMan.retrieveGroup("editorsgroup");
            for (int i = 0; i < getUserListTOCArt().length; i++) {
                String userID = getUserListTOCArt()[i].trim();
                if (!userID.equals("")) {
                    volGR.addMemberUserID(userID);
                    uMan.updateGroup(volGR);
                    LOGGER.debug("added user=" + userID + " as member to group=" + getVolGRID());
                    volGRGeneral.addMemberUserID(userID);
                    uMan.updateGroup(volGRGeneral);
                    LOGGER.debug("added user=" + userID + " as member to group=volumegroup");
                    artGR.addMemberUserID(userID);
                    uMan.updateGroup(artGR);
                    LOGGER.debug("added user=" + userID + " as member to group=" + getArtGRID());
                    artGRGeneral.addMemberUserID(userID);
                    uMan.updateGroup(artGRGeneral);
                    LOGGER.debug("added user=" + userID + " as member to group=editorsgroup");
                }
            }
        }
        LOGGER.info("All users assigned to groups");
    }

    private final void createGroups() {
        String superUserID = PROPS.getString("MCR.Users.Superuser.UserName", "root");
        final String volGRID = getVolGRID();
        final String volGRDescr = "Group to edit JPVolumes for " + this.shortCut;
        final String artGRID = getArtGRID();
        final String artGRDescr = "Group to edit JPArticles, JPInsts and Person for " + this.shortCut;
        MCRUserMgr uMan = MCRUserMgr.instance();

        // add volume group
        MCRGroup volumeGroup = new MCRGroup();
        volumeGroup.setID(volGRID);
        volumeGroup.setDescription(volGRDescr);
        volumeGroup.addAdminUserID(superUserID);
        uMan.createGroup(volumeGroup);
        LOGGER.debug("Group=" + volGRID + " (" + volGRDescr + ") created");

        // add article group
        MCRGroup articleGroup = new MCRGroup();
        articleGroup.setID(artGRID);
        articleGroup.setDescription(artGRDescr);
        articleGroup.addAdminUserID(superUserID);
        uMan.createGroup(articleGroup);
        LOGGER.debug("Group=" + artGRID + " (" + artGRDescr + ") created");

        LOGGER.info("Groups (" + volGRID + ", " + artGRID + ") created");
    }

    /**
     * @return
     */
    private String getArtGRID() {
        final String artGRID = "artGR_" + this.shortCut;
        return artGRID;
    }

    /**
     * @return
     */
    private String getVolGRID() {
        final String volGRID = "volGR_" + this.shortCut;
        return volGRID;
    }

    private String[] getUserListTOC() {
        return userListTOC;
    }

    public void setUserListTOC(String[] userListTOC) {
        this.userListTOC = userListTOC;
    }

    private String[] getUserListArt() {
        return userListArt;
    }

    public void setUserListArt(String[] userListArt) {
        this.userListArt = userListArt;
    }

    private String[] getUserListTOCArt() {
        return userListTOCArt;
    }

    public void setUserListTOCArt(String[] userListTOCArt) {
        this.userListTOCArt = userListTOCArt;
    }

}
