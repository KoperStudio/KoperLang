package pw.koper.lang;

import pw.koper.lang.common.KoperCompiler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final String testPath = "./example-code.koper";
    public static void main(String[] args) throws IOException {
        new KoperCompiler(new File(testPath)).compile();
    }
}