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
        System.out.println("Starting");
//        StringBuilder code = new StringBuilder();
//        /*try (Stream<String> st = Files.lines(Paths.get(testPath))) {
//            st.forEach(code::append);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        }*/
//        byte[] read = Files.readAllBytes(Paths.get(testPath));
//        File file = new File(testPath);
        new KoperCompiler(new File(testPath)).compile();
//        KoperLang.compile(file.getName(), new String(read, StandardCharsets.UTF_8));
    }
}