package fsu.jportal.resources;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

@Path("derivatebrowser/gui")
public class DerivateBrowserGUIResource {
    
    @Context
    HttpServletResponse response;

    @GET
    @Path("{filename:.*}")
    public Response getResources(@PathParam("filename") String filename) {
        if (filename.endsWith(".js")) {
            return Response.ok(getClass()
                .getResourceAsStream("/META-INF/resources/modules/derivate-browser/gui/" + filename))
                .header("Content-Type", "application/javascript")
                .build();
        }
        
        if (filename.endsWith(".css")) {
            return Response.ok(getClass()
                .getResourceAsStream("/META-INF/resources/modules/derivate-browser/gui/" + filename))
                .header("Content-Type", "text/css")
                .build();
        }
        return Response.ok(getClass()
            .getResourceAsStream("/META-INF/resources/modules/derivate-browser/gui/" + filename))
            .build();

    }
}
