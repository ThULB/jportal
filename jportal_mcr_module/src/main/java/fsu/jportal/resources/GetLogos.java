package fsu.jportal.resources;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

@Path("logoImporter")
public class GetLogos {	
	private static Logger LOGGER = Logger.getLogger(GlobalMessageFile.class);
	
	@GET
	@Path("getList/{path:.*}")
	public Response getLogoList(@PathParam("path") String path) {
		String urlString = getLogoURL();
		if(urlString == null){
			return Response.serverError().build();
		}else{
			urlString = urlString + "/" + path;
		}
		
		try {
			URL url = new URL(urlString);
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0");
			InputStreamReader in2 = new InputStreamReader(connection.getInputStream());
			String contentType = connection.getContentType();
			
			return Response.ok(in2, contentType).build();
		} catch (IOException e) {
			LOGGER.error("Could not load the URL: " + urlString + " ---> error: " + e.getMessage());
		}
		return Response.serverError().build();
	}
	
	@GET
	@Path("getLogoURLBase")
	public String getLogoURL() {
		return MCRConfiguration.instance().getString("JP.Site.Logo.url", null);
	}
}
