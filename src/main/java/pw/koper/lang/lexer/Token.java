package pw.koper.lang.lexer;

public class Token {
    public TokenKind kind;
    public String literal;
    public int lineNumber;
    public int column;
    public int start;
    public int end;

    public Token(TokenKind kind, String literal, int lineNumber, int column, int start) {
        this.kind = kind;
        this.literal = literal;
        this.lineNumber = lineNumber;
        this.column = column;
        this.start = start;
        this.end = start + literal.length();
    }

    public Token(TokenKind kind, int lineNumber, int column, int start, int len) {
        this.kind = kind;
        this.literal = kind.literal;
        this.lineNumber = lineNumber;
        this.column = column;
        this.start = start;
        this.end = start + len;
    }

    public int length() {
        return literal.length();
    }
}
