package de.fsu.org.ext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import de.fsu.org.instrumentation.JavaAgent;

public class ClassLoaderExt {
    static final Logger LOGGER = Logger.getLogger(ClassLoaderExt.class);
            
    public static ArrayList<URL> getResources(String name) throws IOException {
        ArrayList<URL> arrayList = new ArrayList<URL>();
        String[] resourcePaths = JavaAgent.getArgsArrays();
        
        for (String path : resourcePaths) {
            Path classPath = Paths.get(path);
            Path resourcePath = classPath.resolve(name);
            if (Files.exists(resourcePath)) {
                LOGGER.info("Using resource: " + resourcePath.toString());
                try {
                    URL url = resourcePath.toUri().toURL();
                    arrayList.add(url);
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        
        return arrayList;
    }
    
}
