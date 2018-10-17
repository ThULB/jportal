package fsu.jportal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.function.Consumer;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.config.MCRConfiguration;

/**
 * Created by chi on 17.10.18.
 * @author Huu Chi Vu
 */
public class ProxyUtil {
    private static Logger LOGGER = LogManager.getLogger();

    public static void getConnection(String urlString, Consumer<URLConnection> connectionHandler, Consumer<Exception> errorHandler) {
        if (urlString == null) {
            errorHandler.accept(new NullPointerException("URL is null"));
        }

        try {
            URL url = new URL(urlString);
            URLConnection connection = url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");

            connectionHandler.accept(connection);
        } catch (IOException e) {
            errorHandler.accept(e);
        }
    }

    public static String getProperty(String property) {
        return MCRConfiguration.instance().getString(property, null);
    }

    public static Consumer<Exception> errorHandler(HttpServletRequest req) {
        return error -> LOGGER.error("An error occured with the request {}", req.getRequestURI(), error);
    }

    public static Consumer<URLConnection> connectionHandler(HttpServletResponse resp) {
        return connection -> {
            String url = connection.getURL().toString();

            try {
                InputStream inputStream = connection.getInputStream();
                String contentType = connection.getContentType();

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType(contentType);

                ServletOutputStream outputStream = resp.getOutputStream();

                byte[] buffer = new byte[1024];
                int len;
                while ((len = inputStream.read(buffer)) != -1){
                    outputStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                LOGGER.error("Could not load the URL: {}", url, e);
            }
        };
    }

    public static void getConnection(String url, HttpServletResponse resp, HttpServletRequest req) {
        getConnection(url, connectionHandler(resp), errorHandler(req));
    }
}
