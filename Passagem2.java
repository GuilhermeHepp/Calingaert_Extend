import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Passagem2 {

public void executar(List<String> linhas, TabelaSimbolos tabela, String nomeArquivo) {
    List<String> lst = new ArrayList<>();
    StringBuilder obj = new StringBuilder();
    int endereco = 0;
    boolean erro = false;

    for (int i = 0; i < linhas.size(); i++) {
        String linha = linhas.get(i).trim();
        if (linha.isEmpty() || linha.startsWith("*")) continue;

        LinhaAsm parsed = LinhaAsm.parse(linha);
        String opcode = parsed.opcode != null ? parsed.opcode.toUpperCase() : "";

        try {
            if (opcode.isEmpty()) {
                // Linha sem opcode (talvez só label), ignora
                continue;
            }

            switch (opcode) {
                case "START":
                case "END":
                case "STACK":
                case "INTDEF":
                case "INTUSE":
                    // Diretivas que não geram código binário
                    continue;

                case "CONST":
                    int valorConst = Integer.parseInt(parsed.operandos.get(0));
                    lst.add(String.format("%04X %16s %s", endereco,
                            String.format("%16s", Integer.toBinaryString(valorConst)).replace(' ', '0'),
                            linha));
                    obj.append(String.format("%16s", Integer.toBinaryString(valorConst)).replace(' ', '0'));
                    endereco += 1;
                    break;

                case "SPACE":
                case "STOP":
                    lst.add(String.format("%04X %16s %s", endereco,
                            "0000000000000000", linha));
                    obj.append("0000000000000000");
                    endereco += 1;
                    break;

                default:
                    int codInstrucao = Util.obterOpcode(opcode);
                    if (codInstrucao == -1) throw new Exception("Instrução inválida");

                    int instrucao = codInstrucao;
                    String op1 = !parsed.operandos.isEmpty() ? parsed.operandos.get(0) : null;
                    String op2 = parsed.operandos.size() > 1 ? parsed.operandos.get(1) : null;

                    boolean imediato = op1 != null && op1.startsWith("#");
                    boolean indireto = op1 != null && op1.endsWith(",I");

                    if (imediato) instrucao |= 128;
                    else if (indireto) instrucao |= 32;

                    StringBuilder codigo = new StringBuilder();
                    codigo.append(String.format("%16s", Integer.toBinaryString(instrucao)).replace(' ', '0'));

                    if (op1 != null) {
                        op1 = op1.replace("#", "").replace(",I", "");
                        int valor = tabela.getEndereco(op1);
                        codigo.append(String.format("%16s", Integer.toBinaryString(valor)).replace(' ', '0'));
                    }

                    if (op2 != null) {
                        op2 = op2.replace("#", "").replace(",I", "");
                        int valor = tabela.getEndereco(op2);
                        codigo.append(String.format("%16s", Integer.toBinaryString(valor)).replace(' ', '0'));
                    }

                    lst.add(String.format("%04X %s %s", endereco, codigo.toString(), linha));
                    obj.append(codigo.toString());
                    endereco += codigo.length() / 16;
                    break;
            }
        } catch (Exception e) {
            lst.add(String.format("%04X ERRO: %s -> %s", endereco, e.getMessage(), linha));
            erro = true;
        }
    }

    lst.add(erro ? "Montagem finalizada com ERROS." : "Nenhum erro detectado.");

    try {
        Files.write(Paths.get(nomeArquivo.replace(".ASM", ".LST")), lst);
        Files.write(Paths.get(nomeArquivo.replace(".ASM", ".OBJ")), Collections.singleton(obj.toString()));
    } catch (IOException e) {
        System.err.println("Erro ao escrever saída: " + e.getMessage());
    }
}
}

