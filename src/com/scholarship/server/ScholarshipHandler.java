package com.scholarship.server;

import com.scholarship.dao.ScholarshipDAO;
import com.scholarship.model.Scholarship;
import com.scholarship.model.Criterion;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ScholarshipHandler implements HttpHandler {

    private ScholarshipDAO scholarshipDAO = new ScholarshipDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String path = exchange.getRequestURI().getPath();
            String response;
            int statusCode = 200;

            if (path.matches("/api/scholarships/\\d+")) {
                int id = Integer.parseInt(path.substring(path.lastIndexOf('/') + 1));
                System.out.println("[DEBUG] Fetching scholarship with ID: " + id);
                response = getSingleScholarshipJson(id);
                System.out.println(
                        "[DEBUG] Response: " + response.substring(0, Math.min(100, response.length())) + "...");
                // Check if response is an error
                if (response.contains("\"error\"")) {
                    statusCode = response.contains("not found") ? 404 : 500;
                }
            } else {
                response = getScholarshipsJson();
                if (response.contains("\"error\"")) {
                    statusCode = 500;
                }
            }

            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
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
        String query = exchange.getRequestURI().getQuery();
        int id = -1;
        if (query != null && query.startsWith("id=")) {
            try {
                id = Integer.parseInt(query.split("=")[1]);
            } catch (NumberFormatException e) {
                // ignore
            }
        }

        if (id != -1 && scholarshipDAO.delete(id)) {
            String response = "{\"success\": true}";
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            sendError(exchange, 400, "Invalid ID or failed to delete");
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        try {
            // Check if update (has ID) or create
            String idStr = JsonUtils.extractValue(requestBody, "id");
            int id = (idStr != null && !idStr.isEmpty()) ? Integer.parseInt(idStr) : 0;

            String title = JsonUtils.extractValue(requestBody, "title");
            String description = JsonUtils.extractValue(requestBody, "description");
            String amount = JsonUtils.extractValue(requestBody, "amount");
            String deadlineStr = JsonUtils.extractValue(requestBody, "deadline");
            String status = JsonUtils.extractValue(requestBody, "status");
            String eligibility = JsonUtils.extractValue(requestBody, "eligibilityCriteria");

            // Basic validation
            if (title == null || title.isEmpty()) {
                sendError(exchange, 400, "Title is required");
                return;
            }

            Scholarship s = new Scholarship();
            s.setScholarshipID(id);
            s.setTitle(title);
            s.setDescription(description);
            s.setAmount(amount);
            s.setForQualification(eligibility); // Using forQualification field for eligibility text
            s.setDeadline(java.sql.Date.valueOf(deadlineStr));
            s.setActive("Open".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status));

            // Note: Criteria parsing would ideally go here if we had a JSON parser library.
            // For now, initializing empty list to avoid NPE
            s.setCriteria(new java.util.ArrayList<>());

            boolean success;
            if (id > 0) {
                success = scholarshipDAO.update(s);
            } else {
                int newId = scholarshipDAO.create(s);
                success = newId > 0;
            }

            if (success) {
                String response = "{\"success\": true}";
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } else {
                sendError(exchange, 500, "Failed to save scholarship");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"error\": \"%s\"}", JsonUtils.escape(message));
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, response.length());
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    private String getSingleScholarshipJson(int id) {
        try {
            Scholarship s = scholarshipDAO.findById(id);

            if (s == null) {
                return "{\"error\": \"Scholarship not found\"}";
            }

            // Build criteria array
            StringBuilder criteriaJson = new StringBuilder("[");
            boolean firstCriteria = true;
            for (Criterion c : s.getCriteria()) {
                if (!firstCriteria)
                    criteriaJson.append(",");
                firstCriteria = false;
                criteriaJson.append(String.format(
                        "{\"id\": %d, \"name\": \"%s\", \"weightage\": %d, \"maxscore\": %.2f}",
                        c.getCriteriaID(),
                        JsonUtils.escape(c.getName()),
                        c.getWeightage(),
                        c.getMaxScore()));
            }
            criteriaJson.append("]");

            return String.format(
                    "{\"scholarship\": {\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": \"%s\", \"forQualification\": \"%s\", \"deadline\": \"%s\", \"isactive\": %b, \"applicant_count\": 0}, \"criteria\": %s}",
                    s.getScholarshipID(),
                    JsonUtils.escape(s.getTitle()),
                    JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                    JsonUtils.escape(s.getAmount()),
                    JsonUtils.escape(s.getForQualification() != null ? s.getForQualification() : ""),
                    s.getDeadline(),
                    s.isActive(),
                    criteriaJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getScholarshipsJson() {
        StringBuilder json = new StringBuilder("{\"scholarships\": [");

        try {
            List<Scholarship> scholarships = scholarshipDAO.findAllActive();

            boolean first = true;
            for (Scholarship s : scholarships) {
                if (!first)
                    json.append(",");
                first = false;

                // Build criteria array
                StringBuilder criteriaJson = new StringBuilder("[");
                boolean firstCriteria = true;
                for (Criterion c : s.getCriteria()) {
                    if (!firstCriteria)
                        criteriaJson.append(",");
                    firstCriteria = false;
                    criteriaJson.append(String.format(
                            "{\"id\": %d, \"name\": \"%s\", \"weightage\": %d, \"maxScore\": %.2f}",
                            c.getCriteriaID(),
                            JsonUtils.escape(c.getName()),
                            c.getWeightage(),
                            c.getMaxScore()));
                }
                criteriaJson.append("]");

                json.append(String.format(
                        "{\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": \"%s\", \"forQualification\": \"%s\", \"deadline\": \"%s\", \"status\": \"%s\", \"criteria\": %s}",
                        s.getScholarshipID(),
                        JsonUtils.escape(s.getTitle()),
                        JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                        JsonUtils.escape(s.getAmount()),
                        JsonUtils.escape(s.getForQualification() != null ? s.getForQualification() : ""),
                        s.getDeadline(),
                        s.isActive() ? "Active" : "Closed",
                        criteriaJson.toString()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }

        json.append("]}");
        return json.toString();
    }
}
