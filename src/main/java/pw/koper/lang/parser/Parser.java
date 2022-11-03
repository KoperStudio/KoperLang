package pw.koper.lang.parser;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.common.internal.*;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.lexer.TokenKind;
import pw.koper.lang.parser.ast.AstImpl;
import pw.koper.lang.parser.ast.Node;

import java.util.*;

import static pw.koper.lang.common.StringUtil.toJvmName;
import static pw.koper.lang.lexer.TokenKind.*;

public class Parser extends CompilationStage<KoperClass> {

    private final KoperCompiler compiler;
    private final LinkedList<Node> tree = new LinkedList<>();
    private final ListIterator<Token> tokenIterator;
    private Token currentToken;
    private final String initialFileName;
    private final KoperClass result;

    public Parser(KoperCompiler compiler) {
        this.compiler = compiler;
        this.tokenIterator = (ListIterator<Token>) compiler.getTokens().iterator();
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
        parseClassBody();
        if(errors.size() != 0) {
            throw new CompilationException(errors);
        }
        return result;
    }

    private void parseClassBody() {
        if(!nextToken().is(LEFT_CURLY_BRACE)) {
            missingToken(LEFT_CURLY_BRACE);
            return;
        }

        Token declaration = nextToken();
    }

    private void parsePrototypeDeclaration() {
        AccessModifier accessModifier = null;
        HashSet<TokenKind> metKinds = new HashSet<>();
        boolean expectMethod = false;
        boolean expectField = false;
        Modifiers modifiers = Modifiers.builder();
        while(true) {
            Token current = nextToken();
            if(metKinds.contains(current.kind)) {
                invalidToken("Already declared that modifier of the method", current);
                return;
            }
            metKinds.add(current.kind);
            AccessModifier possibleModifier = AccessModifier.fromToken(current);
            if(accessModifier != null && possibleModifier != AccessModifier.UNKNOWN) {
                invalidToken("Class visibility is already declared", current);
                return;
            }
            if(possibleModifier != AccessModifier.UNKNOWN) {
                accessModifier = possibleModifier;
            }
            switch (current.kind) {
                case KEY_GETTING -> {
                    modifiers.getting();
                    expectField = true;
                }
                case KEY_SETTING -> {
                    modifiers.setting();
                    expectField = true;
                }
                case KEY_ABSTRACT -> {
                    modifiers.abstractKey();
                    expectMethod = true;
                }
                case TYPE_VOID -> {
                    expectMethod = true;
                }
            }
            if(current.isTypeDeclaration()) {
                break;
            }
        }

        if(accessModifier == null) {
            accessModifier = AccessModifier.PUBLIC;
        }
        modifiers.access(accessModifier);
        parseTypeAndNameDeclaration(modifiers, expectField, expectMethod);
    }

    private void parseTypeAndNameDeclaration(Modifiers modifiers, boolean expectField, boolean expectMethod) {
        Type type = tokenToType(currentToken);
        if(currentToken.literal.equals(result.name)) {
            // this is a constructor of the class.
            // <init> method
            type = PrimitiveTypes.VOID;
        }

        if(type == null) {
            unexpectedToken("Invalid type: ", currentToken);
            return;
        }
        if(expectMethod && expectField) {
            invalidToken("Fields can't be absract and have void type, methods can't have 'getting' or 'setting' attribute", currentToken);
        }

    }

    private Type tokenToType(Token token) {
        return switch (token.kind) {
            case TYPE_VOID -> PrimitiveTypes.VOID;
            case TYPE_BYTE -> PrimitiveTypes.BYTE;
            case TYPE_SHORT -> PrimitiveTypes.SHORT;
            case TYPE_CHAR -> PrimitiveTypes.CHAR;
            case TYPE_INT -> PrimitiveTypes.INT;
            case TYPE_LONG -> PrimitiveTypes.LONG;
            case TYPE_FLOAT -> PrimitiveTypes.FLOAT;
            case TYPE_DOUBLE -> PrimitiveTypes.DOUBLE;
            case NAME -> new KoperObject(token.literal.replace(".\\", "/"));
            default -> null;
        };
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
                case KEY_EXTENDS -> {
                    nextToken();
                    if(!currentToken.is(NAME)) {
                        unexpectedToken("Expected extending class name, got " + currentToken.literal, currentToken);
                        return;
                    }
                    superClass = result.getClassByName(currentToken.literal);
                    continue;
                }
                case KEY_IMPLEMENTS -> {
                    System.out.println(currentToken.literal);
                    if(!parseImplementsExpression(interfaces)) {
                        return;
                    }
                    continue;
                }
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
        } while (!nextToken().isOrEof(LEFT_CURLY_BRACE, compiler, false)); // loop until '{' or EOF
        if(metNames.size() > 1) {
            Token theFirst = metNames.get(1);
            int start = theFirst.start;
            int end = metNames.get(metNames.size() - 1).end;
            errors.add(new CodeError(this, "Invalid name declaration", compiler.getLineByNumber(theFirst.lineNumber), end, start));
            return;
        }
        // Nobody declared the class type
        if(type == null) {
            notDeclared("Class type", currentToken);
            return;
        }

        // Filling first part of class information like its type and name
        Token className = metNames.get(0);
        if(!className.isStrict()) {
            invalidToken("Class name can't contain special characters and can't start with numbers", currentToken);
            return;
        }

        // Package got declared and saved to name, so we add separator
        if(!result.name.equals("")) {
            result.name += "/";
        }
        result.name += className.literal;
        result.classType = type;

        // Enums and interfaces can't be abstract
        if(type != ClassType.CLASS && result.isAbstract) {
            invalidDeclaration("Enums and interfaces can't be abstract!", result.name);
        }

        // If class name is illegal
        if(!className.literal.equals(initialFileName.replace(".koper", ""))) {
            invalidToken("Class name have to equal file name and .koper extension", currentToken);
            return;
        }

        // filling other data
        result.superClass = superClass;
        result.interfaces = interfaces;
    }

    private boolean parseImplementsExpression(Collection<String> interfaces) {
        while(true) {
            Token name = nextToken(); // interface itself
            Token comma = nextToken(); // ,
            if(!name.is(NAME)) {
                unexpectedToken("Name of implementing interface", name);
                return false;
            }
            interfaces.add(result.getClassByName(name.literal)); // just adding it, if we will have further errors, this wouldn't have impact
            if(comma.is(LEFT_CURLY_BRACE)) {
                tokenIterator.previous();
                break;
            } else if(comma.is(NAME)) {
                unexpectedToken("Splitter (comma)", comma);
                return false;
            }
        }

        return true;
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
            className = toJvmName(currentToken.literal);
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
                String fullImportName = toJvmName(currentToken.literal);
                String[] data = fullImportName.split("/");
                result.imports.put(data[data.length - 1], fullImportName);
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

    private void missingToken(TokenKind whatToken) {
        errors.add(new CodeError(this, "Missing token: " + whatToken.literal + " is missing"));
    }

    private void notDeclared(String what, Token where) {
        errors.add(new CodeError(compiler, what + " is not declared", where));
    }

    private void invalidDeclaration(String why, String className) {
        errors.add(new CodeError(this, "Invalid declaration at " + className + ": " + why));
    }
}
