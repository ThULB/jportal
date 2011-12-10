package fsu.jportal.access;

public interface StrategyStep {
    public boolean checkPermission(String id, String permission);
    public void addAlternative(StrategyStep step);
}
