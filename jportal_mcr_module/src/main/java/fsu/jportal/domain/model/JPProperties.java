package fsu.jportal.domain.model;

import java.util.HashMap;

public class JPProperties {
    private HashMap<String, String> properties;

    public JPProperties() {
        this.properties = new HashMap<>();
    }

    public HashMap<String, String> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, String> properties) {
        this.properties = properties;
    }

    public void setProperty(String propName, String propVal) {
        getProperties().put(propName, propVal);
    }
}
