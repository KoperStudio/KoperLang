package pw.koper.lang.parser.ast.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pw.koper.lang.common.internal.KoperMethod;
import pw.koper.lang.parser.ast.StatementAST;

@AllArgsConstructor
@Getter
public class MethodStatement extends StatementAST {
    private final int lineNumber;
    private final KoperMethod forMethod;
}
