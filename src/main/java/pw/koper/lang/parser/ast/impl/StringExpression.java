package pw.koper.lang.parser.ast.impl;

import org.objectweb.asm.MethodVisitor;
import pw.koper.lang.common.internal.KoperMethod;

public class StringExpression extends NonComplexStatement {
    public StringExpression(String literal) {
        super(literal);
    }
    @Override
    public void generateBytecode(KoperMethod context, MethodVisitor visitor) {
        visitor.visitLdcInsn(getValue()); // putting string value on the stack
    }
}