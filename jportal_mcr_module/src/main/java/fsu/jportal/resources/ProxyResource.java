package fsu.jportal.resources;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import fsu.jportal.backend.mcr.JPConfig;

@Path("proxy")
public class ProxyResource {
    private static Logger LOGGER = LogManager.getLogger(ProxyResource.class);

    @GET
    @Path("logo/{path:.*}")
    public Response get(@PathParam("path") String path) {
        return resolveURL(getProperty("JP.Site.Logo.url"), url -> url + "/" + path.replaceAll(" ", "%20"));
    }

    @GET
    @Path("pdf{path:.*}")
    public Response getPDFCreatorURL(@PathParam("path") String path, @Context UriInfo info) {
        Function<String,String> urlModifier = url -> {
            String query = info.getRequestUri().getQuery();
            return url + path + "?" + query;
        };

        return resolveURL(getProperty("JP.Viewer.PDFCreatorURI"), urlModifier);
    }

    private Response resolveURL(String urlString, Function<String,String> urlModifier) {
        if (urlString == null) {
            return Response.serverError().build();
        }

        if(urlModifier != null) {
            urlString = urlModifier.apply(urlString);
        }

        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            return Response.ok(connection.getContent(), connection.getContentType()).build();
        } catch (IOException e) {
            LOGGER.error("Could not load the URL: " + urlString + " ---> error: " + e.getMessage());
        }
        return Response.serverError().build();
    }

    private String getProperty(String property) {
        return JPConfig.getString(property, null);
    }
}
