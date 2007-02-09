package org.mycore.access.strategies;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessManager;

public class MCRJPortalStrategy implements MCRAccessCheckStrategy {
	
	private static final Logger LOGGER = Logger.getLogger(MCRParentRuleStrategy.class);
	private static final Pattern TYPE_PATTERN = Pattern.compile("[^_]*_([^_]*)_*");	
	
	private final static MCRAccessCheckStrategy ANCASTOR_STRATEGY = new MCRParentRuleStrategy();

	public boolean checkPermission(String id, String permission) {
		if ((id.contains("_jpjournal_") || (id.contains("_person_")) || id.contains("_jpinst_"))) {
			//LOGGER.debug("#####################################################################################################");
			//LOGGER.debug("journal oder person oder institution mit permission="+permission+" zu überprüfen: ergebnis="+checkPermissionOfType(id, permission)+"...");
			//LOGGER.debug("#####################################################################################################");
			return checkPermissionOfType(id, permission);
		}
		if ((ANCASTOR_STRATEGY.checkPermission(id, permission)) && (checkPermissionOfType(id, permission))) {
			//LOGGER.debug("#####################################################################################################");			
			//LOGGER.debug("NICHT person oder institution mit permission="+permission+" zu überprüfen: ergebnis=TypeCheck("+
			//checkPermissionOfType(id, permission)+") & AncastorCheck("+ANCASTOR_STRATEGY.checkPermission(id, permission)+")...");
			//LOGGER.debug("#####################################################################################################");			
			return true;
		}
		return false;
	}
	
    public boolean checkPermissionOfType(String id, String permission) {
        String objectType = getObjectType(id);

        if (MCRAccessManager.getAccessImpl().hasRule("default_" + objectType, permission)) {
            LOGGER.debug("using access rule defined for object type.");
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
}