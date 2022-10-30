package pw.koper.lang.parser;

import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.parser.ast.Node;

import java.util.LinkedList;
import java.util.List;

public class Parser extends CompilationStage<LinkedList<Node>> {

    private final KoperCompiler compiler;

    public Parser(KoperCompiler compiler) {
        this.compiler = compiler;
    }

    @Override
    public LinkedList<Node> proceed() throws CompilationException {
        return null;
    }
}
