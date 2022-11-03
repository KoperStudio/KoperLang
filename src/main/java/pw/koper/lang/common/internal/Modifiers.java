package pw.koper.lang.common.internal;

import lombok.Getter;

@Getter
public class Modifiers {
    private AccessModifier accessModifier;
    private boolean isAbstract;
    private boolean setting;
    private boolean getting;

    private Modifiers() {}

    public void setting() {
        this.setting = true;
    }

    public void getting() {
        this.getting = true;
    }

    public void abstractKey() {
        this.isAbstract = true;
    }

    public void access(AccessModifier modifier) {
        this.accessModifier = modifier;
    }

    public static Modifiers builder() {
        return new Modifiers();
    }
}
