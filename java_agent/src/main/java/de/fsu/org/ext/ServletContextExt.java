package de.fsu.org.ext;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.log4j.Logger;

import de.fsu.org.instrumentation.JavaAgent;

public class ServletContextExt {
    static final Logger LOGGER = Logger.getLogger(ServletContextExt.class);
        
    public static URL _getResource(String path) throws MalformedURLException {
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
}
