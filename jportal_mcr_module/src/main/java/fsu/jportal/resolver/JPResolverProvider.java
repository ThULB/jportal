package fsu.jportal.resolver;

import fsu.jportal.nio.JarResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mycore.common.xml.MCRURIResolver.MCRResolverProvider;

import javax.xml.transform.URIResolver;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JPResolverProvider implements MCRResolverProvider {
    private static final Logger LOGGER = LogManager.getLogger(JPResolverProvider.class);
    @Override
    public Map<String, URIResolver> getURIResolverMapping() {
        HashMap<String, URIResolver> resolverMap = new HashMap<String, URIResolver>();
        LOGGER.info("Init resolver ....");
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            
            String pkgName = "fsu.jportal.resolver";
            Enumeration<URL> resources = classLoader.getResources(pkgName.replace(".", "/"));
            while (resources.hasMoreElements()) {
                URL url = (URL) resources.nextElement();
                JarResource jarResource = new JarResource(url);
                
                for (Path path : jarResource.listFiles()) {
                    String fileName = path.getFileName().toString();
                    
                    if(fileName.endsWith(".class")){
                        String className = fileName.replace(".class", "");
                        String qualClassName = pkgName + "." + className;
                        Class<?> resolverClass = Class.forName(qualClassName);
                        
                        URIResolverSchema schemaAnnot = resolverClass.getAnnotation(URIResolverSchema.class);
                        if(schemaAnnot != null){
                            String schema = schemaAnnot.schema();
                            Object resolverObj = resolverClass.newInstance();
                            if(resolverObj instanceof URIResolver){
                                resolverMap.put(schema, (URIResolver)resolverObj);
                                LOGGER.info("Add " + className + " with schema " + schema);
                            }
                            
                        }
                    }
                }
            }
        } catch (IOException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return resolverMap;
    }

}
