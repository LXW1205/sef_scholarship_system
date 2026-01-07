package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class UserHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // In a real app, verify Admin role here
            String response = getUsersJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getUsersJson() {
        StringBuilder json = new StringBuilder("{\"users\": [");
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT userID, username, email, firstName, lastName, role FROM \"User\" ORDER BY userID")) {
            
            boolean first = true;
            while (rs.next()) {
                if (!first) json.append(",");
                first = false;
                
                String fullName = (rs.getString("firstName") != null ? rs.getString("firstName") : "") + " " + 
                                  (rs.getString("lastName") != null ? rs.getString("lastName") : "");
                fullName = fullName.trim();
                if (fullName.isEmpty()) fullName = rs.getString("username");

                json.append(String.format("{\"id\": %d, \"username\": \"%s\", \"email\": \"%s\", \"name\": \"%s\", \"role\": \"%s\", \"isActive\": true}",
                        rs.getInt("userID"),
                        escape(rs.getString("username")),
                        escape(rs.getString("email")),
                        escape(fullName),
                        escape(rs.getString("role"))
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
