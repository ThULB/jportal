package fsu.jportal.access;

import java.util.HashMap;

import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.Text;
import org.jdom.xpath.XPath;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.access.strategies.MCRObjectIDStrategy;
import org.mycore.access.strategies.MCRObjectTypeStrategy;
import org.mycore.access.strategies.MCRParentRuleStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRConstants;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class AccessStrategy implements MCRAccessCheckStrategy {
	private AccessStrategyConfig accessConfig;

	private static class DefaultConfig implements AccessStrategyConfig {
		private HashMap<String, MCRAccessCheckStrategy> strategies;

		public DefaultConfig() {
			strategies = new HashMap<String, MCRAccessCheckStrategy>();
			strategies.put(OBJ_ID_STRATEGY, new MCRObjectIDStrategy());
			strategies.put(OBJ_TYPE_STRATEGY, new MCRObjectTypeStrategy());
			strategies.put(PARENT_STRATEGY, new MCRParentRuleStrategy());
		}

		@Override
		public MCRAccessInterface getAccessInterface() {
			return MCRAccessManager.getAccessImpl();
		}

		@Override
		public MCRAccessCheckStrategy getAccessCheckStrategy(String strategyName) {
			return strategies.get(strategyName);
		}

		@Override
		public MCRXMLMetadataManager getXMLMetadataMgr() {
			return MCRXMLMetadataManager.instance();
		}
	}

	public AccessStrategy() {
		this(new DefaultConfig());
	}

	public AccessStrategy(AccessStrategyConfig accessConfig) {
		setAccessConfig(accessConfig);
	}

	public boolean checkPermission(String id, String permission) {
		MCRAccessInterface accessInterface = getAccessConfig()
				.getAccessInterface();

		if (id == null || "".equals(id) || permission == null
				|| "".equals(permission)) {
			return false;
		}

		if (isSuperUser()) {
			return true;
		}

		if (accessInterface.hasRule(id, permission)) {
			return accessInterface.checkPermission(id, permission);
		}

		if (isCRUD_Operation(permission)) {
			String crudid = "CRUD";
			if (accessInterface.hasRule(crudid, permission)) {
				return accessInterface.checkPermission(crudid, permission);
			}
		}

		try {
            MCRObjectID objID = MCRObjectID.getInstance(id);
            String typeId = objID.getTypeId();

            MCRObjectID parentID = getParentID(objID);
            String permForType = permission + "_" + typeId;

            if (parentID != null) {
            	if (accessInterface.hasRule(parentID.toString(), permForType)) {
            		return accessInterface.checkPermission(parentID.toString(),
            				permForType);
            	}
            	MCRObjectID journalID = getParentID(parentID);
            	if (accessInterface.hasRule(journalID.toString(), permForType)) {
            		return accessInterface.checkPermission(journalID.toString(),
            				permForType);
            	}
            }
            
            if (accessInterface.hasRule("default_" + typeId, permission)) {
            	return accessInterface.checkPermission("default_" + typeId,
            			permission);
            }
        } catch (MCRException e) {
            // Maybe no valid MCRObjectID -> TODO add check method for valid ID into MCRObjectID
        }

		if (accessInterface.hasRule("default", permission)) {
			return accessInterface.checkPermission("default", permission);
		}

		return false;
	}

	private boolean isCRUD_Operation(String permission) {
		return permission.startsWith("create_")
				|| permission.startsWith("read_")
				|| permission.startsWith("update_")
				|| permission.startsWith("delete_");
	}

	private MCRObjectID getParentID(MCRObjectID objID) {
	    if(!getAccessConfig().getXMLMetadataMgr().exists(objID)){
	        return null;
	    }
	    
		Document objXML = getAccessConfig().getXMLMetadataMgr().retrieveXML(
				objID);
		try {
			String path = "/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()";
			if (objID.getTypeId().equals("derivate")) {
				path = "/mycorederivate/derivate/linkmetas/linkmeta/@xlink:href";
			}
			XPath pathToJournalID = XPath.newInstance(path);
			pathToJournalID.addNamespace(MCRConstants.XLINK_NAMESPACE);
			Object idTextNode = pathToJournalID.selectSingleNode(objXML);

			if (idTextNode instanceof Text) {
				return MCRObjectID.getInstance(((Text) idTextNode).getText());
			} else if (idTextNode instanceof Attribute) {
				return MCRObjectID.getInstance(((Attribute) idTextNode)
						.getValue());
			}
		} catch (JDOMException e) {
			e.printStackTrace();
		}

		return null;
	}

	private boolean isSuperUser() {
		String currentUserID = MCRSessionMgr.getCurrentSession()
				.getUserInformation().getCurrentUserID();
		return currentUserID.equals(MCRConfiguration.instance().getString(
				"MCR.Users.Superuser.UserName"));
	}

	private void setAccessConfig(AccessStrategyConfig accessConfig) {
		this.accessConfig = accessConfig;
	}

	private AccessStrategyConfig getAccessConfig() {
		return accessConfig;
	}
}
