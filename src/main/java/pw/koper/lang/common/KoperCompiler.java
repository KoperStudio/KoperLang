package pw.koper.lang.common;

import pw.koper.lang.lexer.Lexer;
import pw.koper.lang.lexer.Token;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;

public class KoperCompiler {
    private final Lexer lexer;
    private final File file;

    public KoperCompiler(File file) throws IOException {
        this.file = file;
        byte[] read = Files.readAllBytes(file.toPath());
        this.lexer = new Lexer(this, new String(read, StandardCharsets.UTF_8));
    }
    public void compile() {
        LinkedList<Token> tokens;
        try {
            tokens = lexer.lex();
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.out.println("Error: " + error.render());
            }
            return;
        }
        for(Token token : tokens) {
            System.out.println(token.kind + ": " + token.literal);
        }
    }

    public File getCompilingFile() {
        return file;
    }
}
