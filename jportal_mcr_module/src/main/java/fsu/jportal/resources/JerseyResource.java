package fsu.jportal.resources;

import java.net.URI;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public abstract class JerseyResource {
    @Context
    UriInfo uriInfo;
    
    protected URI getBaseURI(){
        return uriInfo.getBaseUri();
    }
}
