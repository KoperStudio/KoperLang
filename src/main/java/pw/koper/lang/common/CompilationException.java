package pw.koper.lang.common;

import java.util.HashSet;

public class CompilationException extends Exception {
    private final HashSet<CodeError> errors;

    public CompilationException(HashSet<CodeError> errors) {
        this.errors = errors;
    }

    public HashSet<CodeError> getErrors() {
        return errors;
    }
}
