package com.scholarship.server;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class RegisterHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleRegister(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleRegister(HttpExchange exchange) throws IOException {
        try {
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes());
            
            String username = JsonUtils.extractJsonValue(requestBody, "username");
            String email = JsonUtils.extractJsonValue(requestBody, "email");
            String password = JsonUtils.extractJsonValue(requestBody, "password");
            String role = JsonUtils.extractJsonValue(requestBody, "role");
            String fullName = JsonUtils.extractJsonValue(requestBody, "fullName");
            
            // Default to Student if no role specified
            if (role == null || role.isEmpty()) {
                role = "Student";
            }
            
            // Check if username or email already exists
            if (userExists(username, email)) {
                sendResponse(exchange, 400, "{\"error\": \"Username or email already exists\"}");
                return;
            }
            
            // Insert user into database
            int userId = createUser(username, email, password, role);
            
            if (userId > 0) {
                String response = String.format("{\"success\": true, \"message\": \"User registered successfully\", \"userId\": %d}", userId);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create user\"}");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    
    private boolean userExists(String username, String email) {
        String sql = "SELECT COUNT(*) FROM \"User\" WHERE username = ? OR email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    private int createUser(String username, String email, String password, String role) {
        String sql = "INSERT INTO \"User\" (username, email, password, role, isActive) VALUES (?, ?, ?, ?, true) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, password); // In production, hash this password!
            pstmt.setString(4, role);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
