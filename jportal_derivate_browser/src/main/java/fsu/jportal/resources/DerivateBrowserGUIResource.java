package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("derivatebrowser/gui")
public class DerivateBrowserGUIResource {

    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename) {
        return getClass().getResourceAsStream("/META-INF/resources/modules/derivate-browser/gui/" + filename);
    }

}
