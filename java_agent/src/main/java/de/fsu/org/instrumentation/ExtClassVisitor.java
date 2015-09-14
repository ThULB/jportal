package de.fsu.org.instrumentation;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ASM5;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

public abstract class ExtClassVisitor extends ClassVisitor {

    private Class<?> EXT_CLASS;
    static Logger LOGGER;
    protected String className;
    protected String classDesc;

    public ExtClassVisitor(ClassVisitor cv, Class<?> extClass) {
        super(ASM5, cv);
        setEXT_CLASS(extClass);
        LOGGER = LogManager.getLogger(getClass());
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
            Method extMethod = getClass().getDeclaredMethod("ext_" + name, MethodVisitor.class);
            Class<?>[] params = getParams(desc);
            Method method = getEXT_CLASS().getDeclaredMethod(name, params);
            int modifiers = method.getModifiers();
            String descriptor = Type.getMethodDescriptor(method);
            if (desc.equals(descriptor) && access == modifiers) {
                LOGGER.info("Change method " + name + " " + desc + " for " + className);
                MethodVisitor mv_redirect = cv.visitMethod(access, name, desc, signature, exceptions);
                extMethod.invoke(this, mv_redirect);
    
                MethodVisitor mv_orig = cv.visitMethod(access, name + "_orig", desc, signature, exceptions);
    
                if (mv_orig != null) {
                    return mv_orig;
                }
            }
        } catch (NoSuchMethodException | SecurityException e) {
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    
        return super.visitMethod(access, name, desc, signature, exceptions);
    }
    
    private Class<?>[] getParams(String methodDescriptor) throws ClassNotFoundException {
        Type[] argumentTypes = Type.getArgumentTypes(methodDescriptor);
        Class<?>[] classArray = new Class<?>[argumentTypes.length];
        for (int i = 0; i < argumentTypes.length; i++) {
            Type type = argumentTypes[i];
            Class<?> clazz = Class.forName(type.getClassName());
            classArray[i] = clazz;            
        }
        
        return classArray;
    }

    public Class<?> getEXT_CLASS() {
        return EXT_CLASS;
    }

    private void setEXT_CLASS(Class<?> eXT_CLASS) {
        EXT_CLASS = eXT_CLASS;
    }
}