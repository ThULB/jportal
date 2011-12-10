package fsu.jportal.access;

abstract class AbstractStrategyStep implements StrategyStep{
    private AccessStrategyConfig accessStrategyConfig;
    private StrategyStep alternativeStep;

    public AbstractStrategyStep(AccessStrategyConfig accessStrategyConfig) {
        this.accessStrategyConfig = accessStrategyConfig;
    }
    
    protected AccessStrategyConfig getAccessStrategyConfig(){
        return this.accessStrategyConfig;
    }

    @Override
    public void addAlternative(StrategyStep step) {
        this.alternativeStep = step;
    }
    
    protected StrategyStep getAlternative(){
        return this.alternativeStep;
    }
}