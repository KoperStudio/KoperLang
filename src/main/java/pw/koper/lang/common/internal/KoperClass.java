package pw.koper.lang.common.internal;

import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.KoperCompiler;

import java.util.*;

import static org.objectweb.asm.Opcodes.*;
import static pw.koper.lang.util.StringUtil.toJvmName;

public class KoperClass {
    private final String sourceFileName;
    public final KoperCompiler compiler;
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
    public StaticConstructor staticConstructor = null;
    public HashSet<KoperConstructor> constructors = new HashSet<>();

    public HashSet<KoperField> toStaticInit = new HashSet<>();
    public HashSet<KoperField> toObjectInit = new HashSet<>();
    public List<Annotation> annotationList = new ArrayList<>();

    // Map from short name to full name
    public HashMap<String, String> imports = new HashMap<>();

    public KoperClass(String sourceFileName, KoperCompiler compiler) {
        this.sourceFileName = sourceFileName;
        this.compiler = compiler;
    }

    public HashSet<CodeError> generationErrors = new HashSet<>();
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

        for(Annotation annotation : annotationList){
            annotation.generateBytecode(classWriter);
        }

        checkErrorsOrThrow();

        // method generation
        for(KoperMethod method : methods) {
            method.generateBytecode(classWriter);
        }

        checkErrorsOrThrow();

        classWriter.visit(Opcodes.V11, access, name, null, superClass, interfaces.toArray(new String[0]));
        for(KoperField field : fields) {

            field.generateBytecode(classWriter);
            if(field.isStatic() && field.isInitialised() && field.isComplexInitialisation()) {
                if(staticConstructor == null) {
                    willHaveStaticConstructor();
                }
            }

            if(field.hasGetter) {
                generateGetter(classWriter, field, field.isStatic());
            }

            if(field.hasSetter) {
                generateSetter(classWriter, field, field.isStatic());
            }
        }

        if(constructors.isEmpty()) {
            generateDefaultConstructor(classWriter);
        }

        checkErrorsOrThrow();

        if(staticConstructor != null) {
            staticConstructor.generateBytecode(classWriter);
        }

        checkErrorsOrThrow();

        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
    // Returns true if class isn't enum and interface
    public boolean isFullClass() {
        return classType == ClassType.CLASS;
    }

    private void checkErrorsOrThrow() throws CompilationException {
        if(!generationErrors.isEmpty()) {
            throw new CompilationException(generationErrors);
        }
    }

    private void generateDefaultConstructor(ClassWriter classWriter) {
        MethodVisitor visitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "()V", null, new String[0]);
        visitor.visitVarInsn(Opcodes.ALOAD, 0);
        visitor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", true);
        for(KoperField field : toObjectInit) {
            visitor.visitVarInsn(Opcodes.ALOAD, 0);
            field.initializer.generateBytecode(, visitor);
            visitor.visitFieldInsn(PUTFIELD, name, field.getName(), field.getType().toDescriptor());
        }
        visitor.visitInsn(RETURN);
        visitor.visitEnd();
    }

    public String getClassByName(String name) {
        if(name.contains(".")) return toJvmName(name);

        return imports.get(name);
    }

    public void willHaveStaticConstructor() {
        this.staticConstructor = new StaticConstructor(this);
    }

    private void generateGetter(ClassWriter writer, KoperField field, boolean isStatic) {
        MethodVisitor getter = writer.visitMethod(ACC_PUBLIC, "get" + StringUtils.capitalize(field.getName()), "()" + field.getType().toDescriptor(), null, new String[0]);
        Label body = new Label();
        getter.visitLabel(body);
        int instruction;
        if(isStatic) {
            instruction = GETSTATIC;
        } else {
            getter.visitVarInsn(ALOAD, 0);
            instruction = GETFIELD;
        }

        getter.visitFieldInsn(instruction, name, field.getName(), field.getType().toDescriptor());

        getter.visitInsn(ARETURN);
        Label meta = new Label();
        getter.visitLabel(meta);
        getter.visitLocalVariable("this", "L" + name + ";", null, body, meta, 0);
        getter.visitEnd();
    }

    private void generateSetter(ClassWriter writer, KoperField field, boolean isStatic) {
        MethodVisitor setter = writer.visitMethod(ACC_PUBLIC, "set" + StringUtils.capitalize(field.getName()), "(" + field.getType().toDescriptor() + ")V", null, new String[0]);
        Label body = new Label();
        setter.visitLabel(body);
        int instruction;
        if(isStatic) {
            instruction = PUTSTATIC;
        } else {
            setter.visitVarInsn(ALOAD, 0);
            instruction = PUTFIELD;
        }

        setter.visitVarInsn(ALOAD, 1);
        setter.visitFieldInsn(instruction, name, field.getName(), field.getType().toDescriptor());
        setter.visitInsn(RETURN);
        Label meta = new Label();
        setter.visitLabel(meta);
        setter.visitLocalVariable("this", "L" + name + ";", null, body, meta, 0);
        setter.visitLocalVariable(field.getName(), field.getType().toDescriptor(), null, body, meta, 1);
        setter.visitEnd();
    }
}