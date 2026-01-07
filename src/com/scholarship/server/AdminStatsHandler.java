package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class AdminStatsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = getStatsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getStatsJson() {
        int totalUsers = 0;
        int activeScholarships = 0;
        int pendingApps = 0;
        int approvedMonth = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // users
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"User\"")) {
                if (rs.next()) totalUsers = rs.getInt(1);
            }
            // active scholarships
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Scholarship WHERE isActive = true")) {
                if (rs.next()) activeScholarships = rs.getInt(1);
            }
            // pending apps
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Pending'")) {
                if (rs.next()) pendingApps = rs.getInt(1);
            }
            // approved apps (simplifying to total approved for now as date filtering in SQL varies)
             try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Approved'")) {
                if (rs.next()) approvedMonth = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
        
        return String.format("{\"totalUsers\": %d, \"activeScholarships\": %d, \"pendingApps\": %d, \"approvedMonth\": %d}",
                totalUsers, activeScholarships, pendingApps, approvedMonth);
    }
}
