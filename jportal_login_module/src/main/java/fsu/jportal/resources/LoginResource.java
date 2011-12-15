package fsu.jportal.resources;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.mycore.common.MCRSession;
import org.mycore.common.MCRSessionMgr;
import org.mycore.user.MCRUserMgr;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fsu.jportal.resources.filter.MyCoReSecurityFilterFactory.MCRDBAccess;

@Path("/login")
public class LoginResource extends JerseyResource{
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
    
    @POST
    @MCRDBAccess
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(String json){
        Logger logger = Logger.getLogger(LoginResource.class);
        Gson gson = new GsonBuilder().create();
        LoginData loginData = gson.fromJson(json, LoginData.class);
        Response response = null;
        
        if(loginData.getUserID() != null){
            if (!MCRUserMgr.instance().existUser(loginData.getUserID())) {
                response = Response.status(Status.CONFLICT).build();
            } else if (MCRUserMgr.instance().login(loginData.getUserID(), loginData.getPassword())) {
                response = Response.ok().build();
            }
        }
        return response;
    }
}
