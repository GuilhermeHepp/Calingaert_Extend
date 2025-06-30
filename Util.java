import java.util.HashMap;
import java.util.Map;

public class Util {
    private static final Map<String, Integer> OPCODES = new HashMap<>();

    static {
        OPCODES.put("BR", 0);
        OPCODES.put("BRPOS", 1);
        OPCODES.put("ADD", 2);
        OPCODES.put("LOAD", 3);
        OPCODES.put("BRZERO", 4);
        OPCODES.put("BRNEG", 5);
        OPCODES.put("SUB", 6);
        OPCODES.put("STORE", 7);
        OPCODES.put("WRITE", 8);
        OPCODES.put("DIVIDE", 10);
        OPCODES.put("STOP", 11);
        OPCODES.put("READ", 12);
        OPCODES.put("COPY", 13);
        OPCODES.put("MULT", 14);
        OPCODES.put("CALL", 15);
        OPCODES.put("RET", 16);
    }

    public static int obterOpcode(String instrucao) {
        return OPCODES.getOrDefault(instrucao.toUpperCase(), -1);
    }
}

