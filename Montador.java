import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Montador {
    public void montar(String arquivoEntrada) {
        try {
            List<String> linhas = Files.readAllLines(Paths.get(arquivoEntrada));
            Passagem1 p1 = new Passagem1();
            TabelaSimbolos tabela = p1.executar(linhas);

            Passagem2 p2 = new Passagem2();
            p2.executar(linhas, tabela, arquivoEntrada);

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo: " + e.getMessage());
        }
    }
}

