package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KoperClassMember {
    private Type type;
    private String name;
    private AccessModifier accessModifier;
    private boolean isStatic;
    private boolean isFinal;
}
