package fsu.jportal.servlet;

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static fsu.jportal.util.ProxyUtil.getConnection;
import static fsu.jportal.util.ProxyUtil.getProperty;

/**
 * Created by chi on 17.10.18.
 * @author Huu Chi Vu
 */
public class PDFProxyServlet extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = Optional.ofNullable(req.getPathInfo()).orElse("");
        String query = Optional.ofNullable(req.getQueryString()).orElse("");
        String pdfUrl = getProperty("JP.Viewer.PDFCreatorURI");
        String url = pdfUrl + path + "?" + query;

        getConnection(url, resp, req);
    }
}
