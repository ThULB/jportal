package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("ACLE/gui")
public class ACLEGUIResource {
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return getClass().getResourceAsStream("/jportal_acl_editor_module/gui/" + filename);
    }
}