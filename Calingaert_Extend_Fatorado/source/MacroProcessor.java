import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MacroProcessor {
    public static void processar(String entrada, String saida) {
        try {
            List<String> linhas = Files.readAllLines(Paths.get(entrada));
            MacroTable macroTable = new MacroTable();
            List<String> saidaExpandida = new ArrayList<>();

            boolean dentroDefMacro = false;
            Macro macroAtual = null;

            for (String linhaOriginal : linhas) {
                String linha = linhaOriginal.trim();
                if (linha.isEmpty() || linha.startsWith("*")) {
                    if (!dentroDefMacro) saidaExpandida.add(linhaOriginal);
                    continue;
                }

                String[] partes = linha.split("\\s+", 2);
                String instrucao = partes[0];

                if (instrucao.equalsIgnoreCase("MACRO")) {
                    dentroDefMacro = true;
                    continue;
                }

                if (instrucao.equalsIgnoreCase("MEND")) {
                    if (macroAtual != null) {
                        macroTable.adicionarMacro(macroAtual);
                        macroAtual = null;
                    }
                    dentroDefMacro = false;
                    continue;
                }

                if (dentroDefMacro) {
                    if (macroAtual == null) {
                        macroAtual = Macro.fromDefinicao(linha);
                    } else {
                        macroAtual.adicionarLinha(linha);
                    }
                } else {
                    String macroName = linha.split("\\s+")[0];
                    if (macroTable.existeMacro(macroName)) {
                        List<String> expandido = macroTable.expandirMacro(linha);
                        saidaExpandida.addAll(expandido);
                    } else {
                        saidaExpandida.add(linhaOriginal);
                    }
                }
            }

            Files.write(Paths.get(saida), saidaExpandida);
            System.out.println("Macro processamento concluído. Saída gerada em: " + saida);

        } catch (IOException e) {
            System.err.println("Erro ao processar macros: " + e.getMessage());
        }
    }
}

