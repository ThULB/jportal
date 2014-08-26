package fsu.thulb.servlets;

import java.util.Set;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

public class JPServletContainerInitializer implements ServletContainerInitializer {

    @Override
    public void onStartup(Set<Class<?>> arg0, ServletContext arg1) throws ServletException {
        System.out.println("JP Init......................");
    }

}
