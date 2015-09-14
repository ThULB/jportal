package de.fsu.org.instrumentation;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

public class MCRXSLTransformerTransformer implements ClassFileTransformer {
    
    static final Logger LOGGER = LogManager.getLogger(MCRXSLTransformerTransformer.class);

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        
        if ("org/mycore/common/content/transformer/MCRXSLTransformer".equals(className)) {
            ClassReader classReader = new ClassReader(classfileBuffer);
            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
            MCRXSLTransformerVisitor xslTransformerVisitor = new MCRXSLTransformerVisitor(classWriter);
            classReader.accept(xslTransformerVisitor, 0);
            return classWriter.toByteArray();
        }
        
        return classfileBuffer;
    }

}
