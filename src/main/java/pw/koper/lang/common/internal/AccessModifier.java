package pw.koper.lang.common.internal;

import org.objectweb.asm.Opcodes;
import pw.koper.lang.lexer.Token;

import static pw.koper.lang.lexer.TokenKind.KEY_PRIVATE;

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

    public int toOpcode() {
        return switch (this) {
            case PUBLIC, UNKNOWN -> Opcodes.ACC_PUBLIC;
            case PROTECTED -> Opcodes.ACC_PROTECTED;
            case PRIVATE -> Opcodes.ACC_PRIVATE;
        };
    }
}
