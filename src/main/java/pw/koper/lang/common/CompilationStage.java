package pw.koper.lang.common;

public abstract class CompilationStage<R> {
    protected String fileName;
    protected int position = 0;
    protected int line = 0;
    protected int column = 0;

    public abstract R proceed() throws CompilationException;
}
