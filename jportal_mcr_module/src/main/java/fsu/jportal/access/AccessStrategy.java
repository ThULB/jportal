package fsu.jportal.access;

import java.util.HashMap;

import org.apache.log4j.Logger;
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
    private class InvalidIDException extends Exception {

    }
    
    private AccessStrategyConfig accessConfig;

    private static Logger LOGGER = Logger.getLogger(AccessStrategy.class);

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
        MCRAccessInterface accessInterface = getAccessConfig().getAccessInterface();

        if (id == null || "".equals(id) || permission == null || "".equals(permission)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        return checkPermForObj(id, permission, getAccessConfig());
    }
    
    protected boolean checkPermForObj(String id, String permission, AccessStrategyConfig accessStrategyConfig) {
        StrategyStep objIdCheck = new ObjIdCheck(accessStrategyConfig);
        StrategyStep crudCheck = new CRUDCheck(accessStrategyConfig);
        StrategyStep parentCheck = new ParentCheck(accessStrategyConfig);
        StrategyStep defaultCheck = new DefaultCheck(accessStrategyConfig);
        
        parentCheck.addAlternative(defaultCheck);
        crudCheck.addAlternative(parentCheck);
        objIdCheck.addAlternative(crudCheck);
        
        return objIdCheck.checkPermission(id, permission);
    }

    protected MCRObjectID getObjId(String id) throws InvalidIDException {
        try {
            MCRObjectID objID = MCRObjectID.getInstance(id);
            return objID;
        } catch (MCRException e) {
            throw new InvalidIDException();
        }
    }

    private boolean isCRUD_Operation(String permission) {
        return permission.startsWith("create_") || permission.startsWith("read_") || permission.startsWith("update_")
                || permission.startsWith("delete_");
    }

    private MCRObjectID getParentID(MCRObjectID objID) {
        //LOGGER.info("getParentId: " + objID + " exists " + getAccessConfig().getXMLMetadataMgr().exists(objID));
        if (!getAccessConfig().getXMLMetadataMgr().exists(objID)) {
            return null;
        }

        Document objXML = getAccessConfig().getXMLMetadataMgr().retrieveXML(objID);
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
                return MCRObjectID.getInstance(((Attribute) idTextNode).getValue());
            }
        } catch (JDOMException e) {
            e.printStackTrace();
        }

        return null;
    }

    private boolean isSuperUser() {
        String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getCurrentUserID();
        return currentUserID.equals(MCRConfiguration.instance().getString("MCR.Users.Superuser.UserName"));
    }

    private void setAccessConfig(AccessStrategyConfig accessConfig) {
        this.accessConfig = accessConfig;
    }

    private AccessStrategyConfig getAccessConfig() {
        return accessConfig;
    }
}
