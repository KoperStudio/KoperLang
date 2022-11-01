package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.parser.ast.Node;

import java.util.HashSet;
import java.util.Set;

public class KoperClass {
    private final String sourceFileName;
    public String name;
    public boolean isPublic;
    public boolean isAbstract;
    public boolean isStatic;
    public boolean isData;
    public String superClass;
    public Set<String> interfaces;
    public Set<KoperMethod> methods = new HashSet<>();

    public KoperClass(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
    public byte[] generateClass() throws CompilationException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visitSource(this.sourceFileName.replace(".koper", ".class"), null);
        classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, name, null, superClass, new String[0]);
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }
    
}
