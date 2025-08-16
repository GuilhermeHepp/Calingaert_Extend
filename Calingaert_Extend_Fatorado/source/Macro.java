import java.util.*;

public class Macro {
    private String nome;
    private List<String> parametros;
    private List<String> corpo;

    public Macro(String nome, List<String> parametros) {
        this.nome = nome;
        this.parametros = parametros;
        this.corpo = new ArrayList<>();
    }

    public static Macro fromDefinicao(String linha) {
        String[] partes = linha.trim().split("\\s+");
        String nome = partes[0];
        List<String> parametros = new ArrayList<>();

        if (partes.length > 1) {
            String[] rawParams = partes[1].split(",");
            for (String p : rawParams) parametros.add(p.trim());
        }
        return new Macro(nome, parametros);
    }

    public void adicionarLinha(String linha) {
        corpo.add(linha);
    }

    public List<String> expandir(List<String> args) {
        List<String> resultado = new ArrayList<>();
        for (String linha : corpo) {
            String expandida = linha;
            for (int i = 0; i < parametros.size(); i++) {
                expandida = expandida.replace(parametros.get(i), args.get(i));
            }
            resultado.add(expandida);
        }
        return resultado;
    }

    public String getNome() {
        return nome;
    }
}
