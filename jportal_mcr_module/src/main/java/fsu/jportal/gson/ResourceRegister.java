package fsu.jportal.gson;

import java.util.List;
import java.util.Map;

public class ResourceRegister {
    private Map<String, List<String>> map;

    public ResourceRegister(Map<String, List<String>> map) {
        setMap(map);
    }

    private void setMap(Map<String, List<String>> map) {
        this.map = map;
    }

    public Map<String, List<String>> getMap() {
        return map;
    }
    
}
