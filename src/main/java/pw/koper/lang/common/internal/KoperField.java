package pw.koper.lang.common.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;

@RequiredArgsConstructor
@Getter
public class KoperField {
    private final Type type;
    private final String name;
    private final AccessModifier accessModifier;
    public boolean hasSetter = false;
    public boolean hasGetter = false;

    public void generateBytecode(ClassWriter classWriter) {
        classWriter.visitField(accessModifier.toOpcode(), name, type.toDescriptor(), type.toSignature(), null);
    }
}
