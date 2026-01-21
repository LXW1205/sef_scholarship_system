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

            String email = JsonUtils.extractJsonValue(requestBody, "email");
            if (email != null)
                email = email.toLowerCase();
            String password = JsonUtils.extractJsonValue(requestBody, "password");
            String role = JsonUtils.extractJsonValue(requestBody, "role");
            String fullName = JsonUtils.extractJsonValue(requestBody, "fullName");

            // Extract role-specific fields
            String studentID = JsonUtils.extractJsonValue(requestBody, "studentID");
            String major = JsonUtils.extractJsonValue(requestBody, "major");
            String department = JsonUtils.extractJsonValue(requestBody, "department");
            String position = JsonUtils.extractJsonValue(requestBody, "position");
            String adminLevel = JsonUtils.extractJsonValue(requestBody, "adminLevel");

            // Default to Student if no role specified
            if (role == null || role.isEmpty()) {
                role = "Student";
            }

            // Check if email already exists
            if (emailExists(email)) {
                sendResponse(exchange, 400, "{\"error\": \"Email already exists\"}");
                return;
            }

            // Create user based on role
            int userId = -1;
            if ("Student".equals(role)) {
                userId = createStudent(fullName, email, password, studentID, major);
            } else if ("Reviewer".equals(role)) {
                userId = createReviewer(fullName, email, password, department);
            } else if ("Committee".equals(role) || "CommitteeMember".equals(role)) {
                userId = createCommitteeMember(fullName, email, password, position);
            } else if ("Admin".equals(role)) {
                userId = createAdmin(fullName, email, password, adminLevel);
            } else {
                userId = createUser(fullName, email, password, role);
            }

            if (userId > 0) {
                String response = String.format(
                        "{\"success\": true, \"message\": \"User registered successfully\", \"userId\": %d}", userId);
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 500, "{\"error\": \"Failed to create user\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM \"User\" WHERE email = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

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

    private int createStudent(String fullName, String email, String password, String studentID, String major) {
        if (studentID == null || studentID.isEmpty()) {
            studentID = "S" + System.currentTimeMillis() % 10000000;
        }
        String sql = "INSERT INTO Student (fullName, email, password, role, isActive, studentID, cgpa, major) VALUES (?, ?, ?, 'Student', true, ?, 0.0, ?) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, studentID);
            pstmt.setString(5, major != null ? major : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int createReviewer(String fullName, String email, String password, String department) {
        String reviewerID = "R" + (System.currentTimeMillis() % 10000);
        String sql = "INSERT INTO Reviewer (fullName, email, password, role, isActive, reviewerID, department) VALUES (?, ?, ?, 'Reviewer', true, ?, ?) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, reviewerID);
            pstmt.setString(5, department != null ? department : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int createCommitteeMember(String fullName, String email, String password, String position) {
        String committeeID = "C" + (System.currentTimeMillis() % 10000);
        String sql = "INSERT INTO CommitteeMember (fullName, email, password, role, isActive, committeeID, position) VALUES (?, ?, ?, 'Committee', true, ?, ?) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, committeeID);
            pstmt.setString(5, position != null ? position : "");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int createAdmin(String fullName, String email, String password, String adminLevel) {
        String adminID = "A" + (System.currentTimeMillis() % 10000);
        String sql = "INSERT INTO Admin (fullName, email, password, role, isActive, adminID, adminLevel) VALUES (?, ?, ?, 'Admin', true, ?, ?) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fullName);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, adminID);
            pstmt.setString(5, adminLevel != null ? adminLevel : "Staff");
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next())
                    return rs.getInt(1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private int createUser(String fullName, String email, String password, String role) {
        String sql = "INSERT INTO \"User\" (fullName, email, password, role, isActive) VALUES (?, ?, ?, ?, true) RETURNING userID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fullName);
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
