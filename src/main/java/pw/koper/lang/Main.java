package pw.koper.lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class Main {
    private static final String testPath = "/Users/sasha/Projects/KoperLang/example-code.koper";
    public static void main(String[] args) {
        System.out.println("Starting");
        StringBuilder code = new StringBuilder();
        /*try (Stream<String> st = Files.lines(Paths.get(testPath))) {
            st.forEach(code::append);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        byte[] readed = Files.readAllBytes(Paths.get(testPath));
        for(byte b : readed){
            System.out.println((char) b);
            code.append((char) b);
        }
        KoperLang.compile(code.toString());
    }
}