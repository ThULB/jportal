package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

@Path("jquery")
public class JQueryCNDResource {
    @GET
    @Path("ui/css")
    public InputStream getUICSS(){
        return this.getClass().getResourceAsStream("/jquery/ui/css/jquery-ui-1.8.16.custom.css");
    }
    
    @GET
    @Path("ui/images/{filename}")
    public InputStream getUIImages(@PathParam("filename") String filename){
        return this.getClass().getResourceAsStream("/jquery/ui/images/" + filename);
    }
}
