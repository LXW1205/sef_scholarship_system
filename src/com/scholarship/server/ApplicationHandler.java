package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class ApplicationHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // In a real app, filtering by user would happen here via token
            String response = getApplicationsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getApplicationsJson() {
        StringBuilder json = new StringBuilder("{\"applications\": [");
        
        String sql = "SELECT a.appID, s.title as scholarship_title, a.submissionDate, a.status " +
                     "FROM Application a " +
                     "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                     "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                
                json.append(String.format("{\"id\": %d, \"scholarship_title\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\"}",
                        rs.getInt("appID"),
                        escape(rs.getString("scholarship_title")),
                        rs.getTimestamp("submissionDate"),
                        rs.getString("status")
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}"; // Return error json
        }
        
        json.append("]}");
        return json.toString();
    }

    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
