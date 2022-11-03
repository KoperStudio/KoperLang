package pw.koper.lang.common.internal;

public abstract class Type {

    public abstract String toDescriptor();

    // sometimes we don't need signatures and null value can be passed
    public String toSignature() {
        return null;
    }
}
