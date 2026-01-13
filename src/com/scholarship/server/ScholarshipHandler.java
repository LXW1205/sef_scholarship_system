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
        } else {
            exchange.sendResponseHeaders(405, -1);
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
                    "{\"scholarship\": {\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": %.2f, \"deadline\": \"%s\", \"isactive\": %b, \"applicant_count\": 0}, \"criteria\": %s}",
                    s.getScholarshipID(),
                    JsonUtils.escape(s.getTitle()),
                    JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                    s.getAmount(),
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
                        "{\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": %.2f, \"deadline\": \"%s\", \"status\": \"%s\", \"criteria\": %s}",
                        s.getScholarshipID(),
                        JsonUtils.escape(s.getTitle()),
                        JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                        s.getAmount(),
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
