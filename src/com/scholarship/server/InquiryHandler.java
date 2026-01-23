package com.scholarship.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.scholarship.dao.InquiryDAO;
import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Inquiry;
import com.scholarship.utils.JsonUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class InquiryHandler implements HttpHandler {

    private final InquiryDAO inquiryDAO = new InquiryDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        // CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            // Verify Authentication
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String[] userAuth = AuthHandler.verifyToken(authHeader);
            if (userAuth == null) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }

            int userId = Integer.parseInt(userAuth[0]);
            String role = userAuth[1];

            if (method.equalsIgnoreCase("GET")) {
                handleGet(exchange, path, userId, role);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePost(exchange, path, userId, role);
            } else {
                sendError(exchange, 405, "Method not allowed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, String path, int userId, String role)
            throws SQLException, IOException {
        List<Map<String, Object>> responseList;

        if (role.equalsIgnoreCase("Admin")) {
            responseList = inquiryDAO.findAllWithDetails();
        } else if (role.equalsIgnoreCase("Student")) {
            String studentID = getStudentID(userId);
            if (studentID == null) {
                sendError(exchange, 400, "Student record not found for user ID: " + userId);
                return;
            }
            responseList = inquiryDAO.findByStudentIdWithDetails(studentID);
        } else {
            sendError(exchange, 403, "Access denied. Only Admins and Students can view inquiries.");
            return;
        }

        String jsonResponse = JsonUtils.toJson(responseList);
        sendResponse(exchange, 200, jsonResponse);
    }

    private void handlePost(HttpExchange exchange, String path, int userId, String role)
            throws IOException, SQLException {
        String[] pathParts = path.split("/");

        if (pathParts.length == 3) {
            // POST /api/inquiries (Create - Student only)
            if (!role.equalsIgnoreCase("Student")) {
                sendError(exchange, 403, "Only students can submit inquiries.");
                return;
            }
            createInquiry(exchange, userId);
        } else if (pathParts.length == 5 && pathParts[4].equals("answer")) {
            // POST /api/inquiries/{id}/answer (Answer - Admin only)
            if (!role.equalsIgnoreCase("Admin")) {
                sendError(exchange, 403, "Only admins can answer inquiries.");
                return;
            }
            int inquiryId = Integer.parseInt(pathParts[3]);
            answerInquiry(exchange, inquiryId);
        } else {
            sendError(exchange, 404, "Endpoint not found");
        }
    }

    private void createInquiry(HttpExchange exchange, int userId) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.parseBody(exchange.getRequestBody());
        String message = body.get("message");

        if (message == null || message.trim().isEmpty()) {
            sendError(exchange, 400, "Message is required");
            return;
        }

        String studentID = getStudentID(userId);
        if (studentID == null) {
            sendError(exchange, 400, "Student record not found.");
            return;
        }

        Inquiry inquiry = new Inquiry(0, studentID, message, null); // ID auto-gen
        if (inquiryDAO.create(inquiry)) {
            sendResponse(exchange, 201, "{\"message\": \"Inquiry submitted successfully\"}");
        } else {
            sendError(exchange, 500, "Failed to submit inquiry");
        }
    }

    private void answerInquiry(HttpExchange exchange, int inquiryId) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.parseBody(exchange.getRequestBody());
        String answer = body.get("answer");

        if (answer == null || answer.trim().isEmpty()) {
            sendError(exchange, 400, "Answer is required");
            return;
        }

        System.out.println("[DEBUG] Answering Inquiry ID: " + inquiryId);
        if (inquiryDAO.updateAnswer(inquiryId, answer)) {
            System.out.println("[DEBUG] Inquiry answered successfully");
            sendResponse(exchange, 200, "{\"message\": \"Inquiry answered successfully\"}");
        } else {
            System.out.println("[ERROR] Inquiry not found or update failed: " + inquiryId);
            sendError(exchange, 404, "Inquiry not found");
        }
    }

    private String getStudentID(int userId) throws SQLException {
        // Can be moved to UserDAO/StudentDAO eventually
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT studentID FROM Student WHERE userID = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("studentID");
            }
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String jsonResponse = "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
        sendResponse(exchange, statusCode, jsonResponse);
    }
}
