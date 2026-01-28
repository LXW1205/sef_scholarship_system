package scholarship;

import scholarship.db.DatabaseConnection;
import scholarship.server.AuthHandler;
import scholarship.server.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        // Run database migrations
        // scholarship.db.DatabaseMigration.run();

        // Test DB Connection
        DatabaseConnection.testConnection();

        // Init Scheduled Tasks (Deadline Notifications)
        scholarship.server.SystemHandler.initScheduledTasks();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // serve static files
        server.createContext("/", new StaticFileHandler("www"));

        // API endpoints
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/auth/register", new scholarship.server.RegisterHandler());
        server.createContext("/api/auth/forgot-password", new scholarship.server.ForgotPasswordHandler());
        server.createContext("/api/auth/reset-password", new scholarship.server.ResetPasswordHandler());
        server.createContext("/api/scholarships", new scholarship.server.ScholarshipHandler());
        server.createContext("/api/applications", new scholarship.server.ApplicationHandler());
        server.createContext("/api/users", new scholarship.server.UserHandler());
        server.createContext("/api/admin/stats", new scholarship.server.AdminStatsHandler());
        server.createContext("/api/notifications", new scholarship.server.NotificationHandler());
        server.createContext("/api/system", new scholarship.server.SystemHandler());

        // New Handlers
        server.createContext("/api/inquiries", new scholarship.server.InquiryHandler());
        server.createContext("/api/clarifications", new scholarship.server.ClarificationHandler());
        server.createContext("/api/interviews", new scholarship.server.InterviewHandler());

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started on port " + port);
        System.out.println("Open http://localhost:" + port + "/index.html in your browser");
    }
}
