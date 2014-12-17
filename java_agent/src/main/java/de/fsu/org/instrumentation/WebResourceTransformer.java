package de.fsu.org.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class WebResourceTransformer implements ClassFileTransformer {
    
    static Logger LOGGER = Logger.getLogger(WebResourceTransformer.class);
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader cr = new ClassReader(classfileBuffer);
        ClassWriter cw = new ClassWriter(cr,0);
        cr.accept(new WebResourceVisitor(cw), 0);
        return cw.toByteArray();
    }

}
