package pw.koper.lang.common.internal;

import lombok.Singular;
import org.objectweb.asm.Opcodes;

public class PrimitiveTypes {
    public static class ByteType extends Type {
        @Override
        public String getDescriptor() {
            return "B";
        }
    }

    public static class ShortType extends Type {
        @Override
        public String getDescriptor() {
            return "S";
        }
    }

    public static class CharType extends Type {

        @Override
        public String getDescriptor() {
            return "C";
        }
    }

    public static class IntType extends Type {

        @Override
        public String getDescriptor() {
            return "I";
        }
    }

    public static class LongType extends Type {

        @Override
        public String getDescriptor() {
            return "J";
        }

        @Override
        public String getInstructionPrefix() {
            return "L";
        }
    }

    public static class FloatType extends Type {

        @Override
        public String getDescriptor() {
            return "F";
        }

        @Override
        public String getInstructionPrefix() {
            return "F";
        }
    }

    public static class DoubleType extends Type {

        @Override
        public String getDescriptor() {
            return "D";
        }
        @Override
        public String getInstructionPrefix() {
            return "D";
        }
    }

    public static class BooleanType extends Type {

        @Override
        public String getDescriptor() {
            return "Z";
        }
    }

    public static class VoidType extends Type {

        @Override
        public String getDescriptor() {
            return "V";
        }

        @Override
        public String getInstructionPrefix() {
            return "";
        }
    }
    public static final Type VOID = new VoidType();
}
