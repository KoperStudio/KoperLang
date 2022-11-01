package pw.koper.lang.common;

public class StringUtil {
    public static String getEntireLine(String text, int index) {
        String[] textSplit = text.split("\n");
        return textSplit[index];
    }

    public static String classNameToDescriptor(String className) {
        return "L" + className.replaceAll("\\.", "/") + ";";
    }
}
