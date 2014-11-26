package de.fsu.org;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class SimpleClassTransformer implements ClassFileTransformer {
    static final Logger LOGGER = Logger.getLogger(SimpleClassTransformer.class);

    static class ClassPrinter extends ClassVisitor{
        public ClassPrinter(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }
        
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            LOGGER.info(name + " extend " + superName + " {");
        }
        
        @Override
        public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
            LOGGER.info("    " + desc + " " + name);
            return null;
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            LOGGER.info("    " + name + desc);
            return null;
        }
        
        @Override
        public void visitEnd() {
            LOGGER.info("}");
        }
    }
    
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if("fsu/jportal/resolver/OptionFolderResolver".equals(className)){
            LOGGER.info("Class : " + className);
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, 0);
            ClassPrinter classPrinter = new ClassPrinter(classWriter);
            classReader.accept(classPrinter, 0);
            return classWriter.toByteArray();
        }

        return classfileBuffer;
    }

}
