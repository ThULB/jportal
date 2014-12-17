package de.fsu.org.ext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import de.fsu.org.instrumentation.JavaAgent;

public class ServletExt {
    static final Logger LOGGER = Logger.getLogger(ServletExt.class);
    
    public static boolean _doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] resourcePaths = JavaAgent.getArgsArrays();
        String reqPath = getReqPath(req);

        for (String path : resourcePaths) {
            Path classPath = Paths.get(path);

            Path resourcePath = classPath.resolve(reqPath);
            if (Files.exists(resourcePath)) {
                LOGGER.info("Using resource: " + resourcePath.toString());
                try {
                    Files.copy(resourcePath, resp.getOutputStream());
                    return true;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private static String getReqPath(HttpServletRequest req) {
        String reqPath = req.getServletPath();
        return "META-INF/resources" + reqPath;
    }
}
