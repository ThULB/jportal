package fsu.jportal.access;

import java.util.HashMap;

import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.access.strategies.MCRObjectIDStrategy;
import org.mycore.access.strategies.MCRObjectTypeStrategy;
import org.mycore.access.strategies.MCRParentRuleStrategy;
import org.mycore.common.MCRConfiguration;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;

public class AccessStrategy implements MCRAccessCheckStrategy {
    private StrategyStep strategyChainStart;
    
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
        initStrategyChain();
    }

    protected void initStrategyChain() {
        AccessStrategyConfig accessStrategyConfig = getAccessConfig();
        
        StrategyStep objIdCheck = new ObjIdCheck(accessStrategyConfig);
        StrategyStep crudCheck = new CRUDCheck(accessStrategyConfig);
        StrategyStep parentCheck = new ParentCheck(accessStrategyConfig);
        StrategyStep defaultCheck = new DefaultCheck(accessStrategyConfig);

        parentCheck.setAlternative(objIdCheck);
        defaultCheck.setAlternative(parentCheck);
        crudCheck.setAlternative(defaultCheck);
        objIdCheck.setAlternative(crudCheck);
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

    private boolean isSuperUser() {
        String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getUserID();
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
