package fsu.thulb.webapp;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

/**
 * Created by chi on 27.03.20
 *
 * @author Huu Chi Vu
 */
public class DNBUrnApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        final Set<Class<?>> classes = new HashSet<>();
        classes.add(DNBUrnResource.class);
        return classes;
    }
}
