package pw.koper.lang.lexer;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;

import java.util.HashSet;
import java.util.LinkedList;

public class Lexer {
    private static final char[] skip = {
      '\r', '\n', '\t', 8, 9, 11, 12, 32, ' ', '\0'
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

    private void add() {
        position++;
        column++;
    }
    private final LinkedList<Token> tokens = new LinkedList<>();

    public LinkedList<Token> lex() throws CompilationException {

        while(true) {
            Token next = next();
            if(next == null) {
                break;
            }
            if(next.kind.equals(TokenKind.EOF)) {
                System.out.println("EOF");
                return tokens;
            } else if(next.kind.equals(TokenKind.UNKNOWN)) {
                errors.add(new CodeError("Unexpected token", next.start, next.end));
            }
            tokens.add(next);
        }
        return tokens;
    }

    private char getAt(int pos) {
        return input.charAt(pos);
    }

    private String getAt(int start, int end) {
        return input.substring(start, end);
    }

    private char peek() {
        if((input.length() - 1) == position) {
            return '\0';
        }
        return input.charAt(position);
    }

    private Token next() {
        if(spaces()) {
            System.out.println("EOF");
            return atom(TokenKind.EOF);
        }
        char current = peek();
        if((input.length() - 1) == position) {
            return atom(TokenKind.EOF);
        }

        char possibleQuote = isQuote(current);
        if(possibleQuote != '\0') {
            add();
            return string(possibleQuote);
        } else if(isIdentifierChar(current)) {
            return identifier();
        } else {
            return somethingElse();
        }

//        return atom(TokenKind.UNKNOWN);
    }

    private Token somethingElse() {
        int start = position;
        if(spaces()) {
            return atom(TokenKind.EOF);
        }
        System.out.println("Finished");
        String literal = getAt(start, position);
        TokenKind result = TokenKind.getByLiteral(literal);
        if(result.equals(TokenKind.UNKNOWN)) {
            errors.add(new CodeError("Unexpected EOF"));
            return atom(TokenKind.EOF);
        } else {
            return new Token(result, result.literal, line, column, start);
        }
    }

    private boolean spaces() {
        while(isSpace(peek())) {
            if(peek() == '\0') {
                return true;
            }
            add();
        }
        return false;
    }

    private Token identifier() {
        int start = position;
        char first = getAt(start);
        boolean annotation = false;
        if(first == '@') {
            annotation = true;
            start++;
            add();
        }
        while(!isSpace(peek())) add();
        String literal = getAt(start, position);
        TokenKind result = TokenKind.getByLiteral(literal);

        if(annotation && !result.equals(TokenKind.UNKNOWN)) {
            errors.add(new CodeError("Annotation can't have keyword name", start, position));
            return null;
        } else if(annotation) {
            return new Token(TokenKind.ANNOTATION, literal, line, column, start);
        } else if(!result.equals(TokenKind.UNKNOWN)) {
            return new Token(result, result.literal, line, column, start);
        } else {
            return new Token(TokenKind.NAME, literal, line, column, start);
        }
    }

    private Token string(char possibleQuote) {
        int start = ++position;
        while(peek() != possibleQuote) {
            if((input.length() - 1 ) >= position) {
                errors.add(new CodeError("Unfinished string", start, position));
                return null;
            }
            add();
        }
        String string = input.substring(start, position);
        return new Token(TokenKind.STRING, string, line, column, position);
    }

    private Token atom(TokenKind kind) {
        return new Token(kind, line, column, position + 1, 1);
    }

    private boolean isSpace(char possible) {
        if(possible == '\n') {
            line++;
            column = 1;
            return false;
        } else if(possible == '\0') {
            return true;
        }
        for(char skipping : skip) {
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
