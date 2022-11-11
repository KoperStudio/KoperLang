package pw.koper.lang.parser.ast.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import pw.koper.lang.parser.ast.StatementAST;

@AllArgsConstructor
@Getter
public class NonComplexStatement extends StatementAST {
    private final Object value;
}
