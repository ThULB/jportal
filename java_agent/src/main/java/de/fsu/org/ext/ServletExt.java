package de.fsu.org.ext;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import de.fsu.org.instrumentation.JavaAgent;

public class ServletExt {
static final Logger LOGGER = LogManager.getLogger(ServletExt.class);
    
    public boolean _doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] resourcePaths = JavaAgent.getArgsArrays();
        String reqPath = getReqPath(req);

        for (String path : resourcePaths) {
            Path classPath = Paths.get(path);

            Path resourcePath = classPath.resolve(reqPath);
            if (Files.exists(resourcePath) && !Files.isDirectory(resourcePath)) {
                LOGGER.info("Using resource: " + resourcePath.toString());
                try {
                    resp.setContentType(Files.probeContentType(resourcePath));
                    Files.copy(resourcePath, resp.getOutputStream());
                    return true;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private String getReqPath(HttpServletRequest req) {
        String reqPath = req.getServletPath();
        return "META-INF/resources" + reqPath;
    }
}
