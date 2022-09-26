package pw.koper.lang.lexer;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

public class Lexer {
    private static final char[] skip = {
      '\r', '\n', '\t', 8, 9, 11, 12, 32, ' '
    };
    private int line = 1;
    private int column = 1;
    private TokenKind currentKind;
    private final HashSet<CodeError> errors = new HashSet<>(1);
    private int position = 0;
    private final String input;
    public Lexer(String code) {
//        input.append(code);
        this.input = code;
    }

//    public String getRawInput() {
//        return rawInput;
//    }
    private final LinkedList<Token> tokens = new LinkedList<>();

    public LinkedList<Token> lex() throws CompilationException {
        Token next;
        while(!(next = next()).kind.equals(TokenKind.EOF)) {
            tokens.add(next);
        }
        return tokens;
    }

    private char peek() {
        return input.charAt(position);
    }

    private Token next() {
        while(needsToBeSkipped(peek())) position++;
        char current = peek();
        if((input.length() - 1) == position) {
            return atom(TokenKind.EOF);
        }

        char possibleQuote = isQuote(current);
        if(possibleQuote != '\0') {
            position++;
            return string(possibleQuote);
        }
    }

    private Token identifier() {
        int start = ++position;

    }

    private Token string(char possibleQuote) {
        int start = ++position;
        while(peek() != possibleQuote) {
            if((input.length() - 1 ) >= position) {
                errors.add(new CodeError("Unfinished string", start, start + position));
                return null;
            }
            position++;
        }
        int end = start + position;
        String string = input.substring(start, end);
        return new Token(TokenKind.STRING, string, line, start, end);
    }

    private Token atom(TokenKind kind) {
        return new Token(kind, line, column, position + 1, 1);
    }

    private boolean needsToBeSkipped(char possible) {
        for(char skipping : skip) {
            if(skipping == '\n') {
                line++;
                column = 1;
                return true;
            }
            if(possible == skipping) {
                return true;
            }
        }
        return false;
    }
    private char isQuote(char current) {
        if(current == '\'') {
            return current;
        } else if(current == '"') {
            return current;
        }
        return '\0';
    }
    private boolean isIdentifierChar(char current) {
        return switch (current) {
            default -> false;
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z' ->
                    true;
        };
    }

    private boolean isNumber(char current) {
        return switch (current) {
            default -> false;
            case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0' ->
                    true;
        };
    }
}
