import java.util.HashMap;
import java.util.Map;

public class TabelaSimbolos {
    
private final Map<String, Integer> simbolos = new HashMap<>();

    public void adicionar(String label, int endereco) {
        simbolos.put(label, endereco);
    }

    public boolean contem(String label) {
        return simbolos.containsKey(label);
    }

    public int getEndereco(String label) throws Exception {
        if (!simbolos.containsKey(label)) throw new Exception("Símbolo não definido: " + label);
        return simbolos.get(label);
    }
}
