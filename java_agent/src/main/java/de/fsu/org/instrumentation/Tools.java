package de.fsu.org.instrumentation;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;

public class Tools{
    static Logger LOGGER = LogManager.getLogger(Tools.class);
    public static boolean isAssignable(String className, String ofClass) {
        LOGGER.info("isAssignable " + className + " - " + ofClass);
        className = className.replace('.', '/');
        ofClass = ofClass.replace('.', '/');

        if(className.contains("org/akhikhl/gretty")){
            return false;
        }

        if (className.equals(ofClass)) {
            return true;
        }

        try {
            ClassReader cr = new ClassReader(className);
            String[] interfaces = cr.getInterfaces();
            for (String curInterface : interfaces) {
                if (curInterface.equals(ofClass)) {
                    return true;
                }
            }

            String superName = cr.getSuperName();
            if (superName == null) {
                return false;
            } else {
                return isAssignable(superName, ofClass);
            }

        } catch (IOException e) {
        }

        return false;
    }
}