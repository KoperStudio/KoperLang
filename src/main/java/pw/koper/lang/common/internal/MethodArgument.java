package pw.koper.lang.common.internal;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MethodArgument {
    private final Type type;
    private final String name;
}
