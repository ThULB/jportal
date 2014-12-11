package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.*;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class ClassLoaderVisitor extends ClassVisitor {

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
        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/net/MalformedURLException");
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLineNumber(32, l3);
        mv.visitLdcInsn("xsl/jp-layout-main.xsl");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
        Label l4 = new Label();
        mv.visitJumpInsn(IFEQ, l4);
        Label l5 = new Label();
        mv.visitLabel(l5);
        mv.visitLineNumber(33, l5);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("########### Detect File: ");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
        mv.visitLabel(l0);
        mv.visitLineNumber(35, l0);
        mv.visitTypeInsn(NEW, "java/io/File");
        mv.visitInsn(DUP);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitLineNumber(36, l6);
        mv.visitLdcInsn("/Users/chi/Development/workspace/jportal_gradle/jportal_mcr_module/src/main/resources/xsl/jp-layout-main.xsl");
        Label l7 = new Label();
        mv.visitLabel(l7);
        mv.visitLineNumber(35, l7);
        mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitLineNumber(37, l8);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toURI", "()Ljava/net/URI;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/net/URI", "toURL", "()Ljava/net/URL;", false);
        Label l9 = new Label();
        mv.visitLabel(l9);
        mv.visitLineNumber(35, l9);
        mv.visitVarInsn(ASTORE, 2);
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitLineNumber(38, l10);
        mv.visitTypeInsn(NEW, "java/util/ArrayList");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
        mv.visitVarInsn(ASTORE, 3);
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitLineNumber(39, l11);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
        mv.visitInsn(POP);
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitLineNumber(40, l12);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "enumeration", "(Ljava/util/Collection;)Ljava/util/Enumeration;", false);
        mv.visitLabel(l1);
        mv.visitInsn(ARETURN);
        mv.visitLabel(l2);
        mv.visitLineNumber(41, l2);
        mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {"java/net/MalformedURLException"});
        mv.visitVarInsn(ASTORE, 2);
        Label l13 = new Label();
        mv.visitLabel(l13);
        mv.visitLineNumber(43, l13);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/net/MalformedURLException", "printStackTrace", "()V", false);
        mv.visitLabel(l4);
        mv.visitLineNumber(47, l4);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "getResources_orig", "(Ljava/lang/String;)Ljava/util/Enumeration;", false);
        mv.visitInsn(ARETURN);
        Label l14 = new Label();
        mv.visitLabel(l14);
        String classDesc = Type.getObjectType(className).getDescriptor();
        mv.visitLocalVariable("this", classDesc, null, l3, l14, 0);
        mv.visitLocalVariable("name", "Ljava/lang/String;", null, l3, l14, 1);
        mv.visitLocalVariable("url", "Ljava/net/URL;", null, l10, l2, 2);
        mv.visitLocalVariable("arrayList", "Ljava/util/ArrayList;", "Ljava/util/ArrayList<Ljava/net/URL;>;", l11, l2, 3);
        mv.visitLocalVariable("e", "Ljava/net/MalformedURLException;", null, l13, l4, 2);
        mv.visitMaxs(4, 4);
        mv.visitEnd();
    }
}