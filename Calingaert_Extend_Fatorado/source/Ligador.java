import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class Ligador {
    private TabelaGlobalSimbolos tabelaGlobal = new TabelaGlobalSimbolos();
    private List<Modulo> modulos = new ArrayList<>();

    public void ligarArquivos(String[] arquivos, String nomeSaida, boolean absoluto) throws IOException {
        int enderecoAtual = 0;

        // Passagem 1: leitura e tabela de símbolos globais
        for (String nomeArquivo : arquivos) {
            Modulo modulo = Modulo.lerDeArquivo(nomeArquivo);
            modulo.setEnderecoBase(enderecoAtual);
            modulos.add(modulo);

            for (Map.Entry<String, Integer> simbolo : modulo.getSimbolosDefinidos().entrySet()) {
                String nome = simbolo.getKey();
                int valor = absoluto ? simbolo.getValue() + enderecoAtual : simbolo.getValue();

                if (!tabelaGlobal.adicionarSimbolo(nome, valor)) {
                    throw new IOException("Erro: símbolo global redefinido -> " + nome);
                }
            }

            enderecoAtual += modulo.getCodigo().size();
        }

        // Passagem 2: geração do HPX
        List<String> hpx = new ArrayList<>();
        for (Modulo modulo : modulos) {
            int base = absoluto ? modulo.getEnderecoBase() : 0;
            for (int i = 0; i < modulo.getCodigo().size(); i++) {
                String linha = modulo.getCodigo().get(i);
                linha = substituirSimbolosGlobais(linha, modulo, base);
                hpx.add(linha);
            }
        }

        Files.write(Paths.get(nomeSaida), hpx);
    }

    private String substituirSimbolosGlobais(String linha, Modulo modulo, int base) throws IOException {
        for (String ref : modulo.getSimbolosUsados()) {
            if (linha.contains(ref)) {
                Integer endereco = tabelaGlobal.buscar(ref);
                if (endereco == null) {
                    throw new IOException("Erro: símbolo global não definido -> " + ref);
                }
                linha = linha.replace(ref, Integer.toString(endereco + base));
            }
        }
        return linha;
    }
}
