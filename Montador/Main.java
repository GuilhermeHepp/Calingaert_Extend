public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Main <arquivo.asm>");
            return;
        }

        String original = args[0];
        String expandido = "MASMAPRG.ASM";

        // Etapa 1: Processador de macros
        MacroProcessor.processar(original, expandido);

        // Etapa 2: Montador
        Montador montador = new Montador();
        montador.montar(expandido);
    }
}
