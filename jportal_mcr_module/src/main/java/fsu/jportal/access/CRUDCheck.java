package fsu.jportal.access;

public class CRUDCheck extends AbstractStrategyStep {

    public CRUDCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        if (isCRUD_Operation(permission)) {
            String crudid = "POOLPRIVILEGE";
            if (getAccessStrategyConfig().getAccessInterface().hasRule(crudid, permission)) {
                return getAccessStrategyConfig().getAccessInterface().checkPermission(crudid, permission);
            }
        }
        return getAlternative() != null ? getAlternative().checkPermission(id, permission) : false;
    }

    private boolean isCRUD_Operation(String permission) {
        return permission.startsWith("create-") || permission.startsWith("read-") || permission.startsWith("update-")
                || permission.startsWith("delete-");
    }

}
