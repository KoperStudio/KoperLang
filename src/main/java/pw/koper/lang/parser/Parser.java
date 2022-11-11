package pw.koper.lang.parser;

import pw.koper.lang.common.*;
import pw.koper.lang.common.internal.*;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.lexer.TokenKind;
import pw.koper.lang.parser.ast.impl.LocalVariableStatement;
import pw.koper.lang.parser.ast.Node;
import pw.koper.lang.parser.ast.impl.NumberExpression;
import pw.koper.lang.parser.ast.impl.StringExpression;

import java.util.*;

import static pw.koper.lang.util.StringUtil.toJvmName;
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
        this.result = new KoperClass(initialFileName, compiler);
    }

    public Token nextToken() throws CompilationException {
        if(!tokenIterator.hasNext()) {
            invalidToken("Expected continued declarations, got EOF", currentToken);
            throw new CompilationException(errors);
        }
        currentToken = tokenIterator.next();
        return currentToken;
    }

    @Override
    public KoperClass proceed() throws CompilationException {
        parseHead();
        result.annotationList.addAll(parseAnnotations());
        parseClassDeclaration();
        parseClassBody();
        if(errors.size() != 0) {
            throw new CompilationException(errors);
        }
        return result;
    }

    private void parseClassBody() throws CompilationException {
        if(!currentToken.is(LEFT_CURLY_BRACE)) {
            missingToken(LEFT_CURLY_BRACE);
            return;
        }
        do {
            parseStartMemberDeclaration();
        } while (!currentToken.is(RIGHT_CURLY_BRACE));
    }

    private void parseStartMemberDeclaration() throws CompilationException {
        AccessModifier accessModifier = null;
        HashSet<TokenKind> metKinds = new HashSet<>();
        boolean expectMethod = false;
        boolean expectField = false;
        List<Annotation> annotations = new ArrayList<>();
        ClassMemberDeclaration classMemberDeclaration = ClassMemberDeclaration.builder();
        while(true) {
            Token current = nextToken();
            if(current.is(ANNOTATION)){
                annotations.addAll(parseAnnotations());
            }

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
                    classMemberDeclaration.getting();
                    expectField = true;
                }
                case KEY_STATIC -> {
                    classMemberDeclaration.setStatic(true);
                }
                case KEY_SETTING -> {
                    classMemberDeclaration.setting();
                    expectField = true;
                }
                case KEY_ABSTRACT -> {
                    classMemberDeclaration.abstractKey();
                    expectMethod = true;
                }
                case TYPE_VOID -> {
                    expectMethod = true;
                }
                case KEY_FINAL -> classMemberDeclaration.setFinal(true);
            }
            if(current.isTypeDeclaration()) {
                break;
            }
        }

        if(classMemberDeclaration.isFinal() && classMemberDeclaration.isSetting()) {
            invalidDeclaration("Field can't have setter while being final", classMemberDeclaration.getName());
            return;
        }

        if(accessModifier == null) {
            accessModifier = AccessModifier.PUBLIC;
        }
        classMemberDeclaration.setAccessModifier(accessModifier);
        parseTypeAndNameDeclaration(classMemberDeclaration, expectField, expectMethod, annotations);
    }

    private void parseTypeAndNameDeclaration(ClassMemberDeclaration classMemberDeclaration, boolean expectField, boolean expectMethod, List<Annotation> annotations) throws CompilationException {
        Type type;
        if(currentToken.literal.equals(result.name)) {
            // this is a constructor of the class.
            // <init> method
            type = PrimitiveTypes.VOID;
            classMemberDeclaration.setType(type);
            parseMethodBody(classMemberDeclaration, annotations);
            return;
        }

        type = tokenToType(currentToken);

        if(type == null) {
            unexpectedToken("Invalid type: ", currentToken);
            return;
        }
        if(expectMethod && expectField) {
            invalidToken("Fields can't be abstract and have void type, methods can't have 'getting' or 'setting' attribute", currentToken);
        }
        classMemberDeclaration.setType(type);
        nextToken();
        if(!currentToken.isStrict()) {
            invalidToken("Name contains special characters", currentToken);
            return;
        }
        classMemberDeclaration.setName(currentToken.literal);
        Token end = nextToken();
        if(end.is(LEFT_PAREN)) { // this is 100% method
            parseMethodBody(classMemberDeclaration, annotations);
        } else { // this is field
            KoperField field = new KoperField(this.result, classMemberDeclaration);
            if(end.is(ASSIGN)) {
                // parse expression, we can't really do something
                // but we 100% know that class will have static constructor
                nextToken();
                Node initializer = parseExpression();
                if(initializer == null) {
                    return;
                }
                field.initializer = initializer;
            }

            field.annotationList.addAll(annotations);
            currentToken = tokenIterator.previous();
            result.fields.add(field);
        }
    }

    private void parseMethodBody(ClassMemberDeclaration member, List<Annotation> annotations) throws CompilationException {
        KoperMethod result = new KoperMethod(this.result, member.getType(), member.getName(), member.getAccessModifier(), member.isStatic());
        result.setDeclarationStart(currentToken.start);
        result.annotationList.addAll(annotations);
        parseMethodArguments(member, result);
        result.setDeclarationEnd(currentToken.end);
        parseMethodPrototype(member, result);
        this.result.methods.add(result);
    }

    private List<Annotation> parseAnnotations() throws CompilationException{
        List<Annotation> result = new ArrayList<>();

        while(currentToken.is(ANNOTATION)) {
            if (!currentToken.is(ANNOTATION)) return result;

            Annotation annotation = new Annotation(currentToken.literal, tokenToType(currentToken));

            nextToken();

            if (!currentToken.is(LEFT_PAREN) && currentToken.isClassDeclarationStart()) {
                result.add(annotation);
                return result;
            }
            if(currentToken.is(ANNOTATION)){
                result.add(annotation);
                continue;
            }

            if (!currentToken.is(LEFT_PAREN)) {
                invalidToken("Expected ( (Left paren)", currentToken);
                return result;
            }

            while (true) {
                Token argumentName = nextToken();
                if (!argumentName.is(NAME)) {
                    unexpectedToken("name", argumentName);
                    return result;
                }

                nextToken(); // Must be assign
                if (!currentToken.is(ASSIGN)) {
                    unexpectedToken("= (ASSING)", currentToken);
                    return result;
                }

                Token type = nextToken();

                if (!type.isTypeDeclaration() && !type.is(STRING) && !type.is(NUMBER)) {
                    unexpectedToken("type or annotation", type);
                    return result;
                }

                annotation.arguments.put(argumentName.literal, tokenToType(type) == null ? type.literal : tokenToType(type).toDescriptor());

                Token comma = nextToken(); // Comma expected but might be ) / End of args

                if (!comma.is(RIGHT_PAREN) && !comma.is(COMMA)) {
                    unexpectedToken("Right paren or comma", comma);
                    return result;
                }
                if (comma.is(RIGHT_PAREN)) {
                    break;
                }
            }
            result.add(annotation);
            nextToken();
        }
        return result;
    }
    private void parseMethodArguments(ClassMemberDeclaration classMemberDeclaration, KoperMethod method) throws CompilationException {
        if(!currentToken.is(LEFT_PAREN)) {
            unexpectedToken("'(' (paren)", currentToken);
            return;
        }

        while(true) {
            Token typeToken = nextToken();

            if(typeToken.is(RIGHT_PAREN)){ // if it doesn't need any args ()
                nextToken();
                return;
            }

            Type type = tokenToType(typeToken);
            if (type == null) {
                unexpectedToken("Type declaration (name or keyword of primitive type)", typeToken);
                return;
            }
            Token name = nextToken();
            if (!name.isStrict()) {
                invalidToken("Argument name can't contain special characters", name);
                return;
            }
            method.arguments.add(new MethodArgument(type, name.literal));

            Token comma = nextToken(); // Expect comma but might be right paren if it is end of args
            if(comma.is(RIGHT_PAREN)) {
                nextToken();
                break;
            }

            if(!comma.is(COMMA)){
                unexpectedToken("Splitter (comma ',')", comma);
                return;
            }
        }
    }

    private void parseMethodPrototype(ClassMemberDeclaration classMemberDeclaration, KoperMethod result) throws CompilationException {
        if(!currentToken.is(LEFT_CURLY_BRACE)) {
            unexpectedToken("'{' (paren)", currentToken);
            return;
        }

        // parsing the args expression
        while(!currentToken.is(RIGHT_CURLY_BRACE)) {
            nextToken();
//            parseStatement(result);
        }
        nextToken();
    }

    private void parseStatement(KoperMethod result) throws CompilationException {
        Token type = nextToken();
        boolean isDeclaration = false;
        switch (type.kind) {
            case NAME -> {
                isDeclaration = true;
            }
            default -> {
                notAStatement(type);
                return;
            }
        };
        Token name = nextToken();
        if(name.is(NAME) && name.isStrict()) {
            LocalVariableStatement localVariable = new LocalVariableStatement(name.lineNumber, result);
            result.methodBody.add(localVariable);
        } else {

        }

    }

    private Node parseExpression(KoperMethod in) throws CompilationException {
        if(currentToken.is(STRING)) {
            return new StringExpression(currentToken.literal);
        } else if(currentToken.is(NUMBER)) {
            return new NumberExpression(currentToken.literal);
        }

        return new LocalVariableStatement(123, in);
    }

    private Node parseExpression() throws CompilationException {
        return this.parseExpression(null);
    }

    private KoperObject typeNameToClass(Token token) {
        String className = result.getClassByName(token.literal);
        if(className == null) {
            errors.add(new CodeError(compiler, "Symbol not found", token));
            return null;
        }
        return new KoperObject(className);
    }

    private Type tokenToType(Token token) throws CompilationException{
        Type type = switch (token.kind) {
            case TYPE_VOID -> PrimitiveTypes.VOID;
            case TYPE_BYTE -> new PrimitiveTypes.ByteType();
            case TYPE_SHORT -> new PrimitiveTypes.ShortType();
            case TYPE_CHAR -> new PrimitiveTypes.CharType();
            case TYPE_INT -> new PrimitiveTypes.IntType();
            case TYPE_LONG -> new PrimitiveTypes.LongType();
            case TYPE_FLOAT -> new PrimitiveTypes.FloatType();
            case TYPE_DOUBLE -> new PrimitiveTypes.DoubleType();
            case TYPE_BOOLEAN -> new PrimitiveTypes.BooleanType();
            case NAME, ANNOTATION -> typeNameToClass(token);
            default -> null;
        };
        if(type == null) return null;
        nextToken();
        int arrayIterations = 0;
        if(currentToken.is(LEFT_BRACE)) {
            // array declaration
            tokenIterator.previous();
            while(true) {
                Token right = nextToken();
                if(!right.is(LEFT_BRACE)) {
                    break;
                }

                Token left = nextToken();
                if(!left.is(RIGHT_BRACE)) {
                    invalidToken("Array declaration isn't closed by ]", currentToken);
                    return null;
                }
                arrayIterations++;
            }
        }
        tokenIterator.previous();
        type.setNestedArraysCount(arrayIterations);
        return type;
    }

    private void parseClassDeclaration() throws CompilationException {
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
            return; // I hate this return, caused too much bugs
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

    private boolean parseImplementsExpression(Collection<String> interfaces) throws CompilationException{
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
    private void parseHead() throws CompilationException{
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
            if(next.isClassDeclarationStart() || next.is(ANNOTATION)) {
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

    private void notAStatement(Token token) {
        errors.add(new CodeError(compiler, "Not a statement.", token));
    }

    private void notDeclared(String what, Token where) {
        errors.add(new CodeError(compiler, what + " is not declared", where));
    }

    private void invalidDeclaration(String why, String className) {
        errors.add(new CodeError(this, "Invalid declaration at " + className + ": " + why));
    }
}
