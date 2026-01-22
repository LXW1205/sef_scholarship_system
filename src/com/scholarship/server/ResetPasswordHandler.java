package com.scholarship.server;

import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ResetPasswordHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleResetPassword(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleResetPassword(HttpExchange exchange) throws IOException {
        try {
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes());

            String token = JsonUtils.extractJsonValue(requestBody, "token");
            String newPassword = JsonUtils.extractJsonValue(requestBody, "password");

            if (token == null || newPassword == null || token.isEmpty() || newPassword.isEmpty()) {
                sendResponse(exchange, 400, "{\"error\": \"Token and password are required.\"}");
                return;
            }

            // In this implementation, the token is the userID (from ForgotPasswordHandler)
            int userId;
            try {
                userId = Integer.parseInt(token);
            } catch (NumberFormatException e) {
                sendResponse(exchange, 400, "{\"error\": \"Invalid token format.\"}");
                return;
            }

            com.scholarship.dao.UserDAO userDAO = new com.scholarship.dao.UserDAO();
            com.scholarship.model.User user = userDAO.findById(userId);

            if (user != null) {
                boolean success = userDAO.update(user, newPassword);
                if (success) {
                    String response = "{\"success\": true, \"message\": \"Password reset successfully\"}";
                    sendResponse(exchange, 200, response);
                } else {
                    sendResponse(exchange, 500, "{\"error\": \"Failed to update password in database.\"}");
                }
            } else {
                sendResponse(exchange, 404, "{\"error\": \"User not found for the provided token.\"}");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendResponse(exchange, 500, "{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }
}
