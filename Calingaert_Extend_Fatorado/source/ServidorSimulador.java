import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ServidorSimulador {

    // Assumindo que o arquivo de entrada principal se chama 'main.asm'
    private static final String ARQUIVO_FONTE_PRINCIPAL = "assembly/main.asm";
    private static final String ARQUIVO_HTML_SIMULADOR = "web/maquina.html"; // Mude se o nome for outro
    private static final String ARQUIVO_SAIDA_TXT = "data/programa.txt";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        // Rota para servir o arquivo HTML principal
        server.createContext("/", new RootHandler());
        
        // Rota para compilar o código e retornar o binário
        server.createContext("/compile", new CompileHandler());

        // Rota para baixar o arquivo .txt gerado
        server.createContext("/download", new DownloadHandler());

        server.setExecutor(null); // usa o executor padrão
        server.start();
        System.out.println("Servidor iniciado na porta 8080.");
        System.out.println("Acesse http://localhost:8080 para abrir o simulador.");
    }

    static class RootHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Path htmlPath = Paths.get(ARQUIVO_HTML_SIMULADOR);
            byte[] response = Files.readAllBytes(htmlPath);
            
            t.sendResponseHeaders(200, response.length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(response);
            }
        }
    }

    static class CompileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            try {
                // --- Executa toda a cadeia de compilação ---
                
                // 1. Processador de Macros
                String arquivoExpandido = "assembly/MASMAPRG.ASM";
                MacroProcessor.processar(ARQUIVO_FONTE_PRINCIPAL, arquivoExpandido);

                // 2. Montador
                Montador montador = new Montador();
                montador.montar(arquivoExpandido);
                
                // 3. Ligador (assumindo que o .OBJ gerado tem o mesmo nome base)
                String arquivoObj = arquivoExpandido.replace(".ASM", ".OBJ");
                Ligador ligador = new Ligador();
                ligador.ligarArquivos(new String[]{arquivoObj}, "data/programa.HPX", true);

                // 4. Carregador
                List<String> linhasHpx = Files.readAllLines(Paths.get("data/programa.HPX"));
                StringBuilder saidaBinaria = new StringBuilder();
                for (String linha : linhasHpx) {
                    saidaBinaria.append(linha.trim());
                }
                
                String response = saidaBinaria.toString();

                // NOVO: Salva o arquivo .txt no servidor antes de enviar a resposta
                Files.write(Paths.get(ARQUIVO_SAIDA_TXT), response.getBytes());

                // --- Envia a resposta para o navegador ---
                t.getResponseHeaders().set("Content-Type", "text/plain");
                t.sendResponseHeaders(200, response.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(response.getBytes());
                }

            } catch (Exception e) {
                e.printStackTrace();
                String errorMessage = "Erro de compilação: " + e.getMessage();
                t.getResponseHeaders().set("Content-Type", "text/plain");
                t.sendResponseHeaders(500, errorMessage.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(errorMessage.getBytes());
                }
            }
        }
    }

    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            Path filePath = Paths.get(ARQUIVO_SAIDA_TXT);
            if (!Files.exists(filePath)) {
                String errorMessage = "Arquivo nao encontrado. Compile o codigo primeiro.";
                t.sendResponseHeaders(404, errorMessage.length());
                try (OutputStream os = t.getResponseBody()) {
                    os.write(errorMessage.getBytes());
                }
                return;
            }

            t.getResponseHeaders().set("Content-Type", "text/plain");
            t.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + ARQUIVO_SAIDA_TXT + "\"");
            t.sendResponseHeaders(200, Files.size(filePath));
            try (OutputStream os = t.getResponseBody()) {
                Files.copy(filePath, os);
            }
        }
    }
}