package de.fsu.org.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ServletContextTransformer implements ClassFileTransformer {

    static Logger LOGGER = Logger.getLogger(ServletContextTransformer.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Class<ServletContext> extClass = ServletContext.class;
        if (Tools.isAssignable(className, extClass.getName())) {
            LOGGER.info("Transform " + className);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            cr.accept(new ServletContextVisitor(cw, extClass), 0);
            return cw.toByteArray();
        }
        
        return classfileBuffer;
    }

}
