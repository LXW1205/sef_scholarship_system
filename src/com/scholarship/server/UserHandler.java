package com.scholarship.server;

import com.scholarship.dao.AuditLogDAO;
import com.scholarship.dao.UserDAO;
import com.scholarship.model.*;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class UserHandler implements HttpHandler {

    private final UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // In a real app, verify Admin role here
            String query = exchange.getRequestURI().getQuery();
            String roleFilter = null;
            String idFilter = null;

            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        if (pair[0].equals("role")) {
                            roleFilter = pair[1];
                        } else if (pair[0].equals("id")) {
                            idFilter = pair[1];
                        }
                    }
                }
            }

            if (idFilter != null) {
                try {
                    int id = Integer.parseInt(idFilter);
                    User user = userDAO.findById(id);
                    if (user != null) {
                        sendResponse(exchange, 200, getUserJson(user));
                    } else {
                        sendError(exchange, 404, "User not found");
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "Invalid ID format");
                    return;
                }
            } else {
                sendResponse(exchange, 200, getUsersJson(roleFilter));
            }
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else if ("DELETE".equals(exchange.getRequestMethod())) {
            handleDelete(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();
        String query = exchange.getRequestURI().getQuery();
        int id = -1;
        if (query != null && query.startsWith("id=")) {
            try {
                id = Integer.parseInt(query.split("=")[1]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (id != -1) {
            // Get user info before deletion for logging
            User userToDelete = userDAO.findById(id);
            if (userToDelete != null && userDAO.delete(id)) {
                // Log the deletion
                AuditLogDAO.log(null, "Admin", "User Deleted", "User", String.valueOf(id),
                        "Deleted user: " + userToDelete.getFullName() + " (" + userToDelete.getEmail() + ")", clientIP);

                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 400, "User not found or failed to delete");
            }
        } else {
            sendError(exchange, 400, "Invalid ID");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());

        try {
            int id = Integer.parseInt(JsonUtils.extractValue(requestBody, "id"));
            User existingUser = userDAO.findById(id);
            if (existingUser == null) {
                sendError(exchange, 404, "User not found");
                return;
            }

            String fullName = JsonUtils.extractValue(requestBody, "fullName");
            if (fullName == null || fullName.isEmpty()) {
                fullName = existingUser.getFullName();
            }

            String password = JsonUtils.extractValue(requestBody, "password");

            // Fix: Extract isActive status properly or preserve existing
            String isActiveStr = JsonUtils.extractValue(requestBody, "isActive");
            boolean isActive = (isActiveStr != null) ? "true".equalsIgnoreCase(isActiveStr) : existingUser.isActive();

            // Create a temp user object for updating
            User user;
            String role = JsonUtils.extractValue(requestBody, "role"); // Need role to know if Student
            if (role == null) {
                role = existingUser.getRole();
            }

            if ("Student".equals(role)) {
                Student s = new Student();
                s.setId(id);
                s.setFullName(fullName);
                s.setActive(isActive);
                s.setCgpa(Double.parseDouble(JsonUtils.extractValue(requestBody, "cgpa")));
                s.setMajor(JsonUtils.extractValue(requestBody, "major"));
                s.setQualification(JsonUtils.extractValue(requestBody, "qualification"));
                s.setYearOfStudy(JsonUtils.extractValue(requestBody, "yearOfStudy"));
                s.setExpectedGraduation(JsonUtils.extractValue(requestBody, "expectedGraduation"));
                s.setFamilyIncome(Double.parseDouble(JsonUtils.extractValue(requestBody, "familyIncome")));
                user = s;
            } else if ("Reviewer".equals(role)) {
                Reviewer r = new Reviewer();
                r.setId(id);
                r.setFullName(fullName);
                r.setActive(isActive);
                r.setDepartment(JsonUtils.extractValue(requestBody, "department"));
                user = r;
            } else if ("Committee".equals(role) || "CommitteeMember".equals(role)) {
                CommitteeMember c = new CommitteeMember();
                c.setId(id);
                c.setFullName(fullName);
                c.setActive(isActive);
                c.setPosition(JsonUtils.extractValue(requestBody, "position"));
                user = c;
            } else if ("Admin".equals(role)) {
                Admin a = new Admin();
                a.setId(id);
                a.setFullName(fullName);
                a.setActive(isActive);
                a.setAdminLevel(JsonUtils.extractValue(requestBody, "adminLevel"));
                user = a;
            } else {
                user = new User(id, fullName, "", "", isActive) {
                    public boolean login() {
                        return false;
                    }

                    public void logout() {
                    }
                };
                user.setRole(role);
            }

            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

            if (userDAO.update(user, password)) {
                // Log the update
                AuditLogDAO.log(null, "Admin", "User Updated", "User", String.valueOf(id),
                        "Updated user: " + fullName + " (isActive: " + isActive + ")", clientIP);

                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 500, "Failed to update user");
            }
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error");
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"error\": \"%s\"}", JsonUtils.escape(message));
        sendResponse(exchange, statusCode, response);
    }

    private String getUserJson(User user) {
        String detailedJson = "";
        if (user instanceof Student) {
            Student s = (Student) user;
            detailedJson = String.format(
                    ", \"studentID\": \"%s\", \"cgpa\": %.2f, \"major\": \"%s\", \"qualification\": \"%s\", \"yearOfStudy\": \"%s\", \"expectedGraduation\": \"%s\", \"familyIncome\": %.2f",
                    JsonUtils.escape(s.getStudentID()), s.getCgpa(), JsonUtils.escape(s.getMajor()),
                    JsonUtils.escape(s.getQualification()), JsonUtils.escape(s.getYearOfStudy()),
                    JsonUtils.escape(s.getExpectedGraduation()), s.getFamilyIncome());
        } else if (user instanceof Reviewer) {
            Reviewer r = (Reviewer) user;
            detailedJson = String.format(", \"reviewerID\": \"%s\", \"department\": \"%s\"",
                    JsonUtils.escape(r.getReviewerID()), JsonUtils.escape(r.getDepartment()));
        } else if (user instanceof CommitteeMember) {
            CommitteeMember c = (CommitteeMember) user;
            detailedJson = String.format(", \"committeeID\": \"%s\", \"position\": \"%s\"",
                    JsonUtils.escape(c.getCommitteeID()), JsonUtils.escape(c.getPosition()));
        } else if (user instanceof Admin) {
            Admin a = (Admin) user;
            detailedJson = String.format(", \"adminID\": \"%s\", \"adminLevel\": \"%s\"",
                    JsonUtils.escape(a.getAdminID()), JsonUtils.escape(a.getAdminLevel()));
        }

        return String.format(
                "{\"user\": {\"id\": %d, \"fullName\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"role\": \"%s\", \"isActive\": %b%s}}",
                user.getId(),
                JsonUtils.escape(user.getFullName()),
                JsonUtils.escape(user.getEmail()),
                JsonUtils.escape(user.getPassword()),
                JsonUtils.escape(user.getRole()),
                user.isActive(),
                detailedJson);
    }

    private String getUsersJson(String roleFilter) {
        StringBuilder json = new StringBuilder("{\"users\": [");

        try {
            List<User> users = (roleFilter != null) ? userDAO.findByRole(roleFilter) : userDAO.findAll();

            boolean first = true;
            for (User user : users) {
                if (!first)
                    json.append(",");
                first = false;

                String detailedJson = "";
                if (user instanceof Student) {
                    Student s = (Student) user;
                    detailedJson = String.format(
                            ", \"studentID\": \"%s\", \"cgpa\": %.2f, \"major\": \"%s\", \"qualification\": \"%s\", \"yearOfStudy\": \"%s\", \"expectedGraduation\": \"%s\", \"familyIncome\": %.2f",
                            JsonUtils.escape(s.getStudentID()), s.getCgpa(), JsonUtils.escape(s.getMajor()),
                            JsonUtils.escape(s.getQualification()), JsonUtils.escape(s.getYearOfStudy()),
                            JsonUtils.escape(s.getExpectedGraduation()), s.getFamilyIncome());
                } else if (user instanceof Reviewer) {
                    Reviewer r = (Reviewer) user;
                    detailedJson = String.format(", \"reviewerID\": \"%s\", \"department\": \"%s\"",
                            JsonUtils.escape(r.getReviewerID()), JsonUtils.escape(r.getDepartment()));
                } else if (user instanceof CommitteeMember) {
                    CommitteeMember c = (CommitteeMember) user;
                    detailedJson = String.format(", \"committeeID\": \"%s\", \"position\": \"%s\"",
                            JsonUtils.escape(c.getCommitteeID()), JsonUtils.escape(c.getPosition()));
                } else if (user instanceof Admin) {
                    Admin a = (Admin) user;
                    detailedJson = String.format(", \"adminID\": \"%s\", \"adminLevel\": \"%s\"",
                            JsonUtils.escape(a.getAdminID()), JsonUtils.escape(a.getAdminLevel()));
                }

                json.append(String.format(
                        "{\"id\": %d, \"fullName\": \"%s\", \"email\": \"%s\", \"password\": \"%s\", \"role\": \"%s\", \"isActive\": %b%s}",
                        user.getId(),
                        JsonUtils.escape(user.getFullName()),
                        JsonUtils.escape(user.getEmail()),
                        JsonUtils.escape(user.getPassword()),
                        JsonUtils.escape(user.getRole()),
                        user.isActive(),
                        detailedJson));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }

        json.append("]}");
        return json.toString();
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        byte[] bytes = response.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
