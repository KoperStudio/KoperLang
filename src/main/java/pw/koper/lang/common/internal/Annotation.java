package pw.koper.lang.common.internal;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;

import java.util.HashMap;
import java.util.Map;

public class Annotation {

    public final String annotationName;
    public final Type type;
    public final HashMap<String, String> arguments;

    public Annotation(String annotationName, Type type){
        this.annotationName = annotationName;
        this.arguments = new HashMap<>();
        this.type = type;
    }

    public void generateBytecode(ClassWriter writer) {
        AnnotationVisitor visitor = writer.visitAnnotation(type.toDescriptor(), true);
        for(Map.Entry<String, String> entry : arguments.entrySet()){
            visitor.visit(entry.getKey(), entry.getValue());
        }
        visitor.visitEnd();
    }

    public void generateBytecode(FieldVisitor writer) {
        AnnotationVisitor visitor = writer.visitAnnotation(type.toDescriptor(), true);
        for(Map.Entry<String, String> entry : arguments.entrySet()){
            visitor.visit(entry.getKey(), entry.getValue());
        }
        visitor.visitEnd();
    }

    public void generateBytecode(MethodVisitor writer) {
        AnnotationVisitor visitor = writer.visitAnnotation(type.toDescriptor(), true);
        for(Map.Entry<String, String> entry : arguments.entrySet()){
            visitor.visit(entry.getKey(), entry.getValue());
        }
        visitor.visitEnd();
    }
}
