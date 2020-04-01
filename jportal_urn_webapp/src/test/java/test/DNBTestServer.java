package test;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import fsu.thulb.webapp.DNBUrnResource;

public class DNBTestServer {
    private static final Logger LOGGER = LogManager.getLogger();
    private final HttpServer httpServer;

    private DNBTestServer(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    public static DNBTestServer start(URI baseURIWithPort){
        ResourceConfig rc = new ResourceConfig(DNBUrnResource.class);
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(baseURIWithPort, rc, true);
        LOGGER.info("DNB Testserver started - " + baseURIWithPort.toString());
        return new DNBTestServer(httpServer);
    }

    public void stop(){
        this.httpServer.shutdownNow();
    }
}
