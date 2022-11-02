package pw.koper.lang.common;

import pw.koper.lang.lexer.Token;

public class CodeError {
    private final CompilationStage compilationStage;
    public int start, end, lineNumber;
    public String label, line;
    public CodeError(CompilationStage<?> stage, String label, String line, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.line = line;
        this.compilationStage = stage;
        this.lineNumber = compilationStage.line;
    }

    public CodeError(CompilationStage<?> stage, String error) {
        this.start = -1;
        this.end = -1;
        this.label = error;
        this.compilationStage = stage;
    }

    public CodeError(KoperCompiler compiler, String error, Token on) {
        this(compiler.getStage(), error, compiler.getLineByNumber(on.lineNumber), on.start, on.end);
    }

    public String render() {
        return "Error: " +
                label + "\n" +
                lineNumber + " " + line + "\n" +
                " ".repeat(start + String.valueOf(lineNumber).length() + 1) +
                "^".repeat(end - start) + "\n" +
                "File: " + compilationStage.fileName;
    }
}
