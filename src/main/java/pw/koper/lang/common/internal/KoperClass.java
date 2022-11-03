package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.parser.ast.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static pw.koper.lang.common.StringUtil.toJvmName;

public class KoperClass {
    private final String sourceFileName;
    public String name;
    public boolean isPublic;
    public boolean isAbstract;
    public boolean isStatic;
    public boolean isData;
    public String superClass;
    public ClassType classType;
    public Set<String> interfaces;
    public Set<KoperMethod> methods = new HashSet<>();
    public Set<KoperField> fields = new HashSet<>();

    // Map from short name to full name
    public HashMap<String, String> imports = new HashMap<>();
    public KoperClass(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }
    public byte[] generateClass() throws CompilationException {
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classWriter.visitSource(this.sourceFileName.replace(".koper", ".class"), null);
        int access = ACC_PUBLIC | ACC_SUPER;
        switch (classType) {
            case INTERFACE -> access |= ACC_INTERFACE;
            case ENUM -> access |= ACC_ENUM;
        }
        if(isAbstract) {
            access |= ACC_ABSTRACT;
        }

        classWriter.visit(Opcodes.V11, access, name, null, superClass, interfaces.toArray(new String[0]));
        for(KoperField field : fields) {
            classWriter.visitField(field.getAccessModifier().toOpcode(), field.getName(), field.getType().toDescriptor(), field.getType().toSignature(), null);
        }
        classWriter.visitEnd();
        return classWriter.toByteArray();
    }

    // Returns true if class isn't enum and interface
    public boolean isFullClass() {
        return classType == ClassType.CLASS;
    }

    public String getClassByName(String name) {
        if(name.contains(".")) return toJvmName(name);

        return imports.get(name);
    }
    
}
