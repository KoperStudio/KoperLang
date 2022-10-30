package pw.koper.lang.common;

import pw.koper.lang.gen.BytecodeGenerator;
import pw.koper.lang.lexer.Lexer;
import pw.koper.lang.lexer.Token;
import pw.koper.lang.parser.Parser;
import pw.koper.lang.parser.ast.Node;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class KoperCompiler {
    private final Lexer lexer;
    private final File file;
    private LinkedList<Token> tokens;
    private LinkedList<Node> ast = new LinkedList<>();

    public KoperCompiler(File file) throws IOException {
        this.file = file;
        byte[] read = Files.readAllBytes(file.toPath());
        this.lexer = new Lexer(this, new String(read, StandardCharsets.UTF_8));
    }
    public void compile() {
        try {
            tokens = lexer.proceed();
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.err.println("Error: " + error.render());
            }
            return;
        }
        for(Token token : tokens) {
            System.out.println(token.kind + ": " + token.literal);
        }

        Parser parser = new Parser(this);
        try {
            ast = parser.proceed();
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.err.println("Error: " + error.render());
            }
        }

        String targetFile = file.getName().replace(".koper", ".class");
        BytecodeGenerator generator = new BytecodeGenerator(this);
        try {
            byte[] resultedFile = generator.proceed();
            if(resultedFile.equals(BytecodeGenerator.EMPTY)) {
                System.out.println("Empty output.");
                return;
            }
            Files.write(Path.of(targetFile), resultedFile);
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.err.println("Error: " + error.render());
            }
        } catch (IOException e) {
            System.err.println("Failed to write result to file " + targetFile);
        }
    }

    public LinkedList<Token> getTokens() {
        return tokens;
    }

    public File getCompilingFile() {
        return file;
    }
}
