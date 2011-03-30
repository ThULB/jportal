package org.mycore.access.strategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.mycore.access.MCRAccessManager;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
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
        } else if (isValidID(id)) {
            if (isJpID(id) || permission.equals("read")) {
                return checkPermissionOfType(id, permission);
            } else if ((checkPermissionOfTopObject(id, permission)) && (checkPermissionOfType(id, permission))) {
                return true;
            }
        } else {
            return MCRAccessManager.getAccessImpl().checkPermission(id, permission);
        }

        return false;
    }

    private boolean isJpID(String id) {
        String[] jpIdTypes = {"_jpjournal_", "_person_", "_jpinst_", "_derivate_"};
        for (String type : jpIdTypes) {
            if(id.contains(type)){
                return true;
            }
        }
        
        return false;
    }

    private boolean isValidID(String id) {
        if(id.contains("_class_")){
            return false;
        }
        
        if (id == null) {
            return false;
        }

        String mcr_id = id.trim();

        int MAX_LENGTH = 64;
        
        if (mcr_id.length() > MAX_LENGTH  || mcr_id.length() == 0) {
            return false;
        }

        String[] idParts = MCRObjectID.getIDParts(mcr_id);

        if (idParts.length != 3) {
            return false;
        }

        String mcr_project_id = idParts[0].intern();

        String mcr_type_id = idParts[1].toLowerCase().intern();

        if (!CONFIG.getBoolean("MCR.Metadata.Type." + mcr_type_id, false)) {
            return false;
        }

        int mcr_number = -1;

        try {
            mcr_number = Integer.parseInt(idParts[2]);
        } catch (NumberFormatException e) {
            return false;
        }

        if (mcr_number < 0) {
            return false;
        }

        return true;
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
            Document objXML = MCRXMLMetadataManager.instance().retrieveXML(MCRObjectID.getInstance(id));
            final Element journalElem = objXML.getRootElement().getChild("metadata").getChild("hidden_jpjournalsID").getChild(
                    "hidden_jpjournalID");
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
        String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getCurrentUserID();
        return currentUserID.equals(CONFIG.getString("MCR.Users.Superuser.UserName"));
    }
}
