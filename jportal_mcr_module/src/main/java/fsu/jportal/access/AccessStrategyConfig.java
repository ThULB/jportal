package fsu.jportal.access;

import org.mycore.access.MCRAccessInterface;
import org.mycore.access.strategies.MCRAccessCheckStrategy;
import org.mycore.datamodel.common.MCRXMLMetadataManager;

public interface AccessStrategyConfig{
    static final String PARENT_STRATEGY = "parentStrategy";
    static final String OBJ_TYPE_STRATEGY = "objTypeStrategy";
    static final String OBJ_ID_STRATEGY = "objIDStrategy";
    
    public MCRAccessInterface getAccessInterface();
    public MCRXMLMetadataManager getXMLMetadataMgr();
    public MCRAccessCheckStrategy getAccessCheckStrategy(String strategyName);
}