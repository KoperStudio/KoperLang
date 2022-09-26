package pw.koper.lang.lexer;

import java.util.regex.Pattern;

public enum TokenKind {
    UNKNOWN("UNKNOWN"),
    EOF("EOF"),
    NAME("name"),
    NUMBER("number"),
    STRING("string"),
    CHAR("char"),
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    MOD("%"),
    XOR("^"),
    PIPE("|"),
    INCREMENT("++"),
    DECREMENT("--"),
    LOGICAL_OR("||"),
    LOGICAL_AND("&&"),
    NOT("!"),
    BIT_NOT("~"),
    QUESTION("?"),
    COMMA(","),
    SEMICOLON(";"),
    COLON(":"),
    RIGHT_ARROW("->"),
    LEFT_ARROW("<-"),
    AMPLIFIER("&"),
    HASH("#"),
    AT("@"),
    STRING_DOLLAR("$"),
    LEFT_SHIFT("<<"),
    RIGHT_SHIFT(">>"),
    UNSIGNED_RIGHT_SHIFT(">>>"),
    NOT_IN("!in"),
    NOT_IS("!is"),
    ASSIGN("="),
    PLUS_ASSIGN("+="),
    MINUS_ASSIGN("-="),
    DIV_ASSIGN("/="),
    MULTIPLY_ASSIGN("*="),
    LEFT_CURLY_BRACE("{"),
    RIGHT_CURLY_BRACE("}"),
    RIGHT_BRACE("]"),
    LEFT_BRACE("["),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    EQUALS("=="),
    NOT_EQUALS("!="),
    MORE(">"),
    LESS("<"),
    MORE_EQUALS(">="),
    LESS_EQUALS("<="),
    COMMENT("//"),
    DOT("."),
    DOUBLE_DOT(".."),
    ELLIPSIS("..."),
    KEY_IF("if"),
    KEY_ELSE("else"),
    KEY_FOR("for"),
    KEY_WHILE("while"),
    KEY_CONTINUE("continue"),
    KEY_BREAK("break"),
    KEY_TRUE("true"),
    KEY_FALSE("false"),
    KEY_COST("const"),
    KEY_IMPORT("import"),
    KEY_IN("in"),
    KEY_AS("as"),
    KEY_MATCH("match"),
    KEY_MUT("mut"),
    KEY_NULL("null"),
    KEY_RETURN("return"),
    KEY_PUBLIC("public"),
    KEY_PROTECTED("protected"),
    KEY_PRIVATE("private"),
    KEY_STATIC("static"),
    KEY_CLASS("class"),
    KEY_INTERFACE("interface"),
    KEY_ENUM("enum"),
    KEY_ABSTRACT("abscract"),
    KEY_VOLATILE("volatile"),
    KEY_SYNC("synchronous"),
    KEY_RUN("run"),
    KEY_RUNASYNC("runasync");
    public final int length;
    public final String literal;
    TokenKind(String literal) {
        this.literal = literal;
        this.length = literal.length();
    }

    public static TokenKind getByLiteral(String literal) {
        String translated = literal.trim();
        for(TokenKind current : values()) {
            if(current.literal.equals(translated)) {
                return current;
            }
        }
        return UNKNOWN;
    }

}