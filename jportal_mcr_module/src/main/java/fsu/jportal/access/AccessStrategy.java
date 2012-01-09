package fsu.jportal.access;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.access.strategies.MCRObjectIDStrategy;
import org.mycore.access.strategies.MCRObjectTypeStrategy;
import org.mycore.access.strategies.MCRParentRuleStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRException;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;
import org.mycore.datamodel.metadata.MCRObjectID;

public class AccessStrategy implements MCRAccessCheckStrategy {
    private StrategyStep strategyChainStart;
    
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
        initStrategyChain();
    }

    protected void initStrategyChain() {
        AccessStrategyConfig accessStrategyConfig = getAccessConfig();
        
        StrategyStep objIdCheck = new ObjIdCheck(accessStrategyConfig);
        StrategyStep crudCheck = new CRUDCheck(accessStrategyConfig);
        StrategyStep parentCheck = new ParentCheck(accessStrategyConfig);
        StrategyStep defaultCheck = new DefaultCheck(accessStrategyConfig);
        
        parentCheck.addAlternative(defaultCheck);
        crudCheck.addAlternative(parentCheck);
        objIdCheck.addAlternative(crudCheck);
        setStrategyChainStart(objIdCheck);
    }
    
    public boolean checkPermission(String id, String permission) {
        if (id == null || "".equals(id) || permission == null || "".equals(permission)) {
            return false;
        }

        if (isSuperUser()) {
            return true;
        }

        return getStrategyChainStart().checkPermission(id, permission);
    }
    
    protected MCRObjectID getObjId(String id) throws InvalidIDException {
        try {
            MCRObjectID objID = MCRObjectID.getInstance(id);
            return objID;
        } catch (MCRException e) {
            throw new InvalidIDException();
        }
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

    public void setStrategyChainStart(StrategyStep strategyChainStart) {
        this.strategyChainStart = strategyChainStart;
    }

    public StrategyStep getStrategyChainStart() {
        return strategyChainStart;
    }
}
