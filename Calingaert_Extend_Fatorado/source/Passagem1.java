import java.util.List;

public class Passagem1 {
    
    public TabelaSimbolos executar(List<String> linhas) {
        TabelaSimbolos tabela = new TabelaSimbolos();
        int endereco = 0;
        

        for (String linha : linhas) {
            linha = linha.trim();
            if (linha.isEmpty() || linha.startsWith("*")) continue;

            if (!linha.matches("[\\w\\s,@'#H\\d\\+\\-]*")) {
    throw new RuntimeException("Erro: caracter inválido em -> " + linha);}
            if (linha.length() > 80) {
                throw new RuntimeException("Erro: linha muito longa -> " + linha);
            }  

            LinhaAsm parsed = LinhaAsm.parse(linha);
            if (parsed.label != null) {
                if (tabela.contem(parsed.label)) {
                    System.err.println("Erro: símbolo redefinido -> " + parsed.label);
                } else {
                    tabela.adicionar(parsed.label, endereco);
                }
            }

            endereco += calcularTamanhoInstrucao(parsed);
        }

        return tabela;
    }

    private int calcularTamanhoInstrucao(LinhaAsm linha) {
    if (linha.opcode == null) {
        return 0; // ou lançar exceção, dependendo do caso
    }
    return switch (linha.opcode.toUpperCase()) {
        case "COPY" -> 3;
        case "STOP", "RET" -> 1;
        case "SPACE", "CONST" -> 1;
        case "STACK", "START", "END", "INTDEF", "INTUSE" -> 0;
        default -> 2;
    };
}

}

