package de.fsu.org.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fsu.org.instrumentation.JavaAgent;

public class ServletContextExt {
    static final Logger LOGGER = LogManager.getLogger(ServletContextExt.class);
        
    public URL _getResource(String path) throws MalformedURLException {
        String[] resources = JavaAgent.getArgsArrays();
        if(!path.startsWith("/")){
            path = "/" + path;
        }
        
        path = "META-INF/resources" + path;
        
        for (String resource : resources) {
            Path classPath = Paths.get(resource);

            Path resourcePath = classPath.resolve(path);
            if (Files.exists(resourcePath)) {
                LOGGER.info("Using resource: " + resourcePath.toString());
                return resourcePath.toUri().toURL();
            }
        }
        
        return null;
    }

    public InputStream _getResourceAsStream(String path) {
        try {
            return _getResource(path).openStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }
}
