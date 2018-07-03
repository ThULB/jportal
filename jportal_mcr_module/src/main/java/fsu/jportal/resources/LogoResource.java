package fsu.jportal.resources;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

@Path("proxy")
public class LogoResource {
    private static Logger LOGGER = LogManager.getLogger(LogoResource.class);

    @GET
    @Path("logo/{path:.*}")
    public Response get(@PathParam("path") String path) {
        String urlString = getLogoURL();
        if (urlString == null) {
            return Response.serverError().build();
        } else {
            urlString = urlString + "/" + path;
        }
        return resolveURL(urlString);
    }

    private Response resolveURL(String urlString) {
        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStreamReader reader = new InputStreamReader(connection.getInputStream());
            String contentType = connection.getContentType();

            return Response.ok(reader, contentType).build();
        } catch (IOException e) {
            LOGGER.error("Could not load the URL: " + urlString + " ---> error: " + e.getMessage());
        }
        return Response.serverError().build();
    }

    @GET
    @Path("logoURLBase")
    public String getLogoURL() {
        return getProperty("JP.Site.Logo.url");
    }

    public String getPDFCreatorURL() {
        return getProperty("MCR.Viewer.PDFCreatorURI");
    }

    private String getProperty(String property) {
        return MCRConfiguration.instance().getString(property, null);
    }
}
