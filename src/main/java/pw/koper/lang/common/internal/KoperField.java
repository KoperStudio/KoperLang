package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.parser.ast.Node;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KoperField extends KoperClassMember {

    public List<Annotation> annotationList = new ArrayList<>();

    public boolean hasSetter = false;
    public boolean hasGetter = false;

    public Node valueDeclaration; // as all nodes accepting methodwriter, we will init static method in the <cinit> method

    public KoperField(KoperClass forClass, ClassMemberDeclaration classMemberDeclaration) {
        super(forClass, classMemberDeclaration.getType(), classMemberDeclaration.getName(), classMemberDeclaration.getAccessModifier(), classMemberDeclaration.isStatic(), classMemberDeclaration.isFinal());
        hasSetter = classMemberDeclaration.isSetting();
        hasGetter = classMemberDeclaration.isGetting();
    }

    @Override
    public void generateBytecode(ClassWriter classWriter) {
        FieldVisitor visitor = classWriter.visitField(getOpcodeAccessModifier(), getName(), getType().toDescriptor(), getType().toSignature(), null);
        for(Annotation annotation : annotationList){
            annotation.generateBytecode(visitor);
        }
    }
}
