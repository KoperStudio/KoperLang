package pw.koper.lang.common;

public class CodeError {
    private final CompilationStage compilationStage;
    public int start, end, lineNumber;
    public String label, line;
    public CodeError(CompilationStage stage, String label, String line, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.line = line;
        this.compilationStage = stage;
        this.lineNumber = compilationStage.line;
    }

    public CodeError(CompilationStage stage, String error) {
        this.start = -1;
        this.end = -1;
        this.label = error;
        this.compilationStage = stage;
    }

    public String render() {
        return "Error: " +
                label + " in " + compilationStage.fileName + "\n" +
                lineNumber + " " + line + "\n" +
                " ".repeat(start + String.valueOf(lineNumber).length() + 1) +
                "^".repeat(end - start);
    }
}
