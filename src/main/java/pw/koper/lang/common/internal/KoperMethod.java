package pw.koper.lang.common.internal;

import java.util.HashSet;

public class KoperMethod extends KoperClassMember {

    public final HashSet<MethodArgument> arguments;

    public KoperMethod(Type type, String name, AccessModifier accessModifier, boolean isStatic) {
        super(type, name, accessModifier, isStatic, false);
        arguments = new HashSet<>();
    }
}