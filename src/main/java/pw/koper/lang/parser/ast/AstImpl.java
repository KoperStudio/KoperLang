package pw.koper.lang.parser.ast;

import lombok.AllArgsConstructor;

import javax.swing.plaf.nimbus.State;
import java.util.Set;

public class AstImpl {
    @AllArgsConstructor
    public static class PackageStatement extends StatementAST {
        String name;
    }

    @AllArgsConstructor
    public static class ImportStatement extends StatementAST {
        String toImport;
    }

    @AllArgsConstructor
    public static class ClassDeclaration extends StatementAST {
        boolean isPublic;
        boolean isAbstract;
        boolean isStatic;
        boolean isData;
        String superClass;
        Set<String> interfaces;
    }
}
