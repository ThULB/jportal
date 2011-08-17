package fsu.jportal.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceSercurityConf {
    private Map<String, List<String>> resourceRegister;
    
    private ResourceSercurityConf() {
        setResourceRegister(new HashMap<String, List<String>>());
    }
    
    private static ResourceSercurityConf instance;
    
    public static ResourceSercurityConf instance(){
        if(instance == null){
            instance = new ResourceSercurityConf();
        }
        
        return instance;
    }
    
    public void registerResource(String resourceClass, String methodPath){
        List<String> methodPaths = getResourceRegister().get(resourceClass);
        if(methodPaths == null){
            methodPaths = new ArrayList<String>();
            getResourceRegister().put(resourceClass, methodPaths);
        }
        
        methodPaths.add(methodPath);
    }

    private void setResourceRegister(Map<String, List<String>> resourceRegister) {
        this.resourceRegister = resourceRegister;
    }

    public Map<String, List<String>> getResourceRegister() {
        return resourceRegister;
    }
}
