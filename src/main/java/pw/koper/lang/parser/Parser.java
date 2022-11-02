package pw.koper.lang.parser;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.common.internal.ClassType;
import pw.koper.lang.common.internal.KoperClass;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.lexer.TokenKind;
import pw.koper.lang.parser.ast.AstImpl;
import pw.koper.lang.parser.ast.Node;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import static pw.koper.lang.lexer.TokenKind.*;

public class Parser extends CompilationStage<KoperClass> {

    private final KoperCompiler compiler;
    private final LinkedList<Node> tree = new LinkedList<>();
    private final Iterator<Token> tokenIterator;
    private Token currentToken;
    private final String initialFileName;
    private final KoperClass result;

    public Parser(KoperCompiler compiler) {
        this.compiler = compiler;
        this.tokenIterator = compiler.getTokens().iterator();
        this.initialFileName = compiler.getCompilingFile().getName();
        this.result = new KoperClass(initialFileName);
    }

    public Token nextToken() {
        currentToken = tokenIterator.next();
        return currentToken;
    }

    @Override
    public KoperClass proceed() throws CompilationException {

        parseHead();
        parseClassDeclaration();
        if(errors.size() != 0) {
            throw new CompilationException(errors);
        }
        return result;
    }

    private void parseClassDeclaration() {
        // Initial data
        String superClass = "java/lang/Object";
        HashSet<String> interfaces = new HashSet<>();
        ClassType type = null;
        HashSet<TokenKind> metDeclarators = new HashSet<>();
        ArrayList<Token> metNames = new ArrayList<>();

        // do loop because previous method already goes for next token for us
        do {
            // checking for the modifiers
            switch (currentToken.kind) {
                case KEY_PRIVATE -> result.isPublic = false;
                case KEY_PROTECTED -> {
                    invalidToken("Class can't have protected access level", currentToken);
                    return;
                }
                case KEY_ABSTRACT -> result.isAbstract = true;
                case KEY_STATIC ->  result.isStatic = true;
                case KEY_DATA -> result.isData = true;
            }

            // if token isn't name, then it's modifier, and we need to remember it
            // if we already have the same modifier, throw error
            if(!currentToken.is(NAME)) {
                if(metDeclarators.contains(currentToken.kind)) {
                    invalidToken("Modifier already present", currentToken);
                    return;
                }
                metDeclarators.add(currentToken.kind);
            } else {
                metNames.add(currentToken);
            }

            // maybe current token is possible class type definition
            ClassType possible = currentToken.toClassType();
            if(!possible.equals(ClassType.INVALID)) {
                if(type == null) {
                    type = possible;
                } else {
                    invalidToken("Class type already declared", currentToken);
                    return;
                }
            }
        } while (!nextToken().isOrEof(LEFT_CURLY_BRACE, compiler)); // loop until '{'

        // Nobody declared the class type
        if(type == null) {
            notDeclared("Class type", currentToken);
            return;
        }

        // Enums and interfaces can't be abstract
        if(type != ClassType.CLASS && result.isAbstract) {
            invalidToken("Enums and interfaces can't be abstract!", currentToken);
        }

        // filling all the data to the result class
        result.classType = type;
        Token className = metNames.get(0);
        if(!className.isStrict()) {
            invalidToken("Class name can't contain special characters and can't start with numbers", currentToken);
            return;
        }

        // If class name is illegal
        if(!className.literal.equals(initialFileName.replace(".koper", ""))) {
            invalidToken("Class name have to equal file name and .koper extension", currentToken);
            return;
        }

        // Package got declared and saved to name, so we add separator
        if(!result.name.equals("")) {
            result.name += "/";
        }

        // filling other data
        result.name += className.literal;
        result.superClass = superClass;
        result.interfaces = interfaces;
    }

    // Fills data as imports and package
    private void parseHead() {
        Token firstToken = nextToken();
        String className = "";
        if(firstToken.is(TokenKind.KEY_PACKAGE)) {
            nextToken();
            if(!currentToken.is(TokenKind.NAME)) {
                unexpectedToken("package name", currentToken);
                return;
            }
            className = currentToken.literal.replace("\\.", "/");
        }
        result.name = className;
        while(true) {
            Token next = nextToken();
            if(next.is(TokenKind.KEY_IMPORT)) {
                nextToken();
                if(!currentToken.is(TokenKind.NAME)) {
                    unexpectedToken("importing class name", currentToken);
                    return;
                }
                result.imports.add(currentToken.literal);
            }
            if(next.isClassDeclarationStart()) {
                return;
            }
        }
    }

    // error methods
    private void unexpectedToken(String expected, Token where) {
        errors.add(new CodeError(compiler, "Unexpected token. Expected " + expected + ", got " + where.kind, where));
    }

    private void invalidToken(String why, Token where) {
        errors.add(new CodeError(compiler, "Invalid token " + where.kind + ". " + why, where));
    }

    private void notDeclared(String what, Token where) {
        errors.add(new CodeError(compiler, what + " is not declared", where));
    }
}
