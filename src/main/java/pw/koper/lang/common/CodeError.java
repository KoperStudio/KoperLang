package pw.koper.lang.common;

public class CodeError {
    public int start, end;
    public String error;

    public CodeError(String error, int start, int end) {
        this.start = start;
        this.end = end;
        this.error = error;
    }

    public CodeError(String error) {
        this.start = -1;
        this.end = -1;
        this.error = error;
    }

    public String render() {
        return error;
    }
}
