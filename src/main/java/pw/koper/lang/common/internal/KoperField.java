package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.parser.ast.Node;

@Getter
public class KoperField extends KoperClassMember {

    public boolean hasSetter = false;
    public boolean hasGetter = false;

    public Node valueDeclaration; // as all nodes accepting methodwriter, we will init static method in the <cinit> method

    public KoperField(Type type, String name, AccessModifier accessModifier, boolean isStatic) {
        super(type, name, accessModifier, isStatic, false);
    }

    public KoperField(ClassMemberDeclaration classMemberDeclaration) {
        super(classMemberDeclaration.getType(), classMemberDeclaration.getName(), classMemberDeclaration.getAccessModifier(), classMemberDeclaration.isStatic(), classMemberDeclaration.isFinal());
        hasSetter = classMemberDeclaration.isSetting();
        hasGetter = classMemberDeclaration.isGetting();
    }

    @Override
    public void generateBytecode(ClassWriter classWriter) {
        classWriter.visitField(getOpcodeAccessModifier(), getName(), getType().toDescriptor(), getType().toSignature(), null);
    }
}
