package fsu.jportal.resources;

import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBException;

import org.mycore.access.MCRAccessManager;
import org.mycore.frontend.jersey.filter.access.MCRResourceAccessChecker;
import org.mycore.frontend.jersey.filter.access.MCRRestrictedAccess;

import com.google.gson.Gson;

import fsu.jportal.backend.conf.ObjConfiguration;
import fsu.jportal.domain.model.JPProperties;
import fsu.jportal.domain.model.JPUser;

@Path("objConf")
public class JPObjectConfigurationResource {
    @GET
    @Path("{id}")
    public Response list(@PathParam("id") String id){
        Gson gson = new Gson();

        List<String> configFiles = ObjConfiguration.of(id)
                .list()
                .map(java.nio.file.Path::getFileName)
                .map(java.nio.file.Path::toString)
                .collect(Collectors.toList());
        return Response.ok(gson.toJson(configFiles)).build();
    }

    @GET
    @Path("{id}/{type}")
    @Produces(MediaType.APPLICATION_JSON)
    public JPProperties properties(@PathParam("id") String id, @PathParam("type") String type){
        return ObjConfiguration.of(id)
                .loadJPProperties(type)
                .orElseGet(JPProperties::new);
    }

    @GET
    @Path("props")
    @Produces(MediaType.APPLICATION_JSON)
    public JPProperties getProps() throws JAXBException {
        JPProperties jpProperties = new JPProperties();
        jpProperties.setProperty("foo", "bar");

        return jpProperties;
    }

    @PUT
    @Path("{id}/{type}")
    @Consumes(MediaType.APPLICATION_JSON)
    @MCRRestrictedAccess(JPObjectConfigurationResourceAccess.class)
    public void addProperties(JPProperties properties, @PathParam("id") String id, @PathParam("type") String type){
        ObjConfiguration.of(id).saveJPProperties(type, properties);
    }

    public static class JPObjectConfigurationResourceAccess implements MCRResourceAccessChecker {
        @Override
        public boolean isPermitted(ContainerRequestContext containerRequestContext) {
            return MCRAccessManager.checkPermission("create-jpjournal");
        }
    }

    @GET
    @Path("userinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public JPUser userInfo(){
        return JPUser.info();
    }

    @GET
    @Path("userinfo/{role}")
    public Boolean isUserInRole(@PathParam("role") String role){
        return JPUser.isUserInRole(role);
    }
}
