package de.fsu.org.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class ClassLoaderTransformer implements ClassFileTransformer {
    static Logger LOGGER = LogManager.getLogger(ClassLoaderTransformer.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Class<ClassLoader> extClass = ClassLoader.class;
        if (Tools.isAssignable(className, extClass.getName())) {
            LOGGER.info("Transform " + className);
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassLoaderVisitor classLoaderVisitor = new ClassLoaderVisitor(cw, extClass);
            cr.accept(classLoaderVisitor, 0);
            return cw.toByteArray();
        }
        
        return classfileBuffer;
    }
}
