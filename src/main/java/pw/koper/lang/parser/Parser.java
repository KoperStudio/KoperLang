package pw.koper.lang.parser;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.lexer.TokenKind;
import pw.koper.lang.parser.ast.AstImpl;
import pw.koper.lang.parser.ast.Node;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import static pw.koper.lang.lexer.TokenKind.*;

public class Parser extends CompilationStage<LinkedList<Node>> {

    private final KoperCompiler compiler;
    private final LinkedList<Node> tree = new LinkedList<>();
    private final Iterator<Token> tokenIterator;
    private Token currentToken;
    public Parser(KoperCompiler compiler) {
        this.compiler = compiler;
        this.tokenIterator = compiler.getTokens().iterator();
    }

    @Override
    public LinkedList<Node> proceed() throws CompilationException {
        parseHead();
        parseClassDeclaration();
        if(errors.size() != 0) {
            throw new CompilationException(errors);
        }
        return tree;
    }

    private void parseClassDeclaration() {
        // do loop because previous method already goes for next token for us
        boolean isAbstract = false;
        boolean isStatic = false;
        boolean isData = false;
        boolean isPublic = true;
        String superClass = "java.lang.Object";
        HashSet<String> interfaces = new HashSet<>();
        do {
            switch (currentToken.kind) {
                case KEY_PRIVATE -> isPublic = false;
                case KEY_PROTECTED -> {
                    invalidToken("Class can't have protected access level", currentToken);
                    return;
                }
                case KEY_ABSTRACT -> isAbstract = true;
                case KEY_STATIC ->  isStatic = true;
                case KEY_DATA -> isData = true;
            }
        } while(!nextToken().isClassDeclarationStart());
        nextToken();
        if(!currentToken.isStrict()) {
            invalidToken("Class name can't contain special characters and can't start with numbers", currentToken);
            return;
        }
        tree.add(new AstImpl.ClassDeclaration(isPublic, isAbstract, isStatic, isData, superClass, interfaces));
    }

    // this method returns initial class writer with all needed data
    private void parseHead() {
        Token firstToken = nextToken();
        if(firstToken.is(TokenKind.KEY_PACKAGE)) {
            nextToken();
            if(!currentToken.is(TokenKind.NAME)) {
                unexpectedToken("package name", currentToken);
                return;
            }
            tree.add(new AstImpl.PackageStatement(currentToken.literal));
        }
        while(true) {
            Token next = nextToken();
            if(next.is(TokenKind.KEY_IMPORT)) {
                nextToken();
                if(!currentToken.is(TokenKind.NAME)) {
                    unexpectedToken("importing class name", currentToken);
                    return;
                }
                tree.add(new AstImpl.ImportStatement(currentToken.literal));
            }
            if(next.isClassDeclarationStart()) {
                return;
            }
        }
    }

    private void unexpectedToken(String expected, Token where) {
        errors.add(new CodeError(compiler, "Unexpected token. Expected " + expected + ", got " + where.kind, where));
    }

    private void invalidToken(String why, Token where) {
        errors.add(new CodeError(compiler, "Invalid token " + where.kind + ". " + why, where));
    }

    public Token nextToken() {
        currentToken = tokenIterator.next();
        return currentToken;
    }
}
