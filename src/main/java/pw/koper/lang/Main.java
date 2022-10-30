package pw.koper.lang;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    private static final String testPath = "./example-code.koper";
    public static void main(String[] args) throws IOException {
        System.out.println("Starting");
        StringBuilder code = new StringBuilder();
        /*try (Stream<String> st = Files.lines(Paths.get(testPath))) {
            st.forEach(code::append);
        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
        byte[] readed = Files.readAllBytes(Paths.get(testPath));
        for(byte b : readed){
            code.append((char) b);
        }
        KoperLang.compile(code.toString());
    }
}