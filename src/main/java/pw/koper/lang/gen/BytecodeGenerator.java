package pw.koper.lang.gen;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import pw.koper.lang.Main;
import pw.koper.lang.common.CompilationException;
import pw.koper.lang.common.CompilationStage;
import pw.koper.lang.common.KoperCompiler;

public class BytecodeGenerator extends CompilationStage<byte[]> {

    public static byte[] EMPTY = new byte[0];
    private static final String descriptorRegex = "\\<[^)]*\\>";
    private final KoperCompiler compiler;

    public BytecodeGenerator(KoperCompiler compiler) {
        super();
        this.compiler = compiler;
    }

    @Override
    public byte[] proceed() throws CompilationException {
        if(Main.DEBUG) {
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            classWriter.visitSource(compiler.getCompilingFile().getName(), null);
            String className = "me/mrfunny/TestClass";
            classWriter.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, className, null, "java/lang/Object", new String[0]);
            MethodVisitor init = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/util/List;IILjava/lang/String;Lpw/koper/lang/KoperLang;JSBCFDZ)V", "(Ljava/util/List<Ljava/lang/String;>;IILjava/lang/String;Lme/mrfunny/KoperLang;JSBCFDZ)V", null);
            init.visitCode();
            Label l0 = new Label();
            init.visitLabel(l0);
            init.visitLineNumber(10, l0);
            init.visitVarInsn(Opcodes.ALOAD, 0);
            init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            Label l1 = new Label();
            init.visitLabel(l1);
            init.visitLineNumber(11, l1);
            init.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            init.visitVarInsn(Opcodes.ALOAD, 1);
            init.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
            init.visitInsn(Opcodes.RETURN);
            Label l2 = new Label();
            init.visitLabel(l2);
            init.visitLocalVariable("this", "L" + className, null, l0, l2, 0);
            init.visitLocalVariable("stringList", "Ljava/util/List", "Ljava/util/List<Ljava/lang/String;>;", l0, l2, 1);
            init.visitLocalVariable("amongus", "I", null, l0, l2, 2);
            init.visitEnd();
            classWriter.visitEnd();
            return classWriter.toByteArray();
        }
        return EMPTY;
    }
}
