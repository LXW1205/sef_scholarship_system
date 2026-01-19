package com.scholarship.server;

import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ForgotPasswordHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            handleForgotPassword(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleForgotPassword(HttpExchange exchange) throws IOException {
        try {
            InputStream is = exchange.getRequestBody();
            String requestBody = new String(is.readAllBytes());

            String userId = JsonUtils.extractValue(requestBody, "id");
            String fullName = JsonUtils.extractValue(requestBody, "fullName");
            String role = JsonUtils.extractValue(requestBody, "role");

            com.scholarship.dao.UserDAO userDAO = new com.scholarship.dao.UserDAO();
            com.scholarship.model.User user = userDAO.verifyUserForReset(userId, fullName, role);

            if (user != null) {
                // Return a "reset token" which is just the user's ID for now
                String response = String.format(
                        "{\"success\": true, \"message\": \"Verification successful\", \"resetToken\": \"%d\"}",
                        user.getId());
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 401,
                        "{\"error\": \"Verification failed: User details do not match our records.\"}");
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
