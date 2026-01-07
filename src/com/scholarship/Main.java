package com.scholarship;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.server.AuthHandler;
import com.scholarship.server.StaticFileHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        // Test DB Connection
        DatabaseConnection.testConnection();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // serve static files
        server.createContext("/", new StaticFileHandler("www"));
        
        // API endpoints
        server.createContext("/api/auth/login", new AuthHandler());
        server.createContext("/api/scholarships", new com.scholarship.server.ScholarshipHandler());
        server.createContext("/api/applications", new com.scholarship.server.ApplicationHandler());
        server.createContext("/api/users", new com.scholarship.server.UserHandler());
        server.createContext("/api/admin/stats", new com.scholarship.server.AdminStatsHandler());

        server.setExecutor(null); // creates a default executor
        server.start();
        
        System.out.println("Server started on port " + port);
        System.out.println("Open http://localhost:" + port + "/index.html in your browser");
    }
}
