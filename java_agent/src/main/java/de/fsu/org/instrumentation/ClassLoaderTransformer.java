package de.fsu.org.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import de.fsu.org.ext.ClassLoaderExt;

public class ClassLoaderTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals(ClassLoaderExt.class.getName().replace('.', '/')) && className.contains("ClassLoader")) {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassWriter cw = new ClassWriter(cr, 0);
            ClassLoaderVisitor classLoaderVisitor = new ClassLoaderVisitor(cw);
            cr.accept(classLoaderVisitor, 0);
            return cw.toByteArray();
        }
        
        return classfileBuffer;
    }

}
