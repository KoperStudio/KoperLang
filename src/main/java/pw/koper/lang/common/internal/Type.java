package pw.koper.lang.common.internal;

import lombok.Getter;
import lombok.Setter;
import org.objectweb.asm.Opcodes;

public abstract class Type {

    public abstract String getDescriptor();

    public String toDescriptor() {
        return "[".repeat(nestedArraysCount) + getDescriptor();
    }

    // sometimes we don't need signatures and null value can be passed
    public String toSignature() {
        return null;
    }

    @Getter @Setter
    private int nestedArraysCount = 0;

    public String getInstructionPrefix() {
        return "I"; // integer
    }

    public Object nullValue() {
        return 0;
    }
}
