package pw.koper.lang.lexer;

public class Token {
    public TokenKind kind;
    public String literal;
    public int lineNumber;
    public int column;
    public int position;
    public int tokenIndex;

    public Token(TokenKind kind, String literal, int lineNumber, int column, int position, int tokenIndex) {
        this.kind = kind;
        this.literal = literal;
        this.lineNumber = lineNumber;
        this.column = column;
        this.position = position;
        this.tokenIndex = tokenIndex;
    }

    public int length() {
        return literal.length();
    }
}
