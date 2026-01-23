package com.scholarship;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.server.AuthHandler;
import com.scholarship.server.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {

        // Run database migrations
        com.scholarship.db.DatabaseMigration.run();

        // Test DB Connection
        DatabaseConnection.testConnection();

        // Init Scheduled Tasks (Deadline Notifications)
        com.scholarship.server.SystemHandler.initScheduledTasks();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // serve static files
        server.createContext("/", new StaticFileHandler("www"));

        // API endpoints
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/auth/register", new com.scholarship.server.RegisterHandler());
        server.createContext("/api/auth/forgot-password", new com.scholarship.server.ForgotPasswordHandler());
        server.createContext("/api/auth/reset-password", new com.scholarship.server.ResetPasswordHandler());
        server.createContext("/api/scholarships", new com.scholarship.server.ScholarshipHandler());
        server.createContext("/api/applications", new com.scholarship.server.ApplicationHandler());
        server.createContext("/api/users", new com.scholarship.server.UserHandler());
        server.createContext("/api/admin/stats", new com.scholarship.server.AdminStatsHandler());
        server.createContext("/api/notifications", new com.scholarship.server.NotificationHandler());
        server.createContext("/api/system", new com.scholarship.server.SystemHandler());

        // New Handlers
        server.createContext("/api/inquiries", new com.scholarship.server.InquiryHandler());
        server.createContext("/api/clarifications", new com.scholarship.server.ClarificationHandler());

        server.setExecutor(null); // creates a default executor
        server.start();

        System.out.println("Server started on port " + port);
        System.out.println("Open http://localhost:" + port + "/index.html in your browser");
    }
}
