import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LinhaAsm {
    public String label;
    public String opcode;
    public List<String> operandos = new ArrayList<>();

public static final Set<String> OPCODES = Set.of(
    "READ", "WRITE", "LOAD", "ADD", "STORE", "STOP", "COPY", "RET",
    "SPACE", "CONST", "STACK", "START", "END", "INTDEF", "INTUSE"
);

public static LinhaAsm parse(String linha) {
    LinhaAsm l = new LinhaAsm();

    if (linha.trim().isEmpty() || linha.trim().startsWith("*")) return l;

    String[] partes = linha.trim().split("\\s+");
    int idx = 0;

    if (partes.length > 0) {
        String first = partes[0].toUpperCase();

        if (OPCODES.contains(first)) {
            // Primeiro token é opcode, não tem label
            l.opcode = partes[idx++];
        } else {
            // Primeiro token não é opcode → é label
            l.label = partes[idx++];
            if (idx < partes.length) {
                l.opcode = partes[idx++];
            }
        }

        while (idx < partes.length) {
            l.operandos.add(partes[idx++]);
        }
    }

    return l;
}


}


