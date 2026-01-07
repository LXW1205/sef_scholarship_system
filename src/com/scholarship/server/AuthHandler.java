package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class AuthHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
            
            System.out.println("Received login request: " + requestBody);

            // Very basic JSON parsing for prototype (assumes simple structure)
            String username = extractJsonValue(requestBody, "username");
            String password = extractJsonValue(requestBody, "password");

            String response;
            int statusCode;

            if (username != null && password != null) {
                User user = authenticate(username, password);
                if (user != null) {
                     response = String.format("{\"token\": \"fake-jwt-token\", \"user\": {\"username\": \"%s\", \"role\": \"%s\", \"id\": %d}}", 
                             user.username, user.role, user.id);
                     statusCode = 200;
                } else {
                     response = "{\"error\": \"Invalid credentials\"}";
                     statusCode = 401;
                }
            } else {
                response = "{\"error\": \"Invalid request format\"}";
                statusCode = 400;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }
    
    // Helper to verify user against DB
    private User authenticate(String username, String password) {
        String sql = "SELECT userID, username, role FROM \"User\" WHERE (username = ? OR email = ?) AND password = ? AND isActive = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, username); // Support email login too
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("userID"), rs.getString("username"), rs.getString("role"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Auth failed
    }

    // Helper for manual JSON parsing (avoiding external deps for this prototype)
    private String extractJsonValue(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return null;
        
        start += search.length();
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '"')) {
            start++;
        }
        
        int end = json.indexOf("\"", start);
        if (end == -1) end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        
        if (end == -1) return null;
        
        return json.substring(start, end).trim();
    }
    
    // Simple User DTO
    private static class User {
        int id;
        String username;
        String role;
        
        User(int id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }
    }
}
