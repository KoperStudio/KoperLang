package pw.koper.lang.gen;

import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.internal.Type;

import java.lang.reflect.Field;

public class Instructions {
    public static int push(Type type) {
        for(Field opcode : Opcodes.class.getFields()) {
            if(opcode.getName().equals(type.getInstructionPrefix() + "PUSH")) {
                try {
                    return (int) opcode.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return -1;
    }

    public static int getReturn(Type type) {
        for(Field opcode : Opcodes.class.getFields()) {
            if(opcode.getName().equals(type.getInstructionPrefix() + "RETURN")) {
                try {
                    return (int) opcode.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return -1;
    }
}
