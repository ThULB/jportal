package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ASM5;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.RETURN;

import org.apache.log4j.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public class WebResourceVisitor extends ClassVisitor {
    static final Logger LOGGER = Logger.getLogger(WebResourceVisitor.class);

    private String className;

    public WebResourceVisitor(ClassWriter cw) {
        super(ASM5, cw);
    }
    
    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.className = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }
    
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        
        if(name.equals("doGet") && desc.equals("(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V")){
            MethodVisitor mv_redirect = cv.visitMethod(access, name, desc, signature, exceptions);
            createDoGet(mv_redirect);
            
            MethodVisitor mv_orig = cv.visitMethod(access, name + "_orig", desc, signature, exceptions);
            
            if (mv_orig != null) {
                return mv_orig;
            }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
    
    private void createDoGet(MethodVisitor mv) {
        String classDesc = Type.getObjectType(className).getDescriptor();
        
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLineNumber(19, l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKESTATIC, "de/fsu/org/ext/ServletExt", "_doGet", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)Z", false);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitLineNumber(20, l2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "doGet_orig", "(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V", false);
        mv.visitLabel(l1);
        mv.visitLineNumber(22, l1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitInsn(RETURN);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitLocalVariable("this", classDesc, null, l0, l3, 0);
        mv.visitLocalVariable("req", "Ljavax/servlet/http/HttpServletRequest;", null, l0, l3, 1);
        mv.visitLocalVariable("resp", "Ljavax/servlet/http/HttpServletResponse;", null, l0, l3, 2);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }
}