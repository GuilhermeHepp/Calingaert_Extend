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
                if (opcode.isEmpty()) continue;

                switch (opcode) {
                    case "START":
                    case "END":
                    case "STACK":
                    case "INTDEF":
                    case "INTUSE":
                        continue;

                    case "CONST":
                        int valorConst = Integer.parseInt(parsed.operandos.get(0));
                        String binConst = String.format("%16s", Integer.toBinaryString(valorConst)).replace(' ', '0');
                        lst.add(String.format("%04X %s %s", endereco, binConst, linha));
                        obj.append(binConst);
                        endereco++;
                        break;

                    case "SPACE":
                    case "STOP":
                        lst.add(String.format("%04X 0000000000000000 %s", endereco, linha));
                        obj.append("0000000000000000");
                        endereco++;
                        break;

                    default:
                        int codInstrucao = Util.obterOpcode(opcode);
                        if (codInstrucao == -1) throw new Exception("Instrução inválida");

                        List<String> operandos = parsed.operandos != null ? parsed.operandos : Collections.emptyList();
                        String op1 = operandos.size() > 0 ? operandos.get(0) : null;
                        String op2 = operandos.size() > 1 ? operandos.get(1) : null;


                        boolean imediato = op1 != null && op1.startsWith("#");
                        boolean indireto = op1 != null && op1.endsWith(",I");

                        int instrucao = codInstrucao;
                        if (imediato) instrucao |= 128; // modo imediato
                        else if (indireto) instrucao |= 32; // modo indireto

                        StringBuilder codigo = new StringBuilder();
                        codigo.append(String.format("%16s", Integer.toBinaryString(instrucao)).replace(' ', '0'));

                        if (op1 != null) {
                            op1 = op1.replace("#", "").replace(",I", "");
                            int valor = resolverOperando(op1, tabela);
                            codigo.append(String.format("%16s", Integer.toBinaryString(valor)).replace(' ', '0'));
                        }

                        if (op2 != null) {
                            op2 = op2.replace("#", "").replace(",I", "");
                            int valor = resolverOperando(op2, tabela);
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

    private int resolverOperando(String op, TabelaSimbolos tabela) {
        if (op.startsWith("@")) {
            return Util.parseLiteral(op);
        } else if (Util.eNumero(op)) {
            return Util.parseNumero(op);
        } else if (tabela.contem(op)) {
            return tabela.buscar(op);
        } else {
            throw new RuntimeException("Símbolo não definido: " + op);
        }
    }
}
