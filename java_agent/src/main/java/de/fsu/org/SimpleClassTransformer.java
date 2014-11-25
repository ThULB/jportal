package de.fsu.org;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;

public class SimpleClassTransformer implements ClassFileTransformer {
    static final Logger LOGGER = Logger.getLogger(SimpleClassTransformer.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        
        if(ClassLoader.class.isAssignableFrom(classBeingRedefined)){
            LOGGER.info("Classloader : " + classBeingRedefined.getCanonicalName());
        }

        return classfileBuffer;
    }

}
