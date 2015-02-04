package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INSTANCEOF;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class WebResourceVisitor extends ClassVisitor {
    static final Logger LOGGER = Logger.getLogger(WebResourceVisitor.class);

    private String className;

    private String descriptor;

    private Method getResourceMethod;


    
    public WebResourceVisitor(ClassVisitor cw) {
        super(ASM5, cw);
        
        try {
            getResourceMethod = ServletContext.class.getMethod("getResource", String.class);
            descriptor = Type.getMethodDescriptor(getResourceMethod);
        } catch (NoSuchMethodException | SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        
        super.visit(version, access, name, signature, superName, interfaces);
        
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        
        if(name.equals(getResourceMethod.getName()) && desc.equals(descriptor) && access == ACC_PUBLIC){
            LOGGER.info("Change method for " + className);
            MethodVisitor mv_redirect = cv.visitMethod(access, name, desc, signature, exceptions);
            createGetResource(mv_redirect);
            
            MethodVisitor mv_orig = cv.visitMethod(access, name + "_orig", desc, signature, exceptions);
            
            if (mv_orig != null) {
                return mv_orig;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
    
    private void createGetResource(MethodVisitor mv) {
        String classDesc = Type.getObjectType(className).getDescriptor();
        
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(INSTANCEOF, "javax/servlet/ServletContext");
        Label l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "de/fsu/org/ext/ServletContextExt", "_getResource", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitJumpInsn(IFNULL, l1);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l1);
        mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, className, "getResource_orig", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitInsn(ARETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
        mv.visitLocalVariable("path", "Ljava/lang/String;", null, l0, l5, 1);
        mv.visitLocalVariable("resource", "Ljava/net/URL;", null, l3, l1, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }
}