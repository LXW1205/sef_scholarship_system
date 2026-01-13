package com.scholarship.server;

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
            if (query != null && query.contains("role=")) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("role")) {
                        roleFilter = pair[1];
                        break;
                    }
                }
            }

            String response = getUsersJson(roleFilter);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
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
                    detailedJson = String.format(", \"studentID\": \"%s\", \"fullName\": \"%s\", \"cgpa\": %.2f",
                            JsonUtils.escape(s.getStudentID()), JsonUtils.escape(s.getFullName()), s.getCgpa());
                } else if (user instanceof Reviewer) {
                    Reviewer r = (Reviewer) user;
                    detailedJson = String.format(", \"staffID\": \"%s\", \"department\": \"%s\"",
                            JsonUtils.escape(r.getStaffID()), JsonUtils.escape(r.getDepartment()));
                } else if (user instanceof CommitteeMember) {
                    CommitteeMember c = (CommitteeMember) user;
                    detailedJson = String.format(", \"memberID\": %d, \"position\": \"%s\"",
                            c.getMemberID(), JsonUtils.escape(c.getPosition()));
                } else if (user instanceof Admin) {
                    Admin a = (Admin) user;
                    detailedJson = String.format(", \"adminID\": %d, \"adminLevel\": \"%s\"",
                            a.getAdminID(), JsonUtils.escape(a.getAdminLevel()));
                }

                json.append(String.format(
                        "{\"id\": %d, \"username\": \"%s\", \"email\": \"%s\", \"role\": \"%s\", \"isActive\": %b%s}",
                        user.getId(),
                        JsonUtils.escape(user.getUsername()),
                        JsonUtils.escape(user.getEmail()),
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
}
