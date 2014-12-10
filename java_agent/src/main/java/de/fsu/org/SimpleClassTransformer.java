package de.fsu.org;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import org.apache.log4j.Logger;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypePath;

import static org.objectweb.asm.Opcodes.*;

public class SimpleClassTransformer implements ClassFileTransformer {
    public static class MyMethodVisitor extends MethodVisitor {

        private String className;

        private String methodName;

        public MyMethodVisitor(MethodVisitor mv, String className, String methodName) {
            super(ASM5, mv);
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public void visitCode() {
            Label l0 = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(l0, l1, l2, "java/net/MalformedURLException");
            Label l3 = new Label();
            mv.visitLabel(l3);
            mv.visitLineNumber(29, l3);
            mv.visitLdcInsn("xsl/jp-layout-main.xsl");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z", false);
            Label l4 = new Label();
            mv.visitJumpInsn(IFEQ, l4);
            Label l5 = new Label();
            mv.visitLabel(l5);
            mv.visitLineNumber(30, l5);
            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Find Resource: ");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            mv.visitLabel(l0);
            mv.visitLineNumber(32, l0);
            mv.visitTypeInsn(NEW, "java/io/File");
            mv.visitInsn(DUP);
            Label l6 = new Label();
            mv.visitLabel(l6);
            mv.visitLineNumber(33, l6);
            mv.visitLdcInsn("/Users/chi/Development/workspace/jportal_gradle/jportal_mcr_module/src/main/resources/xsl/jp-layout-main.xsl");
            Label l7 = new Label();
            mv.visitLabel(l7);
            mv.visitLineNumber(32, l7);
            mv.visitMethodInsn(INVOKESPECIAL, "java/io/File", "<init>", "(Ljava/lang/String;)V", false);
            Label l8 = new Label();
            mv.visitLabel(l8);
            mv.visitLineNumber(34, l8);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/File", "toURI", "()Ljava/net/URI;", false);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/net/URI", "toURL", "()Ljava/net/URL;", false);
            Label l9 = new Label();
            mv.visitLabel(l9);
            mv.visitLineNumber(32, l9);
            mv.visitVarInsn(ASTORE, 2);
            Label l10 = new Label();
            mv.visitLabel(l10);
            mv.visitLineNumber(35, l10);
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
            mv.visitVarInsn(ASTORE, 3);
            Label l11 = new Label();
            mv.visitLabel(l11);
            mv.visitLineNumber(36, l11);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
            mv.visitInsn(POP);
            Label l12 = new Label();
            mv.visitLabel(l12);
            mv.visitLineNumber(37, l12);
            mv.visitVarInsn(ALOAD, 3);
            mv.visitMethodInsn(INVOKESTATIC, "java/util/Collections", "enumeration", "(Ljava/util/Collection;)Ljava/util/Enumeration;", false);
            mv.visitLabel(l1);
            mv.visitInsn(ARETURN);
            mv.visitLabel(l2);
            mv.visitLineNumber(38, l2);
            mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {"java/net/MalformedURLException"});
            mv.visitVarInsn(ASTORE, 2);
            Label l13 = new Label();
            mv.visitLabel(l13);
            mv.visitLineNumber(40, l13);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/net/MalformedURLException", "printStackTrace", "()V", false);
            mv.visitLabel(l4);
            
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitLdcInsn(".xsl");
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "contains", "(Ljava/lang/CharSequence;)Z", false);
//            Label l1 = new Label();
//            mv.visitJumpInsn(IFEQ, l1);
//            Label l2 = new Label();
//            mv.visitLabel(l2);
//            mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//            mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
//            mv.visitInsn(DUP);
//            mv.visitLdcInsn("$$$$$$$$$$$$$$$ " + className + " - " + methodName + ": ");
//            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
//            mv.visitVarInsn(ALOAD, 1);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//            mv.visitLabel(l1);
        }

    }
    
    public static class XSLMethodVisitor extends MethodVisitor{

        public XSLMethodVisitor(MethodVisitor mv) {
            super(ASM5, mv);
        }
        
        @Override
        public void visitIntInsn(int opcode, int operand) {
            // TODO Auto-generated method stub
            super.visitIntInsn(opcode, operand);
        }
        
    }
    
    static final Logger LOGGER = Logger.getLogger(SimpleClassTransformer.class);

    public static class ClassLoaderVisitor extends ClassVisitor {
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
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

            if (mv != null && ("getResources".equals(name) || "getResourceAsStream".equals(name))) {
                mv = new MyMethodVisitor(mv, className, name);
            }
            return mv;
        }
    }
    
    public static class XSLTransformerVisitor extends ClassVisitor{

        public XSLTransformerVisitor(ClassVisitor cw) {
            super(ASM5, cw);
        }
        
        @Override
        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
            super.visit(version, access, name, signature, superName, interfaces);
        }
        
        @Override
        public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
            MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
            
            if (mv != null && "checkTemplateUptodate".equals(name)) {
//                mv = new XSLMethodVisitor(mv);
                mv.visitCode();
                Label l0 = new Label();
                mv.visitLabel(l0);
                mv.visitLineNumber(156, l0);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "modifiedChecked", "J");
                mv.visitInsn(LSUB);
                mv.visitFieldInsn(GETSTATIC, "org/mycore/common/content/transformer/MCRXSLTransformer", "CHECK_PERIOD", "J");
                mv.visitInsn(LCMP);
                Label l1 = new Label();
                mv.visitJumpInsn(IFLE, l1);
                mv.visitInsn(ICONST_1);
                Label l2 = new Label();
                mv.visitJumpInsn(GOTO, l2);
                mv.visitLabel(l1);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitInsn(ICONST_0);
                mv.visitLabel(l2);
                mv.visitFrame(F_SAME1, 0, null, 1, new Object[] {INTEGER});
                mv.visitVarInsn(ISTORE, 1);
                Label l3 = new Label();
                mv.visitLabel(l3);
                mv.visitLineNumber(157, l3);
                mv.visitVarInsn(ILOAD, 1);
                Label l4 = new Label();
                mv.visitJumpInsn(IFEQ, l4);
                Label l5 = new Label();
                mv.visitLabel(l5);
                mv.visitLineNumber(158, l5);
                mv.visitInsn(ICONST_0);
                mv.visitVarInsn(ISTORE, 2);
                Label l6 = new Label();
                mv.visitLabel(l6);
                Label l7 = new Label();
                mv.visitJumpInsn(GOTO, l7);
                Label l8 = new Label();
                mv.visitLabel(l8);
                mv.visitLineNumber(159, l8);
                mv.visitFrame(F_APPEND,2, new Object[] {INTEGER, INTEGER}, 0, null);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitVarInsn(LSTORE, 3);
                Label l9 = new Label();
                mv.visitLabel(l9);
                mv.visitLineNumber(160, l9);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templates", "[Ljavax/xml/transform/Templates;");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(AALOAD);
                Label l10 = new Label();
                mv.visitJumpInsn(IFNULL, l10);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "modified", "[J");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(LALOAD);
                mv.visitVarInsn(LLOAD, 3);
                mv.visitInsn(LCMP);
                Label l11 = new Label();
                mv.visitJumpInsn(IFGE, l11);
                mv.visitLabel(l10);
                mv.visitLineNumber(161, l10);
                mv.visitFrame(F_APPEND,1, new Object[] {LONG}, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templateSources", "[Lorg/mycore/common/xsl/MCRTemplatesSource;");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/mycore/common/xsl/MCRTemplatesSource", "getSource", "()Ljavax/xml/transform/sax/SAXSource;", false);
                mv.visitVarInsn(ASTORE, 5);
                Label l12 = new Label();
                mv.visitLabel(l12);
                mv.visitLineNumber(162, l12);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templates", "[Ljavax/xml/transform/Templates;");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "tFactory", "Ljavax/xml/transform/sax/SAXTransformerFactory;");
                mv.visitVarInsn(ALOAD, 5);
                mv.visitMethodInsn(INVOKEVIRTUAL, "javax/xml/transform/sax/SAXTransformerFactory", "newTemplates", "(Ljavax/xml/transform/Source;)Ljavax/xml/transform/Templates;", false);
                mv.visitInsn(AASTORE);
                Label l13 = new Label();
                mv.visitLabel(l13);
                mv.visitLineNumber(163, l13);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templates", "[Ljavax/xml/transform/Templates;");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(AALOAD);
                Label l14 = new Label();
                mv.visitJumpInsn(IFNONNULL, l14);
                Label l15 = new Label();
                mv.visitLabel(l15);
                mv.visitLineNumber(164, l15);
                mv.visitTypeInsn(NEW, "javax/xml/transform/TransformerConfigurationException");
                mv.visitInsn(DUP);
                mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("XSLT Stylesheet could not be compiled: ");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false);
                Label l16 = new Label();
                mv.visitLabel(l16);
                mv.visitLineNumber(165, l16);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templateSources", "[Lorg/mycore/common/xsl/MCRTemplatesSource;");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, "org/mycore/common/xsl/MCRTemplatesSource", "getURL", "()Ljava/net/URL;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/Object;)Ljava/lang/StringBuilder;", false);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false);
                Label l17 = new Label();
                mv.visitLabel(l17);
                mv.visitLineNumber(164, l17);
                mv.visitMethodInsn(INVOKESPECIAL, "javax/xml/transform/TransformerConfigurationException", "<init>", "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
                mv.visitLabel(l14);
                mv.visitLineNumber(167, l14);
                mv.visitFrame(F_APPEND,1, new Object[] {"javax/xml/transform/sax/SAXSource"}, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "modified", "[J");
                mv.visitVarInsn(ILOAD, 2);
                mv.visitVarInsn(LLOAD, 3);
                mv.visitInsn(LASTORE);
                mv.visitLabel(l11);
                mv.visitLineNumber(158, l11);
                mv.visitFrame(F_CHOP,2, null, 0, null);
                mv.visitIincInsn(2, 1);
                mv.visitLabel(l7);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ILOAD, 2);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "templateSources", "[Lorg/mycore/common/xsl/MCRTemplatesSource;");
                mv.visitInsn(ARRAYLENGTH);
                mv.visitJumpInsn(IF_ICMPLT, l8);
                Label l18 = new Label();
                mv.visitLabel(l18);
                mv.visitLineNumber(170, l18);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
                mv.visitFieldInsn(PUTFIELD, "org/mycore/common/content/transformer/MCRXSLTransformer", "modifiedChecked", "J");
                mv.visitLabel(l4);
                mv.visitLineNumber(172, l4);
                mv.visitFrame(F_CHOP,1, null, 0, null);
                mv.visitInsn(RETURN);
                Label l19 = new Label();
                mv.visitLabel(l19);
                mv.visitLocalVariable("this", "Lorg/mycore/common/content/transformer/MCRXSLTransformer;", null, l0, l19, 0);
                mv.visitLocalVariable("check", "Z", null, l3, l19, 1);
                mv.visitLocalVariable("i", "I", null, l6, l18, 2);
                mv.visitLocalVariable("lastModified", "J", null, l9, l11, 3);
                mv.visitLocalVariable("source", "Ljavax/xml/transform/sax/SAXSource;", null, l12, l11, 5);
                mv.visitMaxs(5, 6);
                mv.visitEnd();
                return null;
            }
            return mv;
        }
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);
        boolean useClassWriter = false;
        if (className.contains("ClassLoader")) {
            ClassLoaderVisitor classPrinter = new ClassLoaderVisitor(classWriter);
            classReader.accept(classPrinter, 0);
            useClassWriter = true;
        }
        
        if ("org/mycore/common/content/transformer/MCRXSLTransformer".equals(className)) {
            XSLTransformerVisitor xslTransformerVisitor = new XSLTransformerVisitor(classWriter);
            classReader.accept(xslTransformerVisitor, 0);
            useClassWriter = true;
        }
        
        if(useClassWriter) {
            return classWriter.toByteArray();
        }
        
        return classfileBuffer;
    }

}
