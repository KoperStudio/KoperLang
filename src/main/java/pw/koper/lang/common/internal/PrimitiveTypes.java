package pw.koper.lang.common.internal;

import lombok.Singular;

public class PrimitiveTypes {
    private static class ByteType extends Type {
        @Override
        public String getDescriptor() {
            return "B";
        }
    }

    private static class ShortType extends Type {

        @Override
        public String getDescriptor() {
            return "S";
        }
    }

    private static class CharType extends Type {

        @Override
        public String getDescriptor() {
            return "C";
        }
    }

    private static class IntType extends Type {

        @Override
        public String getDescriptor() {
            return "I";
        }
    }

    private static class LongType extends Type {

        @Override
        public String getDescriptor() {
            return "J";
        }
    }

    private static class FloatType extends Type {

        @Override
        public String getDescriptor() {
            return "F";
        }
    }

    private static class DoubleType extends Type {

        @Override
        public String getDescriptor() {
            return "D";
        }
    }

    private static class BooleanType extends Type {

        @Override
        public String getDescriptor() {
            return "Z";
        }
    }

    private static class VoidType extends Type {

        @Override
        public String getDescriptor() {
            return "V";
        }
    }

    public static final Type BYTE = new ByteType();
    public static final Type SHORT = new ShortType();
    public static final Type CHAR = new CharType();
    public static final Type INT = new IntType();
    public static final Type LONG = new LongType();
    public static final Type FLOAT = new FloatType();
    public static final Type DOUBLE = new DoubleType();
    public static final Type BOOLEAN = new BooleanType();
    public static final Type VOID = new VoidType();
}
