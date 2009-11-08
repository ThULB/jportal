package org.mycore.access.strategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLTableManager;
import org.mycore.datamodel.metadata.MCRObject;
import org.mycore.datamodel.metadata.MCRObjectID;

public class MCRJPortalStrategy implements MCRAccessCheckStrategy {

    private static final Logger LOGGER = Logger.getLogger(MCRParentRuleStrategy.class);

    private static final Pattern TYPE_PATTERN = Pattern.compile("[^_]*_([^_]*)_*");

    private final static MCRObjectIDStrategy ID_STRATEGY = new MCRObjectIDStrategy();

    private static MCRConfiguration CONFIG = MCRConfiguration.instance();

    public boolean checkPermission(String id, String permission) {
        if (superUser())
            return true;
        else if (permission.equals("read-derivates")) {
            if (MCRAccessManager.getAccessImpl().hasRule(id, permission)) {
                return ID_STRATEGY.checkPermission(id, permission);
            } else {
                return true;
            }
        }

        try {
            if(MCRObject.existInDatastore(id)) {
                if (id.contains("_jpjournal_") || id.contains("_person_") || id.contains("_jpinst_") || id.contains("_derivate_") || permission.equals("read")) {
                    return checkPermissionOfType(id, permission);
                } else if ((checkPermissionOfTopObject(id, permission)) && (checkPermissionOfType(id, permission))) {
                    return true;
                }
            };
        } catch(MCRException e) {
            // do not throw an exception here. maybe the id is not a valid mcr id.
            // then the default strategy at this points is to return always false.
        }
        return false;
    }

    public boolean checkPermissionOfType(String id, String permission) {
        String objectType = getObjectType(id);

        if (MCRAccessManager.getAccessImpl().hasRule("default_" + objectType, permission)) {
            LOGGER.debug("using access rule defined for object type " + objectType);
            return MCRAccessManager.getAccessImpl().checkPermission("default_" + objectType, permission);
        }
        LOGGER.debug("using system default access rule.");
        return MCRAccessManager.getAccessImpl().checkPermission("default", permission);
    }

    private static String getObjectType(String id) {
        Matcher m = TYPE_PATTERN.matcher(id);
        if (m.find() && (m.groupCount() == 1)) {
            return m.group(1);
        }
        return "";
    }

    public boolean checkPermissionOfTopObject(String id, String permission) {
        boolean allowed = false;
        if (id != null && permission != null && !id.equals("") && !permission.equals("")) {
            Document objXML = MCRXMLTableManager.instance().readDocument(new MCRObjectID(id));
            final Element journalElem = objXML.getRootElement().getChild("metadata").getChild("hidden_jpjournalsID").getChild("hidden_jpjournalID");
            String journalID = "";
            if (journalElem != null)
                journalID = journalElem.getText();
            if (!journalID.equals("")) {
                LOGGER.debug("Using journal access rule defined for: " + journalID);
                allowed = ID_STRATEGY.checkPermission(journalID, permission);
            } else
                LOGGER.debug("No journal access rule found for: " + journalID);
        }
        return allowed;
    }

    private final static boolean superUser() {
        return MCRSessionMgr.getCurrentSession().getCurrentUserID().equals(CONFIG.getString("MCR.Users.Superuser.UserName"));
    }
}
