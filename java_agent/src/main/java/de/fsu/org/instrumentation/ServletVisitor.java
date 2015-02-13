package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class ServletVisitor extends ExtClassVisitor {

    public ServletVisitor(ClassVisitor cv, Class<?> extClass) {
        super(cv, extClass);
    }

    public void ext_doGet(MethodVisitor mv){
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ServletExt");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ServletExt", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 3);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ServletExt", "_doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Z", false);
        Label l2 = new Label();
        mv.visitJumpInsn(IFNE, l2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "doGet_orig", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", false);
        mv.visitLabel(l2);
        mv.visitFrame(F_APPEND,1, new Object[] {"de/fsu/org/ext/ServletExt"}, 0, null);
        mv.visitInsn(RETURN);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitLocalVariable("this", classDesc, null, l0, l4, 0);
        mv.visitLocalVariable("req", "Ljavax/servlet/http/HttpServletRequest;", null, l0, l4, 1);
        mv.visitLocalVariable("resp", "Ljavax/servlet/http/HttpServletResponse;", null, l0, l4, 2);
        mv.visitLocalVariable("servletExt", "Lde/fsu/org/ext/ServletExt;", null, l1, l4, 3);
        mv.visitMaxs(3, 4);
        mv.visitEnd();
    }
}
