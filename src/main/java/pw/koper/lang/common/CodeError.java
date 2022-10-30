package pw.koper.lang.common;

public class CodeError {
    public int start, end;
    public String label, line;

    public CodeError(String label, String line, int start, int end) {
        this.start = start;
        this.end = end;
        this.label = label;
        this.line = line;
    }

    public CodeError(String error) {
        this.start = -1;
        this.end = -1;
        this.label = error;
    }

    public String render() {
        StringBuilder builder = new StringBuilder();
        builder.append("Error: ");
        builder.append(label).append(":").append("\n");
        builder.append(line).append("\n");
        builder.append(" ".repeat(start));
        builder.append("^".repeat(end-start));
        return builder.toString();
    }
}
