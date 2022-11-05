package pw.koper.lang.common.internal;

import org.objectweb.asm.ClassWriter;
import java.util.HashMap;

public abstract class Annotation {

    public final String annotationName;
    public final Type type;
    public final HashMap<String, String> arguments;

    public Annotation(String annotationName, Type type){
        this.annotationName = annotationName;
        this.arguments = new HashMap<>();
        this.type = type;
    }

    public abstract void generateBytecode(ClassWriter writer);
}
