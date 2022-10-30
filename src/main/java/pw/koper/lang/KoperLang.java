package pw.koper.lang;

import pw.koper.lang.common.CodeError;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.lexer.Lexer;
import pw.koper.lang.lexer.Token;

import java.io.File;
import java.util.LinkedList;

public class KoperLang {
    public static void compile(String code) {
        Lexer lexer = new Lexer(code);
        LinkedList<Token> tokens;
        try {
            tokens = lexer.lex();
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.out.println("Error: "+error.render());
            }
            return;
        }
        for(Token token : tokens) {
            System.out.println(token.kind + ": " + token.literal);
        }
        // parse
    }

    public static void build(File directory) {

    }
}
