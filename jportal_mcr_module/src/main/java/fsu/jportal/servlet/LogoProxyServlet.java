package fsu.jportal.servlet;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static fsu.jportal.util.ProxyUtil.getConnection;
import static fsu.jportal.util.ProxyUtil.getProperty;

/**
 * Created by chi on 17.10.18.
 * @author Huu Chi Vu
 */
public class LogoProxyServlet extends HttpServlet {
    private static Logger LOGGER = LogManager.getLogger();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = Optional.ofNullable(req.getPathInfo()).orElse("");
        String logoUrl = getProperty("JP.Site.Logo.url");
        logoUrl = logoUrl + path.replaceAll(" ", "%20");

        getConnection(logoUrl, resp, req);
    }
}
