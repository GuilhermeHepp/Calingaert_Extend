import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Carregador {
    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.out.println("Uso: java Carregador <arquivo.hpx> <saida.txt>");
            return;
        }

        String entrada = args[0];
        String saida = args[1];

        // Lê todas as linhas do arquivo .HPX
        List<String> linhas = Files.readAllLines(Paths.get(entrada));
        StringBuilder saidaBinaria = new StringBuilder();

        // Concatena todos os bits das linhas sem espaços ou quebras
        for (String linha : linhas) {
            linha = linha.trim();
            if (!linha.isEmpty()) {
                saidaBinaria.append(linha);
            }
        }

        // Escreve o resultado em um arquivo .txt
        Files.write(Paths.get(saida), saidaBinaria.toString().getBytes());
        System.out.println("Arquivo gerado com sucesso: " + saida);
    }
}
