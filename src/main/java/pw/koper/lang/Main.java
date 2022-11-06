package pw.koper.lang;

import pw.koper.lang.common.KoperCompiler;

import java.io.File;
import java.io.IOException;

public class Main {
    private static final String testPath = "./TestClass.koper";
    public static final boolean DEBUG = false;
    public static void main(String[] args) throws IOException {
        new KoperCompiler(new File(testPath)).compile();
    }

}