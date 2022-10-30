package pw.koper.lang.gen;

import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;

public class BytecodeGenerator extends CompilationStage<byte[]> {

    public static byte[] EMPTY = new byte[0];
    public BytecodeGenerator(KoperCompiler compiler) {
        super();
    }

    @Override
    public byte[] proceed() throws CompilationException {
        return EMPTY;
    }
}
