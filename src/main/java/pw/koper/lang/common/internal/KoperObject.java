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
    public Object nullValue() {
        return null;
    }
}
