package fsu.jportal.resolver;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface URIResolverSchema {
    String schema();
}
