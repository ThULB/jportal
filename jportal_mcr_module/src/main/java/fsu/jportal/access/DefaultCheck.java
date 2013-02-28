package fsu.jportal.access;

import org.mycore.access.MCRAccessInterface;
import org.mycore.common.MCRException;
import org.mycore.datamodel.metadata.MCRObjectID;

public class DefaultCheck extends AbstractStrategyStep{

    public DefaultCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        MCRAccessInterface accessInterface = getAccessStrategyConfig().getAccessInterface();
        String typeFromID = getTypeFromID(id);
        if (typeFromID != null && accessInterface.hasRule("default_" + typeFromID, permission)) {
            return accessInterface.checkPermission("default_" + typeFromID, permission);
        }
        
        if (accessInterface.hasRule("default", permission)) {
            return accessInterface.checkPermission("default", permission);
        }
        return getAlternative() != null ? getAlternative().checkPermission(id, permission) : false;
    }

    protected String getTypeFromID(String id) {
        try {
            MCRObjectID objID = MCRObjectID.getInstance(id);
            return objID.getTypeId();
        } catch (MCRException e) {
            return null;
        }
    }

}
