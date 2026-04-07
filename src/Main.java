// package scholarship; (removed for flattening)

import db.DatabaseConnection;
import server.AuthHandler;
import server.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        // Run database migrations
        // db.DatabaseMigration.run();

        // Test DB Connection
        DatabaseConnection.testConnection();

        // Init Scheduled Tasks (Deadline Notifications)
        server.SystemHandler.initScheduledTasks();

        // Render sets the PORT env var dynamically; default to 8080 for local runs
        String portEnv = System.getenv("PORT");
        int port = (portEnv != null) ? Integer.parseInt(portEnv) : 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // serve static files
        server.createContext("/", new StaticFileHandler("www"));

        // API endpoints
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/auth/register", new server.RegisterHandler());
        server.createContext("/api/auth/forgot-password", new server.ForgotPasswordHandler());
        server.createContext("/api/auth/reset-password", new server.ResetPasswordHandler());
        server.createContext("/api/scholarships", new server.ScholarshipHandler());
        server.createContext("/api/applications", new server.ApplicationHandler());
        server.createContext("/api/users", new server.UserHandler());
        server.createContext("/api/admin/stats", new server.AdminStatsHandler());
        server.createContext("/api/notifications", new server.NotificationHandler());
        server.createContext("/api/system", new server.SystemHandler());

        // New Handlers
        server.createContext("/api/inquiries", new server.InquiryHandler());
        server.createContext("/api/clarifications", new server.ClarificationHandler());
        server.createContext("/api/interviews", new server.InterviewHandler());

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started on port " + port);
        System.out.println("Open http://localhost:" + port + "/index.html in your browser");
    }
}
