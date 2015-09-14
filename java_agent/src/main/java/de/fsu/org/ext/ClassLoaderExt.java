package de.fsu.org.ext;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fsu.org.instrumentation.JavaAgent;

public class ClassLoaderExt {
    static final Logger LOGGER = LogManager.getLogger(ClassLoaderExt.class);

    public ArrayList<URL> _getResources(String name) throws IOException {
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

    public URL _getResource(String name) {
        String[] resourcePaths = JavaAgent.getArgsArrays();

        for (String path : resourcePaths) {
            Path classPath = Paths.get(path);
            Path resourcePath = classPath.resolve(name);
            if (Files.exists(resourcePath)) {
                LOGGER.info("Using resource: " + resourcePath.toString());
                try {
                    return resourcePath.toUri().toURL();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public InputStream _getResourceAsStream(String name) {
        try {
            return _getResource(name).openStream();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }
}
