package com.scholarship.server;

import com.scholarship.dao.AuditLogDAO;
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

            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

            if (email != null && password != null) {
                User user = userDAO.authenticate(email, password);
                if (user != null) {
                    // Log successful login
                    AuditLogDAO.log(user.getId(), email, "User Login", "User", String.valueOf(user.getId()),
                            "Successful login for " + user.getFullName() + " (" + user.getRole() + ")", clientIP);

                    String extra = "";
                    if (user instanceof Student) {
                        extra = String.format(", \"studentID\": \"%s\"", ((Student) user).getStudentID());
                    } else if (user instanceof Reviewer) {
                        extra = String.format(", \"reviewerID\": \"%s\"", ((Reviewer) user).getReviewerID());
                    } else if (user instanceof CommitteeMember) {
                        extra = String.format(", \"committeeID\": \"%s\"", ((CommitteeMember) user).getCommitteeID());
                    } else if (user instanceof Admin) {
                        extra = String.format(", \"adminID\": \"%s\"", ((Admin) user).getAdminID());
                    }
                    response = String.format(
                            "{\"token\": \"fake-jwt-token\", \"user\": {\"fullName\": \"%s\", \"role\": \"%s\", \"id\": %d%s}}",
                            user.getFullName(), user.getRole(), user.getId(), extra);
                    statusCode = 200;
                } else {
                    // Log failed login attempt
                    AuditLogDAO.log(null, email, "Failed Login Attempt", "User", null,
                            "Invalid credentials for email: " + email, clientIP);

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
