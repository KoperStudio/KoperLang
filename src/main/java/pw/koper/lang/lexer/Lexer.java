package pw.koper.lang.lexer;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

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
        this.input = code.replace(";", "\n");
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
                tokens.add(atom(TokenKind.EOF));
                return tokens;
            } else if(next.kind.equals(TokenKind.UNKNOWN)) {
                errors.add(new CodeError("Unexpected token", next.start, next.end));
            }
            tokens.addLast(next);
        }
        if(!errors.isEmpty()) {
            throw new CompilationException(errors);
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
        try {
            return input.charAt(position);
        } catch (Exception exception) {
            return '\0';
        }
    }

    private Token next() {
        if(spaces()) {
            return atom(TokenKind.EOF);
        }
        char current = peek();
        if((input.length() - 1) == position) {
            return atom(TokenKind.EOF);
        }

        char possibleQuote = isQuote(current);
        switch (current){
            case '@' -> {
                return identifier();
            }
            case '{' -> {
                return atom(TokenKind.LEFT_CURLY_BRACE);
            }
            case '}' -> {
                return atom(TokenKind.RIGHT_CURLY_BRACE);
            }
            case '[' -> {
                return atom(TokenKind.LEFT_BRACE);
            }
            case ']' -> {
                return atom(TokenKind.RIGHT_BRACE);
            }
            case '(' -> {
                return atom(TokenKind.LEFT_PAREN);
            }
            case ')' -> {
                return atom(TokenKind.RIGHT_PAREN);
            }
            default -> {
                if (possibleQuote != '\0') {
                    add();
                    return string(possibleQuote);
                } else if (isIdentifierChar(current)) {
                    return identifier();
                } else if (isNumber(current)) {
                    return number();
                } else if (isAssignments(current)) {
                    return assignment();
                } else {
                    return somethingElse();
                }
            }
        }
    }

    private Token assignment() {
        add();
        return atom(TokenKind.ASSIGN);
    }

    private Token number() {
        int start = position;
        boolean gotPeriod = false;
        boolean gotX = false;
        while(!isSpace(peek())) {
            add();
            if(peek() == '.') {
                if(gotPeriod) {
                    errors.add(new CodeError("Invalid number literal", start, position));
                    return null;
                }
                gotPeriod = true;
            }
            if(peek() == 'x') {
                if(gotX) {
                    errors.add(new CodeError("Invalid hexadecimal number literal", start, position));
                    return null;
                }
                gotX = true;
            }
        }
        String literal = getAt(start, position);
        if(!gotX) {
            if(isNumeric(literal)) {
                return new Token(TokenKind.NUMBER, literal, line, column, start);
            } else {
                errors.add(new CodeError("Invalid number literal", start, position));
                return null;
            }
        } else {
            try {
                Long.parseLong(literal);
                return new Token(TokenKind.NUMBER, literal, line, column, start);
            } catch (Exception ignored) {}
        }
        errors.add(new CodeError("Invalid number literal", start, position));
        return null;
    }

    private final Pattern pattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    public boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        return pattern.matcher(strNum).matches();
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
        while(isIdentifierChar(peek())) add();
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
        if(string.length() > 1 && possibleQuote == '\'') {
            errors.add(new CodeError("Character quote is longer than 1"));
            return null;
        }
        return new Token(TokenKind.STRING, string, line, column, position);
    }

    private Token atom(TokenKind kind) {
        return new Token(kind, line, column, ++position, 1);
    }

    private boolean isSpace(char possible) {
        if(possible == '\n' || possible == '\r') {
            line++;
            column = 0;
            return true;
        }
//        else if(possible == '\0') {
//            return true;
//        }
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
        if(isSpace(current)) return false;
        return switch (current) {
            default -> false;
            case 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '_', '.' ->
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

    private boolean isAssignments(char current) {
        return switch (current) {
            default -> false;
            case '*', '/', '+', '-', '=' ->
                    true;
        };
    }

    private boolean canContinueNumber(char current) {
        return switch (current) {
            default -> false;
            case '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '_' ->
                    true;
        };
    }

}
