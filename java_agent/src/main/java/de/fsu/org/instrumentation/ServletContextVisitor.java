package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ServletContextVisitor extends ClassVisitor {

//    public ServletContextVisitor(ClassVisitor cv, Class<?> extClass) {
//        super(cv, extClass);
//    }
    
    private Class<?> EXT_CLASS;
    static Logger LOGGER;
    protected String className;
    protected String classDesc;

    public ServletContextVisitor(ClassVisitor cv, Class<?> extClass) {
        super(ASM5, cv);
        setEXT_CLASS(extClass);
        LOGGER = Logger.getLogger(getClass());
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        this.classDesc = Type.getObjectType(className).getDescriptor();
    
        super.visit(version, access, name, signature, superName, interfaces);
    
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        try {
            // if has extender method for 'name' exist then exec
            // if no extender method exists NoSuchMethodException will be throw, no modification for method with 'name'
            Method extMethod = getClass().getMethod("ext_" + name, MethodVisitor.class);
            Method method = getEXT_CLASS().getMethod(name, String.class);
            String descriptor = Type.getMethodDescriptor(method);
            if (desc.equals(descriptor) && access == ACC_PUBLIC) {
                LOGGER.info("Change method " + name + " " + desc + " for " + className);
                MethodVisitor mv_redirect = cv.visitMethod(access, name, desc, signature, exceptions);
//                extMethod.invoke(this, mv_redirect);
                ext_getResource(mv_redirect);
    
                MethodVisitor mv_orig = cv.visitMethod(access, name + "_orig", desc, signature, exceptions);
    
                if (mv_orig != null) {
                    return mv_orig;
                }
            }
        } catch (NoSuchMethodException | SecurityException e) {
        } 
//        catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
    
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    public Class<?> getEXT_CLASS() {
        return EXT_CLASS;
    }

    private void setEXT_CLASS(Class<?> eXT_CLASS) {
        EXT_CLASS = eXT_CLASS;
    }

//    public void ext_getResourceAsStream(MethodVisitor mv) {
//        mv.visitCode();
//        Label l0 = new Label();
//        mv.visitLabel(l0);
//        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ServletContextExt");
//        mv.visitInsn(DUP);
//        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ServletContextExt", "<init>", "()V", false);
//        mv.visitVarInsn(ASTORE, 2);
//        Label l1 = new Label();
//        mv.visitLabel(l1);
//        mv.visitVarInsn(ALOAD, 2);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ServletContextExt", "_getResourceAsStream",
//                "(Ljava/lang/String;)Ljava/io/InputStream;", false);
//        mv.visitVarInsn(ASTORE, 3);
//        Label l2 = new Label();
//        mv.visitLabel(l2);
//        mv.visitVarInsn(ALOAD, 3);
//        Label l3 = new Label();
//        mv.visitJumpInsn(IFNULL, l3);
//        Label l4 = new Label();
//        mv.visitLabel(l4);
//        mv.visitVarInsn(ALOAD, 3);
//        mv.visitInsn(ARETURN);
//        mv.visitLabel(l3);
//        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "de/fsu/org/ext/ServletContextExt", "java/io/InputStream" },
//                0, null);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResourceAsStream_orig",
//                "(Ljava/lang/String;)Ljava/io/InputStream;", false);
//        mv.visitInsn(ARETURN);
//        Label l5 = new Label();
//        mv.visitLabel(l5);
//        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
//        mv.visitLocalVariable("path", "Ljava/lang/String;", null, l0, l5, 1);
//        mv.visitLocalVariable("contextExt", "Lde/fsu/org/ext/ServletContextExt;", null, l1, l5, 2);
//        mv.visitLocalVariable("is", "Ljava/io/InputStream;", null, l2, l5, 3);
//        mv.visitMaxs(2, 4);
//        mv.visitEnd();
//    }

    public void ext_getResource(MethodVisitor mv) {
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "de/fsu/org/ext/ServletContextExt", "_getResource", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNULL, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l2);
        mv.visitFrame(Opcodes.F_APPEND,1, new Object[] {"java/net/URL"}, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResource_orig", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitInsn(ARETURN);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLocalVariable("this", classDesc, null, l0, l4, 0);
        mv.visitLocalVariable("path", "Ljava/lang/String;", null, l0, l4, 1);
        mv.visitLocalVariable("resource", "Ljava/net/URL;", null, l1, l4, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }
}