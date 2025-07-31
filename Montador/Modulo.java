import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Modulo {
    private List<String> codigo = new ArrayList<>();
    private Map<String, Integer> simbolosDefinidos = new HashMap<>();
    private List<String> simbolosUsados = new ArrayList<>();
    private int enderecoBase;

    public static Modulo lerDeArquivo(String nomeArquivo) throws IOException {
        Modulo modulo = new Modulo();
        List<String> linhas = Files.readAllLines(Paths.get(nomeArquivo));

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.startsWith("DEF")) {
                // Exemplo: DEF NOME 12
                String[] partes = linha.split("\\s+");
                if (partes.length == 3) {
                    String nome = partes[1];
                    int endereco = Integer.parseInt(partes[2]);
                    modulo.simbolosDefinidos.put(nome, endereco);
                }
            } else if (linha.startsWith("USE")) {
                // Exemplo: USE NOME
                String[] partes = linha.split("\\s+");
                if (partes.length == 2) {
                    modulo.simbolosUsados.add(partes[1]);
                }
            } else {
                modulo.codigo.add(linha);
            }
        }
        return modulo;
    }

    public void setEnderecoBase(int base) {
        this.enderecoBase = base;
    }

    public int getEnderecoBase() {
        return enderecoBase;
    }

    public List<String> getCodigo() {
        return codigo;
    }

    public Map<String, Integer> getSimbolosDefinidos() {
        return simbolosDefinidos;
    }

    public List<String> getSimbolosUsados() {
        return simbolosUsados;
    }
}

