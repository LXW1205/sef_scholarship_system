package scholarship.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

public class StaticFileHandler implements HttpHandler {
    private final String rootDir;

    public StaticFileHandler(String rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/")) {
            path = "/index.html";
        }

        File file = new File(rootDir + path);
        if (!file.exists()) {
            // Try adding .html if file not found (forcleaner URLs)
            File htmlFile = new File(rootDir + path + ".html");
            if (htmlFile.exists()) {
                file = htmlFile;
            } else {
                String response = "404 (Not Found)\n";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }
        }

        if (file.isDirectory()) {
             // Handle directory index if needed, or 403
             // For now, let's just 404 or try index.html inside
             File indexFile = new File(file, "index.html");
             if (indexFile.exists()) {
                 file = indexFile;
             } else {
                 String response = "403 (Forbidden)\n";
                 exchange.sendResponseHeaders(403, response.length());
                 try (OutputStream os = exchange.getResponseBody()) {
                     os.write(response.getBytes());
                 }
                 return;
             }
        }

        String mimeType = Files.probeContentType(file.toPath());
        if (path.endsWith(".css")) mimeType = "text/css";
        if (path.endsWith(".js")) mimeType = "application/javascript";
        if (mimeType == null) mimeType = "application/octet-stream";

        exchange.getResponseHeaders().set("Content-Type", mimeType);
        // Disable caching to ensure frontend changes are seen immediately
        exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
        exchange.getResponseHeaders().set("Pragma", "no-cache");
        exchange.getResponseHeaders().set("Expires", "0");
        exchange.sendResponseHeaders(200, file.length());

        try (OutputStream os = exchange.getResponseBody();
             FileInputStream fs = new FileInputStream(file)) {
            final byte[] buffer = new byte[0x10000];
            int count = 0;
            while ((count = fs.read(buffer)) >= 0) {
                os.write(buffer, 0, count);
            }
        }
    }
}
