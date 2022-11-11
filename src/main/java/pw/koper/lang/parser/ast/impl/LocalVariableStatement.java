package pw.koper.lang.parser.ast.impl;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.internal.KoperMethod;
import pw.koper.lang.parser.ast.Node;

public class LocalVariableStatement extends MethodStatement {

    public Node assignment;
    public final int varId;
    public LocalVariableStatement(int lineNumber, KoperMethod forMethod) {
        super(lineNumber, forMethod);
        this.varId = forMethod.methodCounter.next();
    }

    @Override
    public void generateBytecode(MethodVisitor visitor) {
        visitor.visitVarInsn(Opcodes.ALOAD, varId);
    }
}
