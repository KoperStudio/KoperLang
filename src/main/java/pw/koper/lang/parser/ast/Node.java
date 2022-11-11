package pw.koper.lang.parser.ast;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.objectweb.asm.MethodVisitor;
import pw.koper.lang.common.internal.KoperClass;
import pw.koper.lang.common.internal.KoperMethod;

import java.lang.reflect.Field;
import java.util.Collection;

public abstract class Node {
    public void generateBytecode(MethodVisitor visitor) {}
    @SneakyThrows
    public String asString() {
        StringBuilder result = new StringBuilder(this.getClass().getSimpleName()).append("{");
        for(Field field : this.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Object value = field.get(this);
            result.append("\n\t").append(field.getName()).append("=");
            if(value instanceof Node) {
                result.append(((Node)value).asString());
            } else if (value instanceof Collection<?>) {
                result.append("[");
                for (Object listValue : (Collection<?>)value) {
                    if(listValue instanceof Node) {
                        result.append("\n\t").append(((Node) listValue).asString());
                    }
                }
                result.append("]");
            } else {
                result.append(field.get(this).toString());
            }
        }
        return result.append("\n").append("}").toString();
    }
}
