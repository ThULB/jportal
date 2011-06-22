package fsu.jportal.access;

import java.util.HashMap;

import org.mycore.access.MCRAccessInterface;
import org.mycore.access.MCRAccessManager;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.access.strategies.MCRObjectIDStrategy;
import org.mycore.access.strategies.MCRObjectTypeStrategy;
import org.mycore.access.strategies.MCRParentRuleStrategy;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.datamodel.common.MCRXMLMetadataManager;

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

    public static class EditorGroupStrategy extends StrategieChain{
        @Override
        protected boolean isReponsibleFor(String id, String permission) {
            return isInEditorGroup();
        }

        @Override
        protected boolean permissionStrategyFor(String id, String permission) {
            if(!(AccessTools.isValidID(id) && isCRUDPerm(permission))){
                return getNextStrategy().checkPermission(id, permission);
            }
            
            return true;
        }
        
        private boolean isCRUDPerm(String permission) {
            return "writedb".equals(permission) || "deletedb".equals(permission);
        }

        private final boolean isInEditorGroup() {
            return MCRSessionMgr.getCurrentSession().getUserInformation().isUserInRole("editorsgroup");
        }
    }
    
    public boolean checkPermission(String id, String permission) {
        StrategieChain strategyChainStart = new SuperUserStrategy();
        StrategieChain strategyChain = strategyChainStart;
        
        strategyChain = strategyChain.setNextStrategy(new EditorGroupStrategy());
        strategyChain = strategyChain.setNextStrategy(new ReadDerivateStrategy(getAccessConfig()));
        strategyChain = strategyChain.setNextStrategy(new IDStrategy(getAccessConfig()));

        return strategyChainStart.checkPermission(id, permission);
    }

    private void setAccessConfig(AccessStrategyConfig accessConfig) {
        this.accessConfig = accessConfig;
    }

    private AccessStrategyConfig getAccessConfig() {
        return accessConfig;
    }
}
