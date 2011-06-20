package fsu.jportal.access;

import java.util.HashMap;

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

    public static abstract class StrategieChain implements MCRAccessCheckStrategy {

        private StrategieChain nextStrategy = null;

        @Override
        public boolean checkPermission(String id, String permission) {
            if(id == null || permission == null || id.equals("") || permission.equals("")){
                return false;
            }
            
            if (isReponsibleFor(id, permission)) {
                return permissionStrategyFor(id, permission);
            } else if (nextStrategy != null) {
                return nextStrategy.checkPermission(id, permission);
            } else {
                return false;
            }
        }

        protected abstract boolean isReponsibleFor(String id, String permission);

        protected abstract boolean permissionStrategyFor(String id, String permission);

        public StrategieChain setNextStrategy(StrategieChain next) {
            this.nextStrategy = next;

            return next;
        }
    }

    public static class ReadDerivateStrategy extends StrategieChain {

        private AccessStrategyConfig config;

        private boolean isValidID = false;

        private boolean readDeriv = false;

        public ReadDerivateStrategy(AccessStrategyConfig config) {
            this.config = config;
        }

        @Override
        protected boolean isReponsibleFor(String id, String permission) {
            readDeriv = permission.equals("read-derivates");
            isValidID = isValidID(id);
            return readDeriv || !isValidID;
        }

        @Override
        protected boolean permissionStrategyFor(String id, String permission) {
            if (config.getAccessInterface().hasRule(id, permission) || (!readDeriv && !isValidID)) {
                return config.getAccessCheckStrategy(AccessStrategyConfig.OBJ_ID_STRATEGY).checkPermission(id, permission);
            } else {
                return true;
            }
        }

        private boolean isValidID(String id) {
            if (contains(id, "_class_")) {
                return false;
            }

            // this is an anti pattern, but I use it any way
            try {
                MCRObjectID.getInstance(id);
                return true;
            } catch (MCRException e) {
                return false;
            }
        }

        private boolean contains(String id, String str) {
            if(id == null){
                return false;
            }
            
            return id.contains(str);
        }

    }

    public static class ValidIDStrategy extends StrategieChain {

        private AccessStrategyConfig config;

        public ValidIDStrategy(AccessStrategyConfig config) {
            this.config = config;
        }

        @Override
        protected boolean isReponsibleFor(String id, String permission) {
            return isValidID(id);
        }

        @Override
        protected boolean permissionStrategyFor(String id, String permission) {
            MCRAccessCheckStrategy objTypeStrategy = config.getAccessCheckStrategy(AccessStrategyConfig.OBJ_TYPE_STRATEGY);
            boolean objTypeStrategyPerm = objTypeStrategy.checkPermission(id, permission);
            
            if(objectWithParents(id) && !"read".equals(permission)){
                return objTypeStrategyPerm && objTypeStrategy.checkPermission(getParentID(id), permission);
            } else {
                return objTypeStrategyPerm;
            }
        }
        
        private boolean objectWithParents(String id){
            for (String idType : new String[] {"_jpvolume_","_jparticle_"}) {
                if (id.contains(idType)) {
                    return true;
                }
            }
            
            return false;
        }

        private boolean isValidID(String id) {
            if (id.contains("_class_")) {
                return false;
            }

            // this is an anti pattern, but I use it any way
            try {
                MCRObjectID.getInstance(id);
                return true;
            } catch (MCRException e) {
                return false;
            }
        }

        private String getParentID(String id) {
            Document objXML = config.getXMLMetadataMgr().retrieveXML(MCRObjectID.getInstance(id));
            try {
                XPath pathToJournalID = XPath.newInstance("/mycoreobject/metadata/hidden_jpjournalsID/hidden_jpjournalID/text()");
                Text idTextNode = (Text) pathToJournalID.selectSingleNode(objXML);
                
                if(idTextNode == null){
                    return "";
                }
                
                return idTextNode.getText();
            } catch (JDOMException e) {
                e.printStackTrace();
            }
            
            return "";
        }
    }

    public static class SuperUser extends StrategieChain {

        @Override
        protected boolean isReponsibleFor(String id, String permission) {
            return isSuperUser();
        }

        @Override
        protected boolean permissionStrategyFor(String id, String permission) {
            return true;
        }

        private final static boolean isSuperUser() {
            String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getCurrentUserID();
            return currentUserID.equals(MCRConfiguration.instance().getString("MCR.Users.Superuser.UserName"));
        }
    }

    public AccessStrategy() {
        this(new DefaultConfig());
    }

    public AccessStrategy(AccessStrategyConfig accessConfig) {
        setAccessConfig(accessConfig);
    }

    public boolean checkPermission(String id, String permission) {
        SuperUser superUserStr = new SuperUser();
        StrategieChain chain = superUserStr.setNextStrategy(new ReadDerivateStrategy(getAccessConfig()));
        chain = chain.setNextStrategy(new ValidIDStrategy(getAccessConfig()));

        return superUserStr.checkPermission(id, permission);
    }

    private void setAccessConfig(AccessStrategyConfig accessConfig) {
        this.accessConfig = accessConfig;
    }

    private AccessStrategyConfig getAccessConfig() {
        return accessConfig;
    }
}
