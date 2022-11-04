package pw.koper.lang.common.internal;

import lombok.Getter;
import lombok.Setter;

@Getter
public class ClassMemberDeclaration {
    @Setter private AccessModifier accessModifier;
    private boolean isAbstract;
    private boolean setting;
    private boolean getting;
    @Setter private Type type;
    @Setter private String name;
    @Setter private boolean isStatic;
    @Setter private boolean isFinal;

    private ClassMemberDeclaration() {}

    public void setting() {
        this.setting = true;
    }

    public void getting() {
        this.getting = true;
    }

    public void abstractKey() {
        this.isAbstract = true;
    }

    public static ClassMemberDeclaration builder() {
        return new ClassMemberDeclaration();
    }
}
