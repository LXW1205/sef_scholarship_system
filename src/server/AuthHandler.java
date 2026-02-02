package server;

import dao.AuditLogDAO;
import dao.UserDAO;
import model.*;
import utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
            // Do not force lowercase, as it might be a case-sensitive ID.
            // UserDAO handles email case-insensitivity.
            String password = JsonUtils.extractValue(requestBody, "password");

            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

            if (email != null && password != null) {
                User user = userDAO.authenticate(email, password);
                if (user != null) {
                    // Log successful login
                    AuditLogDAO.log(user.getId(), email, "User Login", "User", String.valueOf(user.getId()),
                            "Successful login for " + user.getFullName() + " (" + user.getRole() + ")", clientIP);

                    String extra = "";
                    if (user instanceof Student) {
                        Student s = (Student) user;
                        extra = String.format(
                                ", \"studentID\": \"%s\", \"cgpa\": %.2f, \"major\": \"%s\", \"qualification\": \"%s\", \"yearOfStudy\": \"%s\", \"expectedGraduation\": \"%s\", \"familyIncome\": %.2f",
                                JsonUtils.escape(s.getStudentID()), s.getCgpa(), JsonUtils.escape(s.getMajor()),
                                JsonUtils.escape(s.getQualification()), JsonUtils.escape(s.getYearOfStudy()),
                                JsonUtils.escape(s.getExpectedGraduation()), s.getFamilyIncome());
                    } else if (user instanceof Reviewer) {
                        extra = String.format(", \"reviewerID\": \"%s\"", ((Reviewer) user).getReviewerID());
                    } else if (user instanceof CommitteeMember) {
                        extra = String.format(", \"committeeID\": \"%s\"", ((CommitteeMember) user).getCommitteeID());
                    } else if (user instanceof Admin) {
                        extra = String.format(", \"adminID\": \"%s\"", ((Admin) user).getAdminID());
                    }
                    String tokenData = user.getId() + ":" + user.getRole();
                    String token = Base64.getEncoder().encodeToString(tokenData.getBytes(StandardCharsets.UTF_8));

                    String response = String.format(
                            "{\"token\": \"%s\", \"user\": {\"fullName\": \"%s\", \"role\": \"%s\", \"id\": %d%s}}",
                            token, user.getFullName(), user.getRole(), user.getId(), extra);
                    sendResponse(exchange, 200, response);
                } else {
                    // Log failed login attempt
                    AuditLogDAO.log(null, email, "Failed Login Attempt", "User", null,
                            "Invalid credentials for email: " + email, clientIP);

                    sendError(exchange, 401, "Invalid credentials");
                }
            } else {
                sendError(exchange, 400, "Invalid request format");
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
        }
    }

    public static String[] verifyToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7).trim();
        if (token.equals("fake-jwt-token"))
            return null; // Legacy check

        try {
            String decoded = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
            String[] parts = decoded.split(":");
            if (parts.length == 2) {
                return parts; // [userId, role]
            }
        } catch (Exception e) {
            // Invalid token
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
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
