package pw.koper.lang.common.internal;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.parser.ast.Node;
import pw.koper.lang.util.IntIterator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class KoperMethod extends KoperClassMember {

    public final List<Annotation> annotationList = new ArrayList<>();
    public final LinkedList<Node> methodBody = new LinkedList<>();

    public final ArrayList<MethodArgument> arguments;
    public IntIterator methodCounter = new IntIterator();

    public KoperMethod(KoperClass forClass, Type type, String name, AccessModifier accessModifier, boolean isStatic) {
        super(forClass, type, name, accessModifier, isStatic, false);
        arguments = new ArrayList<>();
    }

    @Override
    public void generateBytecode(ClassWriter classWriter) {
        StringBuilder descriptor = new StringBuilder("(");
        for(MethodArgument argument : arguments) {
            descriptor.append(argument.getType().toDescriptor());
        }
        descriptor.append(")");
        descriptor.append(getType().toDescriptor());
        MethodVisitor visitor = classWriter.visitMethod(getOpcodeAccessModifier(), getName(), descriptor.toString(), null, new String[0]);
        for(Annotation annotation : annotationList){
            annotation.generateBytecode(visitor);
        }
        Label firstLabel = new Label();
        visitor.visitLabel(firstLabel);
        if(!isStatic()) {
            visitor.visitVarInsn(Opcodes.ALOAD, 0); // loading 'this' variable
        }
        if(methodBody.isEmpty()) {
            visitor.visitInsn(Opcodes.RETURN);
        } else {
            for(Node node : methodBody) {
                node.generateBytecode(visitor);
            }
        }
        Label lastLabel = new Label();
        visitor.visitLabel(lastLabel);
        int counter = 1; // starting from 1, because 0 index takes 'this' variable
        for(MethodArgument argument : arguments) {
            visitor.visitLocalVariable(argument.getName(), argument.getType().toDescriptor(), argument.getType().toSignature(), firstLabel, lastLabel, counter);
            counter++;
        }
    }

    public void insertInstruction(Node node) {
        methodBody.add(node);
    }
}