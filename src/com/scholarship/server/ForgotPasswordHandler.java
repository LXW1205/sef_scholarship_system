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
            
            String email = JsonUtils.extractJsonValue(requestBody, "email");
            
            // TODO: Generate reset token and store in database
            // TODO: Send email with reset link
            
            // For now, just return success (placeholder implementation)
            String response = "{\"success\": true, \"message\": \"Password reset instructions sent to email\"}";
            sendResponse(exchange, 200, response);
            
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
