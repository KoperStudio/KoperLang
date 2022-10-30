package pw.koper.lang.parser.ast;

public interface Node {
    default Object generateBytecode() {
        return null;
    }
}
