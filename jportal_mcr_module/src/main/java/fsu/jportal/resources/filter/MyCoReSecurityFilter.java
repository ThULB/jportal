package fsu.jportal.resources.filter;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.api.model.PathValue;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

public class MyCoReSecurityFilter implements ResourceFilterFactory {
    @Context
    HttpServletRequest httprequest;

    @Override
    public List<ResourceFilter> create(AbstractMethod am) {
        RolesAllowed ra = am.getAnnotation(RolesAllowed.class);
        if (ra != null) {
            System.out.println("Resource: " + am);
            System.out.println("Resource: " + getPath(am) + " - " + getHttpMethod(am));
            return Collections.<ResourceFilter> singletonList(new BindMcrSessionFilter(httprequest, ra.value(), am));
        }

        return null;
    }

    protected String getHttpMethod(AbstractMethod am) {
        Class[] httpMethods = {POST.class,GET.class,PUT.class,DELETE.class};
        for (Class httpMethod : httpMethods) {
            if(am.isAnnotationPresent(httpMethod)){
                return httpMethod.getSimpleName();
            }
        }
        
        return null;
    }

    protected String getPath(AbstractMethod am) {
        PathValue resourcePath = am.getResource().getPath();
        if (resourcePath == null) {
            return null;
        } 
        
        String path = "/" + resourcePath.getValue();
        Path methodPath = am.getAnnotation(Path.class);
        if (methodPath != null) {
            return path + "/" + methodPath.value();
        }
        
        return path;
    }

}
