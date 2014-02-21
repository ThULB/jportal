package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("moveObj/gui")
public class MoveObjGUIResource {
    
    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return getClass().getResourceAsStream("/jportal_move_obj_module/gui/" + filename);
    }
}