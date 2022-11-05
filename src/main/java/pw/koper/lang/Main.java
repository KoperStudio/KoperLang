package pw.koper.lang;

import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.parser.ast.Node;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main extends CompilationStage<Boolean> implements Node {
    private static final String testPath = "./TestClass.koper";
    public static final boolean DEBUG = false;
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        new KoperCompiler(new File(testPath)).compile();
        System.out.println("Done compilation in " + (System.currentTimeMillis() - start) + "ms");
    }

    @Override
    public Boolean proceed() throws CompilationException {
        return null;
    }
}