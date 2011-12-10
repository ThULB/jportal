package fsu.jportal.access;


public class DefaultCheck extends AbstractStrategyStep{

    public DefaultCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        if (getAccessStrategyConfig().getAccessInterface().hasRule("default", permission)) {
            return getAccessStrategyConfig().getAccessInterface().checkPermission("default", permission);
        }

        return false;
    }

}
