package fsu.jportal.access;


public class ReadDerivateStrategy extends StrategieChain {

    private AccessStrategyConfig config;

    private boolean isValidID = false;

    private boolean readDeriv = false;

    private boolean poolPrivilege;

    public ReadDerivateStrategy(AccessStrategyConfig config) {
        this.config = config;
    }

    @Override
    protected boolean isReponsibleFor(String id, String permission) {
        readDeriv = permission.equals("read-derivates");
        poolPrivilege = "POOLPRIVILEGE".equals(id);
        isValidID = AccessTools.isValidID(id);
        return poolPrivilege || readDeriv || !isValidID;
    }

    @Override
    protected boolean permissionStrategyFor(String id, String permission) {
        if (config.getAccessInterface().hasRule(id, permission) || (!readDeriv && !isValidID) || poolPrivilege) {
            return config.getAccessCheckStrategy(AccessStrategyConfig.OBJ_ID_STRATEGY).checkPermission(id, permission);
        } else {
            return true;
        }
    }
}