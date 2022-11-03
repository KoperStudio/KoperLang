package pw.koper.lang.common.internal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;
import pw.koper.lang.parser.ast.Node;

@RequiredArgsConstructor
@Getter
public class KoperField {
    private final Type type;
    private final String name;
    private final AccessModifier accessModifier;
    public boolean hasSetter = false;
    public boolean hasGetter = false;

    public Node staticValueDeclaration; // as all nodes accepting methodwriter, we will init static method in the <cinit> method
}
