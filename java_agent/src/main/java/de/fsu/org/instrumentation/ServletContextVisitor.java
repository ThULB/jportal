package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.NEW;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ServletContextVisitor extends ExtClassVisitor {

    public ServletContextVisitor(ClassVisitor cv, Class<?> extClass) {
        super(cv, extClass);
    }
    
   

    public void ext_getResourceAsStream(MethodVisitor mv) {
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ServletContextExt");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ServletContextExt", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ServletContextExt", "_getResourceAsStream",
                "(Ljava/lang/String;)Ljava/io/InputStream;", false);
        mv.visitVarInsn(ASTORE, 3);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 3);
        Label l3 = new Label();
        mv.visitJumpInsn(IFNULL, l3);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l3);
        mv.visitFrame(Opcodes.F_APPEND, 2, new Object[] { "de/fsu/org/ext/ServletContextExt", "java/io/InputStream" },
                0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResourceAsStream_orig",
                "(Ljava/lang/String;)Ljava/io/InputStream;", false);
        mv.visitInsn(ARETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
        mv.visitLocalVariable("path", "Ljava/lang/String;", null, l0, l5, 1);
        mv.visitLocalVariable("contextExt", "Lde/fsu/org/ext/ServletContextExt;", null, l1, l5, 2);
        mv.visitLocalVariable("is", "Ljava/io/InputStream;", null, l2, l5, 3);
        mv.visitMaxs(2, 4);
        mv.visitEnd();
    }

    public void ext_getResource(MethodVisitor mv) {
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ServletContextExt");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ServletContextExt", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ServletContextExt", "_getResource", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitVarInsn(ASTORE, 3);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 3);
        Label l3 = new Label();
        mv.visitJumpInsn(IFNULL, l3);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l3);
        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"de/fsu/org/ext/ServletContextExt", "java/net/URL"}, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResource_orig", "(Ljava/lang/String;)Ljava/net/URL;", false);
        mv.visitInsn(ARETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
        mv.visitLocalVariable("path", "Ljava/lang/String;", null, l0, l5, 1);
        mv.visitLocalVariable("contextExt", "Lde/fsu/org/ext/ServletContextExt;", null, l1, l5, 2);
        mv.visitLocalVariable("resource", "Ljava/net/URL;", null, l2, l5, 3);
        mv.visitMaxs(2, 4);
        mv.visitEnd();
    }
}