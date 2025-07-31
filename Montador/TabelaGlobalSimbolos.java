import java.util.*;

public class TabelaGlobalSimbolos {
    private Map<String, Integer> tabela = new HashMap<>();

    public boolean adicionarSimbolo(String nome, int enderecoAbsoluto) {
        if (tabela.containsKey(nome)) {
            return false; // símbolo já definido
        }
        tabela.put(nome, enderecoAbsoluto);
        return true;
    }

    public Integer buscar(String nome) {
        return tabela.get(nome);
    }

    public boolean contem(String nome) {
        return tabela.containsKey(nome);
    }

    public void imprimir() {
        for (Map.Entry<String, Integer> e : tabela.entrySet()) {
            System.out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}

