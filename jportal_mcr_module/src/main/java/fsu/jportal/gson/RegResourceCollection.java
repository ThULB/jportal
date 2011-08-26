package fsu.jportal.gson;

import java.net.URI;
import java.util.Collection;

public class RegResourceCollection {
    private Collection<String> regResources;
    private URI resourceURI;

    public RegResourceCollection(Collection<String> regResources, URI resourceURI) {
        this.setRegResources(regResources);
        this.setResourceURI(resourceURI);
    }

    private void setRegResources(Collection<String> regResources2) {
        this.regResources = regResources2;
    }

    public Collection<String> getRegResources() {
        return regResources;
    }

    private void setResourceURI(URI resourceURI) {
        this.resourceURI = resourceURI;
    }

    public URI getResourceURI() {
        return resourceURI;
    }

}
