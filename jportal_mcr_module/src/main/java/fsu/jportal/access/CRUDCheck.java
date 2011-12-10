package fsu.jportal.access;

public class CRUDCheck extends AbstractStrategyStep {

    public CRUDCheck(AccessStrategyConfig accessStrategyConfig) {
        super(accessStrategyConfig);
    }

    @Override
    public boolean checkPermission(String id, String permission) {
        if (isCRUD_Operation(permission)) {
            String crudid = "CRUD";
            if (getAccessStrategyConfig().getAccessInterface().hasRule(crudid, permission)) {
                return getAccessStrategyConfig().getAccessInterface().checkPermission(crudid, permission);
            }
        }
        
        return getAlternative().checkPermission(id, permission);
    }

    private boolean isCRUD_Operation(String permission) {
        return permission.startsWith("create_") || permission.startsWith("read_") || permission.startsWith("update_")
                || permission.startsWith("delete_");
    }
}
