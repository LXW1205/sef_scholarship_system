package com.scholarship.server;

import com.scholarship.dao.UserDAO;
import com.scholarship.model.*;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class AuthHandler implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
                requestBody = reader.lines().collect(Collectors.joining("\n"));
            }

            String email = JsonUtils.extractValue(requestBody, "email");
            String password = JsonUtils.extractValue(requestBody, "password");

            String response;
            int statusCode;

            if (email != null && password != null) {
                User user = userDAO.authenticate(email, password);
                if (user != null) {
                    response = String.format(
                            "{\"token\": \"fake-jwt-token\", \"user\": {\"fullName\": \"%s\", \"role\": \"%s\", \"id\": %d}}",
                            user.getFullName(), user.getRole(), user.getId());
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
}
