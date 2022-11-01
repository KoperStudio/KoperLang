package pw.koper.lang.common;

import lombok.Getter;

import java.util.HashSet;

public abstract class CompilationStage<R> {
    protected String fileName;
    protected int position = 0;
    protected int line = 0;
    protected int column = 0;
    protected final HashSet<CodeError> errors = new HashSet<>(1);

    public abstract R proceed() throws CompilationException;
}
