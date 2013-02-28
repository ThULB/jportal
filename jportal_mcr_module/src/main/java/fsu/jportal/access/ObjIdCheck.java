package fsu.jportal.access;

class ObjIdCheck extends AbstractStrategyStep {

    public ObjIdCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        if (getAccessStrategyConfig().getAccessInterface().hasRule(id, permission)) {
            return getAccessStrategyConfig().getAccessInterface().checkPermission(id, permission);
        }
        return getAlternative() != null ? getAlternative().checkPermission(id, permission) : false;
    }

}