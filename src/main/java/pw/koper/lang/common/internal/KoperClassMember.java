package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

@Getter
@Setter
@AllArgsConstructor
public class KoperClassMember {
    protected final KoperClass forClass;
    private Type type;
    private String name;
    private AccessModifier accessModifier;
    private boolean isStatic;
    private boolean isFinal;

    public void generateBytecode(ClassWriter classWriter) {}

    public int getOpcodeAccessModifier() {
        int base = getAccessModifier().toOpcode();
        if(isStatic()) {
            base |= Opcodes.ACC_STATIC;
        }
        if(isFinal()) {
            base |= Opcodes.ACC_FINAL;
        }

        return base;
    }
}
