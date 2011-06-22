package fsu.jportal.access;

import org.mycore.access.strategies.MCRAccessCheckStrategy;

public abstract class StrategieChain implements MCRAccessCheckStrategy {

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

    public StrategieChain getNextStrategy() {
        return nextStrategy;
    }
}