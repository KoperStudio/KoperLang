package pw.koper.lang.common;

import org.apache.commons.lang3.tuple.Pair;
import pw.koper.lang.common.internal.KoperClass;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.util.StringUtil;

public class CodeError {
    private final String fileName;
    public int start, end, lineNumber;
    public String label, line;
    public CodeError(CompilationStage<?> stage, String label, String line, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.line = line;
        this.fileName = stage.fileName;
        this.lineNumber = stage.line;
    }

    public CodeError(CompilationStage<?> stage, String error) {
        this.start = -1;
        this.end = -1;
        this.label = error;
        this.fileName = stage.fileName;
        this.lineNumber = stage.line;
    }

    public CodeError(KoperClass koperClass, String error, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = error;
        this.fileName = koperClass.compiler.getCompilingFile().getName();
        Pair<Integer, String> lineData = StringUtil.getLineDataFromSubstring(koperClass.compiler.getInput(), start, end);
        if(lineData == null) {
            return;
        }
        this.lineNumber = lineData.getLeft();
        this.line = lineData.getRight();
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
                "File: " +fileName;
    }
}
