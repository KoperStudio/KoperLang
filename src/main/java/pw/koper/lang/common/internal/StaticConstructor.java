package pw.koper.lang.common.internal;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;

import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class StaticConstructor extends KoperConstructor {
    public StaticConstructor(KoperClass forClass) {
        super(forClass, PrimitiveTypes.VOID, AccessModifier.PUBLIC, true);
    }

    @Override
    public void generateBytecode(ClassWriter classWriter) {
        if(this.forClass.fields.stream().anyMatch(field -> field.isStatic() && field.isInitialised())) {
            MethodVisitor staticConstructor = classWriter.visitMethod(ACC_STATIC, getName(), "()V", null, new String[0]);
            this.forClass.fields
                    .stream()
                    .filter(field -> field.isStatic() && field.isInitialised() && field.isComplexInitialisation())
                    .forEach(field -> field.initializer.generateBytecode(this, staticConstructor));
            staticConstructor.visitEnd();
        }
    }
}
