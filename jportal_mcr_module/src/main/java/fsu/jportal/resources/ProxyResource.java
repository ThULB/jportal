package fsu.jportal.resources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Function;

@Path("proxy")
public class ProxyResource {
    private static Logger LOGGER = LogManager.getLogger(ProxyResource.class);

    @GET
    @Path("logo/{path:.*}")
    public Response get(@PathParam("path") String path) {
        return resolveURL(getProperty("JP.Site.Logo.url"), url -> url + "/" + path);
    }

    @GET
    @Path("pdf{path:.*}")
    public Response getPDFCreatorURL(@PathParam("path") String path, @Context UriInfo info) {
        Function<String,String> urlModifier = url -> {
            String query = info.getRequestUri().getQuery();
            String p = !path.equals("") ? "/" + path : path;
            return url + p + "?" + query;
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
        return MCRConfiguration.instance().getString(property, null);
    }
}