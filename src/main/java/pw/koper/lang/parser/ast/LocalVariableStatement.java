package pw.koper.lang.parser.ast;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.internal.KoperMethod;

public class LocalVariableStatement extends Node {

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
