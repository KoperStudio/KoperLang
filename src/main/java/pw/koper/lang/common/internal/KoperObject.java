package pw.koper.lang.common.internal;

public class KoperObject extends Type {
    private final String name;
    public KoperObject(String name) {
        this.name = name;
    }

    @Override
    public String toDescriptor() {
        return "L" + this.name + ";";
    }
}
