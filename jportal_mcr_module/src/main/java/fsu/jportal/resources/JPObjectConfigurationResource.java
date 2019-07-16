package fsu.jportal.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fsu.jportal.backend.conf.ObjConfiguration;
import org.mycore.common.config.MCRConfiguration;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    public Response properties(@PathParam("id") String id, @PathParam("type") String type){
        Map<Object, Object> propertiesMap = ObjConfiguration.of(id)
                .getProperties(type)
                .map(Properties::entrySet)
                .map(Set::stream)
                .orElse(Stream.empty())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

        Gson gson = new Gson();
        return Response.ok(gson.toJson(propertiesMap)).build();
    }

//    @PUT
//    @Path("{id}/{type}")
//    @Consumes(MediaType.APPLICATION_JSON)
//    public Response addProperties(String data, @PathParam("id") String id, @PathParam("type") String type){
//        Gson gson = new Gson();
//        Map propsMap = gson.fromJson(data, Map.class);
//        ObjConfiguration.of(id)
//                .addProperties(propsMap, type)
//                .map(Response::);
//
//        return Response.created().build()
//    }
}
