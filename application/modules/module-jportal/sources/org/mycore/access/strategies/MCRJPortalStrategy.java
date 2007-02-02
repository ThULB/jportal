package org.mycore.access.strategies;

public class MCRJPortalStrategy implements MCRAccessCheckStrategy {

	private final static MCRAccessCheckStrategy TYPE_STRATEGY = new MCRObjectTypeStrategy();

	private final static MCRAccessCheckStrategy DEFAULT_STRATEGY = new MCRParentRuleStrategy();

	public boolean checkPermission(String id, String permission) {
		if (id.contains("_person_") || id.contains("_jpinst_")) {
			return TYPE_STRATEGY.checkPermission(id, permission);
		}
		return DEFAULT_STRATEGY.checkPermission(id, permission);
	}

}
