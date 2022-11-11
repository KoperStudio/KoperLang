package pw.koper.lang.common.internal;

import java.util.HashSet;

public class KoperConstructor extends KoperMethod {

    public KoperConstructor(KoperClass forClass, Type type, AccessModifier accessModifier, boolean isStatic) {
        super(forClass, type, String.format("<%sinit>", isStatic ? "cl" : ""), accessModifier, isStatic);
    }
}
