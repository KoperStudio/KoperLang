package pw.koper.lang.common.internal;

import lombok.Getter;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import pw.koper.lang.parser.ast.Node;
import pw.koper.lang.parser.ast.impl.NonComplexStatement;
import pw.koper.lang.parser.ast.impl.NumberExpression;
import pw.koper.lang.parser.ast.impl.StringExpression;

import java.util.ArrayList;
import java.util.List;

@Getter
public class KoperField extends KoperClassMember {

    public List<Annotation> annotationList = new ArrayList<>();

    public boolean hasSetter = false;
    public boolean hasGetter = false;

    public Node valueDeclaration; // as all nodes accepting methodwriter, we will init static method in the <cinit> method
    public Node initializer = null;

    public KoperField(KoperClass forClass, ClassMemberDeclaration classMemberDeclaration) {
        super(forClass, classMemberDeclaration.getType(), classMemberDeclaration.getName(), classMemberDeclaration.getAccessModifier(), classMemberDeclaration.isStatic(), classMemberDeclaration.isFinal());
        hasSetter = classMemberDeclaration.isSetting();
        hasGetter = classMemberDeclaration.isGetting();
    }

    @Override
    public void generateBytecode(ClassWriter classWriter) {
        Object initialValue;
        if(!isComplexInitialisation() && isFinal()) {
            initialValue = ((NonComplexStatement)initializer).getValue();
        } else {
            initialValue = null;
            if(initializer != null) {
                if(isStatic()) {
                    forClass.toStaticInit.add(this);
                } else {
                    forClass.toObjectInit.add(this);
                }
            }
        }

        FieldVisitor visitor = classWriter.visitField(getOpcodeAccessModifier(), getName(), getType().toDescriptor(), getType().toSignature(), initialValue);
        for(Annotation annotation : annotationList){
            annotation.generateBytecode(visitor);
        }
    }

    public boolean isInitialised() {
        return this.initializer != null;
    }

    public boolean isComplexInitialisation() {
        return !(initializer instanceof NonComplexStatement);
    }
}
