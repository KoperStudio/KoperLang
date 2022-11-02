package pw.koper.lang.lexer;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.common.internal.ClassType;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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

    public boolean isClassDeclarationStart() {
        return switch (kind) {
            case KEY_PUBLIC, KEY_STATIC, KEY_CLASS, KEY_INTERFACE, KEY_ENUM, KEY_ABSTRACT, KEY_DATA, KEY_PRIVATE -> true;
            default -> false;
        };
    }

    public ClassType toClassType() {
        return switch (kind) {
            case KEY_CLASS -> ClassType.CLASS;
            case KEY_ENUM -> ClassType.ENUM;
            case KEY_INTERFACE -> ClassType.INTERFACE;
            default -> ClassType.INVALID;
        };
    }

    private static final Pattern stringPattern = Pattern.compile("^[A-Za-z][A-Za-z-0-9]*$");

    public boolean isStrict() {
        return stringPattern.matcher(literal).matches();
    }

    public boolean is(TokenKind kind) {
        return this.kind == kind || kind.is(TokenKind.EOF);
    }

    public boolean isOrEof(TokenKind kind, KoperCompiler compiler) {
        if(this.kind == TokenKind.EOF) {
            compiler.getStage().errors.add(new CodeError(compiler, "", this));
            return true;
        }
        return this.kind == kind;
    }

    public int length() {
        return literal.length();
    }
}
