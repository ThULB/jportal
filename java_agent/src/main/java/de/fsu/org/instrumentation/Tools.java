package de.fsu.org.instrumentation;

import java.io.IOException;

import org.objectweb.asm.ClassReader;

public class Tools{
    public static boolean isAssignable(String className, String ofClass){
        className = className.replace('.', '/');
        ofClass = ofClass.replace('.', '/');
        
        if(className.equals(ofClass)){
            return true;
        }
        
        try {
            ClassReader cr = new ClassReader(className);
            String[] interfaces = cr.getInterfaces();
            for (String curInterface : interfaces) {
                if(curInterface.equals(ofClass)){
                    return true;
                }
            }
            
            String superName = cr.getSuperName();
            if(superName == null){
                return false;
            }else{
                return isAssignable(superName, ofClass);
            }
            
        } catch (IOException e) {
        }
        
        return false;
    }
}