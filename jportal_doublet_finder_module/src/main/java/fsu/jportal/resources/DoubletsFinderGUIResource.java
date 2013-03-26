package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("doublets/gui")
public class DoubletsFinderGUIResource {
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return getClass().getResourceAsStream("/jportal_doublet_finder_module/gui/" + filename);
    }
}
