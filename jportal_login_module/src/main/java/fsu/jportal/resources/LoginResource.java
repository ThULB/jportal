package fsu.jportal.resources;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.mycore.frontend.jersey.resources.MCRJerseyResource;
import org.mycore.user2.MCRUserManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@Path("login")
public class LoginResource extends MCRJerseyResource {

    private class LoginData{
        private String userID;
        private String password;
        
        public String getUserID() {
            return userID;
        }
        
        public String getPassword() {
            return password;
        }
    }

    @GET
    @Path("{filename:.*}")
    public InputStream getResources(@PathParam("filename") String filename){
        return this.getClass().getResourceAsStream("/fsu/jportal/login_module/" + filename);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String json){
        Gson gson = new GsonBuilder().create();
        LoginData loginData = gson.fromJson(json, LoginData.class);
        Response response = null;
        
        if(loginData.getUserID() != null){
            if (!MCRUserManager.exists(loginData.getUserID())) {
                response = Response.status(Status.CONFLICT).build();
            } else if(MCRUserManager.login(loginData.getUserID(), loginData.getPassword()) != null){
                response = Response.ok().build();
            }
        }
        return response;
    }

}
