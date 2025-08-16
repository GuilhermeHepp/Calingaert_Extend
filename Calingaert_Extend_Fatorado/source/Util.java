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

    public static boolean eNumero(String str) {
        return (str.startsWith("H'") && str.endsWith("'") && str.length() > 3) || str.matches("\\d+");
    }

    public static int parseNumero(String str) {
        int valor;

        if (str.startsWith("H'") && str.endsWith("'")) {
            String hex = str.substring(2, str.length() - 1);
            if (!hex.matches("[0-9A-Fa-f]+")) {
                throw new RuntimeException("Erro: dígito inválido no número hexadecimal -> " + str);
            }
            valor = Integer.parseInt(hex, 16);
        } else if (str.matches("\\d+")) {
            valor = Integer.parseInt(str);
        } else {
            throw new RuntimeException("Erro: formato inválido de número -> " + str);
        }

        if (valor < 0 || valor > 255) {
            throw new RuntimeException("Erro: valor fora dos limites (0–255) -> " + str);
        }

        return valor;
    }

    public static int parseLiteral(String str) {
        if (!str.startsWith("@")) {
            throw new RuntimeException("Erro: literal mal formatado -> " + str);
        }

        String literal = str.substring(1);
        if (!literal.matches("\\d+")) {
            throw new RuntimeException("Erro: dígito inválido no literal -> " + str);
        }

        int valor = Integer.parseInt(literal);
        if (valor < 0 || valor > 255) {
            throw new RuntimeException("Erro: valor fora dos limites (0–255) -> " + str);
        }

        return valor;
    }
}

