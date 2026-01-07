package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = getScholarshipsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getScholarshipsJson() {
        // Simple manual JSON construction for prototype to avoid dependencies
        StringBuilder json = new StringBuilder("{\"scholarships\": [");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM Scholarship WHERE isActive = true ORDER BY deadline")) {
            
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                
                json.append(String.format("{\"id\": %d, \"title\": \"%s\", \"deadline\": \"%s\", \"status\": \"%s\"}",
                        rs.getInt("scholarshipID"),
                        escape(rs.getString("title")),
                        rs.getDate("deadline"),
                        rs.getBoolean("isActive") ? "Active" : "Closed"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private String escape(String s) {
        if (s == null) return "";
        return s.replace("\"", "\\\"");
    }
}
