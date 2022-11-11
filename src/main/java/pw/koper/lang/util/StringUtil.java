package pw.koper.lang.util;

import org.apache.commons.lang3.tuple.Pair;

public class StringUtil {
    public static String getEntireLine(String text, int index) {
        String[] textSplit = text.split("\n");
        return textSplit[index];
    }

    public static Pair<Integer, String> getLineDataFromSubstring(String text, int start, int end) {
        String[] textSplit = text.split("\n");
        String toSearch = text.substring(start, end);
        for (int i = 0; i < textSplit.length; i++) {
            String line = textSplit[i];
            if(line.contains(toSearch))  {
                return Pair.of(i, line);
            }
        }

        return null;
    }

    public static String classNameToDescriptor(String className) {
        return "L" + toJvmName(className) + ";";
    }

    public static String toJvmName(String className) {
        return className.replaceAll("\\.", "/").intern();
    }
}
