package de.fsu.org.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.fsu.org.ext.ServletExt;

public class ServletExtFiddle extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ServletExt servletExt = new ServletExt();
        if(!servletExt._doGet(req, resp)){
            doGet_orig(req, resp);
        }
    }

    public void doGet_orig(HttpServletRequest req, HttpServletResponse resp) {
        // TODO Auto-generated method stub
        
    }
}
