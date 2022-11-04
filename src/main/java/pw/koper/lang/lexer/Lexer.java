package pw.koper.lang.lexer;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Pattern;

public class Lexer extends CompilationStage<LinkedList<Token>> {
    private static final char[] skip = {
      '\r', '\n', '\t', 8, 9, 11, 12, 32, ' ', '\0'
    };

    private final String input;

    public Lexer(KoperCompiler compiler, String code) {
        this.fileName = compiler.getCompilingFile().getName();
        this.input = code
//                .replace(";", "\n")
        ;
        compiler.setInput(input);
    }

//    public String getRawInput() {
//        return rawInput;
//    }

    private void add() {
        position++;
        column++;
        switch (peek()) {
            case '\n' -> {
                line++;
                column = 1;
            }
            case ';' -> add();
        }
    }
    private final LinkedList<Token> tokens = new LinkedList<>();

    @Override
    public LinkedList<Token> proceed() throws CompilationException {
        while(true) {
            Token next = next();
            if(next == null) {
                break;
            }
            if(next.kind.equals(TokenKind.EOF)) {
                tokens.addLast(atom(TokenKind.RIGHT_CURLY_BRACE));
                tokens.add(atom(TokenKind.EOF));
                return tokens;
            } else if(next.kind.equals(TokenKind.UNKNOWN)) {
                error("Unexpected token", next.start);
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

        switch (current){
            case '"' -> {
                return string('"');
            }
            case '\'' -> {
                return string('\'');
            }
            case '@' -> {
                return identifier();
            }
            case ',' -> {
                return atom(TokenKind.COMMA);
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
                if (isIdentifierChar(current)) {
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
            if(isDelimiter(peek())) {
                break;
            }
            add();

            if(peek() == '.') {
                if(gotPeriod) {
                    error("Invalid number literal", start);
                    return null;
                }
                gotPeriod = true;
            }
            if(peek() == 'x') {
                if(gotX) {
                    error("Invalid hexadecimal number literal", start);
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
                error("Invalid number literal", start);
                return null;
            }
        } else {
            try {
                Long.parseLong(literal);
                return new Token(TokenKind.NUMBER, literal, line, column, start);
            } catch (Exception ignored) {}
        }
        error("Invalid number literal", start);
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
        String literal = getAt(start, position);
        TokenKind result = TokenKind.getByLiteral(literal);
        if(result.equals(TokenKind.UNKNOWN)) {
            errors.add(new CodeError(this, "Unexpected EOF"));
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
            error("Annotation can't have a keyword name", position + 1, start);
            return null;
        } else if(annotation) {
            return new Token(TokenKind.ANNOTATION, literal, line, column, start);
        } else if(!result.equals(TokenKind.UNKNOWN)) {
            return new Token(result, result.literal, line, column, start);
        } else {
            for(String literalPart : literal.split("\\.")) {
                TokenKind runResult = TokenKind.getByLiteral(literalPart);
                if(!runResult.equals(TokenKind.UNKNOWN)) {
                    error("Name can't contain keyword parts", position, start);
                    return null;
                }
            }
            return new Token(TokenKind.NAME, literal, line, column, start);
        }
    }

    private Token string(char possibleQuote) {
        add();
        int start = position;
        boolean escape = false;
        while (true) {
            char current = peek();
            if (current == '\\' && !escape) {
                escape = true;
                add();
                continue;
            }
            if (escape) {
//                add();
                switch (current) {
                    case '\\', 't', 'b', 'n', 'r', 'f', '\'', '"' -> {
                        add();
                        escape = false;
                        continue;
                    }
                    default -> {
                        error("Invalid escape string", position, position +1);
                        return null;
                    }
                }
            }
            if (current == possibleQuote) break;
            if ((input.length() - 1) <= position) {
                error("Unfinished string", start);
                return null;
            }
            add();
        }
        String string = StringEscapeUtils.unescapeJava(input.substring(start, position));
        if(string.length() > 1 && possibleQuote == '\'') {
            error("Character quote is longer than 1");
            return null;
        }
        add();
        return new Token(TokenKind.STRING, string, line, column, position);
    }

    private Token atom(TokenKind kind) {
        return new Token(kind, line, column, ++position, 1);
    }

    private void error(String label, int start, int end){
        int startOfLine;
        int lineColumn = end;
        while(lineColumn >= 0 && input.charAt(lineColumn) != '\n'){
            lineColumn--;
        }
        lineColumn++;
        startOfLine = lineColumn;
        StringBuilder builder = new StringBuilder();
        try {
            while(input.charAt(lineColumn) != '\n'){
                builder.append(input.charAt(lineColumn));
                lineColumn++;
            }
        } catch (StringIndexOutOfBoundsException exception) {
            startOfLine = start;
        }


        int toStart = start - startOfLine;
        if(column == 0) {
            column += start - end;
            toStart = start - lineColumn;
        }
        CodeError error = new CodeError(this, label, builder.toString(), toStart, column);
        errors.add(error);
    }

    private void error(String label, int start){
        error(label, start, position);
    }

    private void error(String label){
        CodeError error = new CodeError(this, label);
        errors.add(error);
    }

    private boolean isSpace(char possible) {
        if(possible == '\n') {
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

    private boolean isDelimiter(char current) {
        return switch (current) {
            default -> false;
            case ',', ';' -> true;
        };
    }
}
