package fsu.jportal.access;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
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

    private Logger LOGGER;

    private static final Pattern TYPE_PATTERN = Pattern.compile("[^_]*_([^_]*)_*");

    private static MCRConfiguration CONFIG = MCRConfiguration.instance();

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
            if (isJpID(id) || permission.equals("read")) {
                return checkPermissionOfType(id, permission);
            } else {
                return (checkPermissionOfTopObject(id, permission)) && (checkPermissionOfType(id, permission));
            }
        }

        private boolean isJpID(String id) {
            String[] jpIdTypes = { "_jpjournal_", "_person_", "_jpinst_", "_derivate_" };
            for (String type : jpIdTypes) {
                if (id.contains(type)) {
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

        public boolean checkPermissionOfType(String id, String permission) {
            String objectType = getObjectType(id);

            MCRAccessInterface aclSystem = config.getAccessInterface();
            if (aclSystem.hasRule("default_" + objectType, permission)) {
                return aclSystem.checkPermission("default_" + objectType, permission);
            }
            return aclSystem.checkPermission("default", permission);
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
                Document objXML = config.getXMLMetadataMgr().retrieveXML(MCRObjectID.getInstance(id));
                final Element journalElem = objXML.getRootElement().getChild("metadata").getChild("hidden_jpjournalsID")
                        .getChild("hidden_jpjournalID");
                String journalID = "";
                if (journalElem != null)
                    journalID = journalElem.getText();
                if (!journalID.equals("")) {
                    allowed = config.getAccessCheckStrategy(AccessStrategyConfig.OBJ_ID_STRATEGY).checkPermission(journalID, permission);
                }
            }
            return allowed;
        }
    }

    public static class SuperUser extends StrategieChain {

        @Override
        protected boolean isReponsibleFor(String id, String permission) {
            return superUser();
        }

        @Override
        protected boolean permissionStrategyFor(String id, String permission) {
            return true;
        }

        private final static boolean superUser() {
            String currentUserID = MCRSessionMgr.getCurrentSession().getUserInformation().getCurrentUserID();
            return currentUserID.equals(MCRConfiguration.instance().getString("MCR.Users.Superuser.UserName"));
        }
    }

    public AccessStrategy() {
        this(new DefaultConfig());
    }

    private void initLogger() {
        this.LOGGER = Logger.getLogger(this.getClass());
    }

    public AccessStrategy(AccessStrategyConfig accessConfig) {
        initLogger();
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
