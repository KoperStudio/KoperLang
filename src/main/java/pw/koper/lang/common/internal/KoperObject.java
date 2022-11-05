package pw.koper.lang.common.internal;

import org.objectweb.asm.Opcodes;

public class KoperObject extends Type {
    private final String name;
    public KoperObject(String name) {
        this.name = name;
    }

    @Override
    public String getDescriptor() {
        return "L" + this.name + ";";
    }

    @Override
    public int getReturnInstructionOpcode() {
        return Opcodes.ARETURN;
    }

    @Override
    public int getLoadInstructionOpcode() {
        return Opcodes.ALOAD;
    }

    @Override
    public Object nullValue() {
        return null;
    }
}
