package pw.koper.lang.common.internal;

public class KoperMethod extends KoperClassMember {

    public KoperMethod(Type type, String name, AccessModifier accessModifier, boolean isStatic) {
        super(type, name, accessModifier, isStatic, false);
    }
}