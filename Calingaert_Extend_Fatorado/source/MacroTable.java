import java.util.*;

public class MacroTable {
    private Map<String, Macro> macros = new HashMap<>();

    public void adicionarMacro(Macro m) {
        macros.put(m.getNome(), m);
    }

    public boolean existeMacro(String nome) {
        return macros.containsKey(nome);
    }

    public List<String> expandirMacro(String chamada) {
        String[] partes = chamada.trim().split("\\s+", 2);
        String nome = partes[0];
        List<String> args = new ArrayList<>();

        if (partes.length > 1) {
            String[] argumentos = partes[1].split(",");
            for (String a : argumentos) args.add(a.trim());
        }

        Macro macro = macros.get(nome);
        return macro.expandir(args);
    }
}
