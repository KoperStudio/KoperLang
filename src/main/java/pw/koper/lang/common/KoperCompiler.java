package pw.koper.lang.common;

import lombok.Getter;
import lombok.Setter;
import pw.koper.lang.common.internal.KoperClass;
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class KoperCompiler {
    private final Lexer lexer;
    private final File file;
    @Getter private LinkedList<Token> tokens;
    @Getter private LinkedList<Node> ast;
    @Getter private CompilationStage<?> stage;

    @Getter @Setter private String input;

    public KoperCompiler(File file) throws IOException {
        this.file = file;
        byte[] read = Files.readAllBytes(file.toPath());
        this.lexer = new Lexer(this, new String(read, StandardCharsets.UTF_8));
        this.stage = lexer;
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
//        for(Token token : tokens) {
//            System.out.println(token.kind + ": " + token.literal);
//        }

        Parser parser = new Parser(this);
        this.stage = parser;
        KoperClass result = null;
        try {
            result = parser.proceed();
        } catch (CompilationException e) {
            for(CodeError error : e.getErrors()) {
                System.err.println("Error: " + error.render());
            }
        }

//        for(Node astNode : ast) {
//            System.out.println(astNode.asString());
//        }
        if(result == null) {
            System.err.println("Failed to compile AST");
            return;
        }
        String targetFile = file.getName().replace(".koper", ".class");
        try {
            byte[] resultedFile = result.generateClass();
            if(Arrays.equals(resultedFile, BytecodeGenerator.EMPTY)) {
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

    public String getLineByNumber(int line) {
        return StringUtil.getEntireLine(input, line);
    }

    public File getCompilingFile() {
        return file;
    }
}
