package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("IPRule/gui")
public class IPRuleGUIResource {
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return getClass().getResourceAsStream("/jportal_acl_ip_editor_module/gui/" + filename);
    }
}
