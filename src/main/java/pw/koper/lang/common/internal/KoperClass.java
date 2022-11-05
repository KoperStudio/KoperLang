package pw.koper.lang.common.internal;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.CompilationException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static pw.koper.lang.common.StringUtil.toJvmName;

public class KoperClass {
    private final String sourceFileName;
    public String name;
    public boolean isPublic;
    public boolean isAbstract;
    public boolean isStatic;
    public boolean isData;
    public String superClass;
    public ClassType classType;
    public Set<String> interfaces;
    public Set<KoperMethod> methods = new HashSet<>();
    public Set<KoperField> fields = new HashSet<>();

    // Map from short name to full name
    public HashMap<String, String> imports = new HashMap<>();
    private boolean staticConstructor;

    public KoperClass(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
    public byte[] generateClass() throws CompilationException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visitSource(this.sourceFileName.replace(".koper", ".class"), null);
        int access = ACC_PUBLIC | ACC_SUPER;
        switch (classType) {
            case INTERFACE -> access |= ACC_INTERFACE;
            case ENUM -> access |= ACC_ENUM;
        }
        if(isAbstract) {
            access |= ACC_ABSTRACT;
        }

        // method generation
        for(KoperMethod method : methods) {
            method.generateBytecode(classWriter);
        }

        HashMap<String, String> staticFields = new HashMap<>();
        classWriter.visit(Opcodes.V11, access, name, null, superClass, interfaces.toArray(new String[0]));
        for(KoperField field : fields) {

            field.generateBytecode(classWriter);
            if(field.isStatic()) {
                staticFields.put(field.getName(), field.getType().toDescriptor());
            }

            if(field.hasGetter) {
                generateGetter(classWriter, field);
            }

            if(field.hasSetter) {
                generateSetter(classWriter, field);
            }
        }

        if(staticConstructor) {
            MethodVisitor staticConstructor = classWriter.visitMethod(ACC_PUBLIC | ACC_STATIC, "<cinit>", "()V", null, new String[0]);
            for(Map.Entry<String, String> entry : staticFields.entrySet()) {
                staticConstructor.visitLdcInsn(new Object());
                staticConstructor.visitFieldInsn(PUTSTATIC, name, entry.getKey(), entry.getValue());
            }
            staticConstructor.visitEnd();
        }

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
    // Returns true if class isn't enum and interface
    public boolean isFullClass() {
        return classType == ClassType.CLASS;
    }

    public String getClassByName(String name) {
        if(name.contains(".")) return toJvmName(name);

        return imports.get(name);
    }

    public void willHaveStaticConstructor() {
        this.staticConstructor = true;
    }

    private void generateGetter(ClassWriter writer, KoperField field) {
        MethodVisitor getter = writer.visitMethod(ACC_PUBLIC, "get" + StringUtils.capitalize(field.getName()), "()" + field.getType().toDescriptor(), null, new String[0]);
        Label body = new Label();
        getter.visitLabel(body);
        getter.visitVarInsn(ALOAD, 0);
        getter.visitFieldInsn(GETFIELD, name, field.getName(), field.getType().toDescriptor());
        getter.visitInsn(ARETURN);
        Label meta = new Label();
        getter.visitLabel(meta);
        getter.visitLocalVariable("this", "L" + name + ";", null, body, meta, 0);
        getter.visitEnd();
    }

    private void generateSetter(ClassWriter writer, KoperField field) {
        MethodVisitor setter = writer.visitMethod(ACC_PUBLIC, "set" + StringUtils.capitalize(field.getName()), "(" + field.getType().toDescriptor() + ")V", null, new String[0]);
        Label body = new Label();
        setter.visitLabel(body);

        setter.visitVarInsn(ALOAD, 0);
        setter.visitVarInsn(ALOAD, 1);
        setter.visitFieldInsn(PUTFIELD, name, field.getName(), field.getType().toDescriptor());
        setter.visitInsn(RETURN);
        Label meta = new Label();
        setter.visitLabel(meta);
        setter.visitLocalVariable("this", "L" + name + ";", null, body, meta, 0);
        setter.visitLocalVariable(field.getName(), field.getType().toDescriptor(), null, body, meta, 1);
        setter.visitEnd();
    }
}
