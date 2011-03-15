package fsu.jportal.metadata;

import java.util.HashMap;
import java.util.Map;

public abstract class XMLMetaElementEntry{
    private HashMap<String, String> tagValueMap = new HashMap<String, String>();
    
    public abstract String getLang();
    public abstract String getMetaElemName();
    
    public Map<String, String> getTagValueMap(){
        return tagValueMap;
    }
    
    
}