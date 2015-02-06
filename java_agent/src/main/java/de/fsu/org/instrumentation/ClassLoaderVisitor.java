package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.*;


import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ClassLoaderVisitor extends ExtClassVisitor {
    

    public ClassLoaderVisitor(ClassVisitor cv, Class<?> extClass) {
        super(cv, extClass);
    }

    public void ext_getResources(MethodVisitor mv) {
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ClassLoaderExt");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ClassLoaderExt", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 2);
        Label l1 = new Label();
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ClassLoaderExt", "_getResources", "(Ljava/lang/String;)Ljava/util/ArrayList;", false);
        mv.visitVarInsn(ASTORE, 3);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "isEmpty", "()Z", false);
        Label l3 = new Label();
        mv.visitJumpInsn(IFNE, l3);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "enumeration", "(Ljava/util/Collection;)Ljava/util/Enumeration;", false);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l3);
        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"de/fsu/org/ext/ClassLoaderExt", "java/util/ArrayList"}, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResources_orig", "(Ljava/lang/String;)Ljava/util/Enumeration;", false);
        mv.visitInsn(ARETURN);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
        mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l5, 1);
        mv.visitLocalVariable("loaderExt", "Lde/fsu/org/ext/ClassLoaderExt;", null, l1, l5, 2);
        mv.visitLocalVariable("resources", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Ljava/net/URL;>;", l2, l5, 3);
        mv.visitMaxs(2, 4);
        mv.visitEnd();
    }
    
//    public void ext_getResource(MethodVisitor mv){
//        mv.visitCode();
//        Label l0 = new Label();
//        mv.visitLabel(l0);
//        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ClassLoaderExt");
//        mv.visitInsn(DUP);
//        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ClassLoaderExt", "<init>", "()V", false);
//        mv.visitVarInsn(ASTORE, 2);
//        Label l1 = new Label();
//        mv.visitLabel(l1);
//        mv.visitVarInsn(ALOAD, 2);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ClassLoaderExt", "_getResource", "(Ljava/lang/String;)Ljava/net/URL;", false);
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
//        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"de/fsu/org/ext/ClassLoaderExt", "java/net/URL"}, 0, null);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResource_orig", "(Ljava/lang/String;)Ljava/net/URL;", false);
//        mv.visitInsn(ARETURN);
//        Label l5 = new Label();
//        mv.visitLabel(l5);
//        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
//        mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l5, 1);
//        mv.visitLocalVariable("loaderExt", "Lde/fsu/org/ext/ClassLoaderExt;", null, l1, l5, 2);
//        mv.visitLocalVariable("is", "Ljava/net/URL;", null, l2, l5, 3);
//        mv.visitMaxs(2, 4);
//        mv.visitEnd();
//    }
    
//    public void ext_getResourceAsStream(MethodVisitor mv){
//        mv.visitCode();
//        Label l0 = new Label();
//        mv.visitLabel(l0);
//        mv.visitTypeInsn(NEW, "de/fsu/org/ext/ClassLoaderExt");
//        mv.visitInsn(DUP);
//        mv.visitMethodInsn(INVOKESPECIAL, "de/fsu/org/ext/ClassLoaderExt", "<init>", "()V", false);
//        mv.visitVarInsn(ASTORE, 2);
//        Label l1 = new Label();
//        mv.visitLabel(l1);
//        mv.visitVarInsn(ALOAD, 2);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "de/fsu/org/ext/ClassLoaderExt", "_getResourceAsStream", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
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
//        mv.visitFrame(Opcodes.F_APPEND,2, new Object[] {"de/fsu/org/ext/ClassLoaderExt", "java/io/InputStream"}, 0, null);
//        mv.visitVarInsn(ALOAD, 0);
//        mv.visitVarInsn(ALOAD, 1);
//        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResourceAsStream_orig", "(Ljava/lang/String;)Ljava/io/InputStream;", false);
//        mv.visitInsn(ARETURN);
//        Label l5 = new Label();
//        mv.visitLabel(l5);
//        mv.visitLocalVariable("this", classDesc, null, l0, l5, 0);
//        mv.visitLocalVariable("name", "Ljava/lang/String;", null, l0, l5, 1);
//        mv.visitLocalVariable("loaderExt", "Lde/fsu/org/ext/ClassLoaderExt;", null, l1, l5, 2);
//        mv.visitLocalVariable("is", "Ljava/io/InputStream;", null, l2, l5, 3);
//        mv.visitMaxs(2, 4);
//        mv.visitEnd();
//    }
}