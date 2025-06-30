public class Main {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Uso: java Main <arquivo.asm>");
            return;
        }

        Montador montador = new Montador();
        montador.montar(args[0]);
    }
}
