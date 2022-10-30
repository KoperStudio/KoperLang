package pw.koper.lang.common;

public class CodeError {
    private final Cursor cursor;
    public int start, end, lineNumber;
    public String label, line;
    public CodeError(Cursor stage, String label, String line, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.line = line;
        this.cursor = stage;
        this.lineNumber = cursor.line;
    }

    public CodeError(Cursor stage, String error) {
        this.start = -1;
        this.end = -1;
        this.label = error;
        this.cursor = stage;
    }

    public String render() {
        return "Error: " +
                label + " in " + cursor.fileName + "\n" +
                lineNumber + " " + line + "\n" +
                " ".repeat(start + String.valueOf(lineNumber).length() + 1) +
                "^".repeat(end - start);
    }
}
