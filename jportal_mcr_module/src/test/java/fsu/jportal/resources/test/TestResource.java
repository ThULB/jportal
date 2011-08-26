package fsu.jportal.resources.test;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("auth")
public class TestResource {
    @GET
    @RolesAllowed("")
    public String get(){
        return "Hello World!";
    }
    
    @GET
    @RolesAllowed("")
    @Path("logout/{id}")
    public String logout(@PathParam("id") String id){
        return "GoodBye " + id + "!";
    }
    
    
}
