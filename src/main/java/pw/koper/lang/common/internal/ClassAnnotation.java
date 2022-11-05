package pw.koper.lang.common.internal;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;

import java.util.Map;

public class ClassAnnotation extends Annotation{


    public ClassAnnotation(String annotationName, Type type) {
        super(annotationName, type);
    }

    @Override
    public void generateBytecode(ClassWriter writer) {
        AnnotationVisitor visitor = writer.visitAnnotation(type.toDescriptor(), true);
        for(Map.Entry<String, String> entry : arguments.entrySet()){
            visitor.visit(entry.getKey(), entry.getValue());
        }
        visitor.visitEnd();
    }
}
