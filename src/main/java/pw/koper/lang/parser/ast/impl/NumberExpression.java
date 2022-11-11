package pw.koper.lang.parser.ast.impl;

import pw.koper.lang.parser.ast.ExpressionAST;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberExpression extends NonComplexStatement{
    private Class<?> javaType = int.class;
    private Number actualValue;
    public NumberExpression(String literal) {
        super(Long.parseLong(literal));
    }
}
