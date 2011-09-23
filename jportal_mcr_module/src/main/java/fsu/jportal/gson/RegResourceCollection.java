package fsu.jportal.gson;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class RegResourceCollection {
    private Map<String, List<String>> regResources;
    private URI resourceURI;

    public RegResourceCollection(Map<String, List<String>> resourceRegister, URI absolutePath) {
        this.regResources = resourceRegister;
        setResourceURI(absolutePath);
    }

    public Map<String, List<String>> getRegResources() {
        return regResources;
    }

    private void setResourceURI(URI resourceURI) {
        this.resourceURI = resourceURI;
    }

    public URI getResourceURI() {
        return resourceURI;
    }

}
