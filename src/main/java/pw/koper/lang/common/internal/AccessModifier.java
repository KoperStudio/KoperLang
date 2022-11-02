package pw.koper.lang.common.internal;

import pw.koper.lang.lexer.Token;

public enum AccessModifier {
    PUBLIC, PROTECTED, PRIVATE, UNKNOWN;

    public static AccessModifier fromToken(Token token) {
        return switch (token.kind) {
            case KEY_PUBLIC -> PUBLIC;
            case KEY_PROTECTED -> PROTECTED;
            case KEY_PRIVATE -> PRIVATE;
            default -> UNKNOWN;
        };
    }
}
