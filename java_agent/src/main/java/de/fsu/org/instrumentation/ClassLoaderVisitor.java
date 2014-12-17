package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.*;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ClassLoaderVisitor extends ClassVisitor {
    static final Logger LOGGER = Logger.getLogger(ClassLoaderVisitor.class);

    private String className;

    public ClassLoaderVisitor(ClassWriter cv) {
        super(ASM5, cv);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if ("getResources".equals(name)) {
            MethodVisitor mv_redirect = cv.visitMethod(access, name, desc, signature, exceptions);
            
            createRedirect(mv_redirect);
            
            MethodVisitor mv_orig = cv.visitMethod(access, name + "_orig", desc, signature, exceptions);

            if (mv_orig != null) {
                return mv_orig;
            }
        }
        
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private void createRedirect(MethodVisitor mv) {
        String classDesc = Type.getObjectType(className).getDescriptor();
        
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(38, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "de/fsu/org/ext/ClassLoaderExt", "getResources", "(Ljava/lang/String;)Ljava/util/ArrayList;", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitLineNumber(39, l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "isEmpty", "()Z", false);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNE, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(40, l3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "enumeration", "(Ljava/util/Collection;)Ljava/util/Enumeration;", false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l2);
        mv.visitLineNumber(43, l2);
        mv.visitFrame(F_APPEND,1, new Object[] {"java/util/ArrayList"}, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResources_orig", "(Ljava/lang/String;)Ljava/util/Enumeration;", false);
        mv.visitInsn(ARETURN);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLocalVariable("this", classDesc, null, l0, l4, 0);
        mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l4, 1);
        mv.visitLocalVariable("resources", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Ljava/net/URL;>;", l1, l4, 2);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }
}