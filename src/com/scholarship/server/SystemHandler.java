package com.scholarship.server;

import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

import java.util.HashMap;
import java.util.Map;

public class SystemHandler implements HttpHandler {

    // In-memory mock storage for settings
    private static Map<String, String> settings = new HashMap<>();
    private static boolean maintenanceMode = false;

    static {
        settings.put("systemName", "DSX - Digital Scholarship Experience");
        settings.put("supportEmail", "support@dsx.edu");
        settings.put("timezone", "UTC+8 (Singapore Time)");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/backup")) {
            handleBackup(exchange);
        } else if (path.endsWith("/maintenance")) {
            handleMaintenance(exchange);
        } else if (path.endsWith("/settings")) {
            handleSettings(exchange);
        } else {
            sendError(exchange, 404, "Endpoint not found");
        }
    }

    private void handleBackup(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // Mock backup: simulate some work
            Thread.sleep(1000);

            // In a real app, we would dump the DB or zip files here
            // For now, we just return success

            sendResponse(exchange, 200, "{\"success\": true, \"message\": \"Backup created successfully\"}");
        } catch (InterruptedException e) {
            sendError(exchange, 500, "Backup interrupted");
        }
    }

    private void handleMaintenance(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 200, "{\"enabled\": " + maintenanceMode + "}");
        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String enabledStr = JsonUtils.extractValue(requestBody, "enabled");

            if (enabledStr != null) {
                maintenanceMode = Boolean.parseBoolean(enabledStr);
                sendResponse(exchange, 200, "{\"success\": true, \"enabled\": " + maintenanceMode + "}");
            } else {
                sendError(exchange, 400, "Missing 'enabled' field");
            }
        } else {
            sendError(exchange, 405, "Method Not Allowed");
        }
    }

    private void handleSettings(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            StringBuilder json = new StringBuilder("{");
            json.append("\"systemName\": \"").append(JsonUtils.escape(settings.get("systemName"))).append("\",");
            json.append("\"supportEmail\": \"").append(JsonUtils.escape(settings.get("supportEmail"))).append("\",");
            json.append("\"timezone\": \"").append(JsonUtils.escape(settings.get("timezone"))).append("\"");
            json.append("}");
            sendResponse(exchange, 200, json.toString());

        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String name = JsonUtils.extractValue(requestBody, "systemName");
            String email = JsonUtils.extractValue(requestBody, "supportEmail");
            String tz = JsonUtils.extractValue(requestBody, "timezone");

            if (name != null)
                settings.put("systemName", name);
            if (email != null)
                settings.put("supportEmail", email);
            if (tz != null)
                settings.put("timezone", tz);

            sendResponse(exchange, 200, "{\"success\": true}");
        } else {
            sendError(exchange, 405, "Method Not Allowed");
        }
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
        String response = String.format("{\"error\": \"%s\"}", JsonUtils.escape(message));
        sendResponse(exchange, statusCode, response);
    }
}
