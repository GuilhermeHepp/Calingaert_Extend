import java.io.*;
import java.util.*;

public class MainLigador {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java MainLigador <abs|rel> <arquivo1.OBJ> <arquivo2.OBJ> ...");
            return;
        }

        String modo = args[0];
        String[] arquivos = Arrays.copyOfRange(args, 1, args.length);
        boolean absoluto = modo.equalsIgnoreCase("abs");

        try {
            Ligador ligador = new Ligador();
            ligador.ligarArquivos(arquivos, "programa.HPX", absoluto);
            System.out.println("Ligação concluída. Arquivo gerado: programa.HPX");
        } catch (IOException e) {
            System.err.println("Erro durante a ligação: " + e.getMessage());
        }
    }
}
