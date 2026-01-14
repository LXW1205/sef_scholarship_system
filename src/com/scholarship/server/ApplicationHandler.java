package com.scholarship.server;

import com.scholarship.dao.ApplicationDAO;
import com.scholarship.dao.EvaluationDAO;
import com.scholarship.model.Application;
import com.scholarship.model.Evaluation;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ApplicationHandler implements HttpHandler {

    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private EvaluationDAO evaluationDAO = new EvaluationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // In a real app, filtering by user would happen here via token
            String response = getApplicationsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        System.out.println("\n[DEBUG] ApplicationHandler RECEIVED POST: " + requestBody);

        try {
            // Check if this is a "create application" request (has scholarshipID)
            if (requestBody.contains("\"scholarshipID\"")) {
                handleCreateApplication(exchange, requestBody);
            }
            // Check if this is a "assign reviewer" request (has appId and reviewerId)
            else if (requestBody.contains("\"appId\"") && requestBody.contains("\"reviewerId\"")) {
                handleAssignReviewer(exchange, requestBody);
            } else {
                System.out.println("[ERROR] Unknown POST request format");
                sendError(exchange, 400, "Unknown request format");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Exception in handlePost: " + e.getMessage());
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleCreateApplication(HttpExchange exchange, String requestBody) throws IOException {
        System.out.println("[DEBUG] Handling Create Application...");
        try {
            // Flexible parsing for scholarshipID
            String idStr = JsonUtils.extractValue(requestBody, "scholarshipID");
            int scholarshipID = (idStr != null) ? Integer.parseInt(idStr.trim()) : 0;

            if (scholarshipID <= 0) {
                sendError(exchange, 400, "Invalid scholarship ID");
                return;
            }

            // TODO: In production, extract studentID from JWT token
            // For now, use a valid student ID from sample data (S2024001 for student001)
            String studentID = "S2024001";

            Application app = new Application(0, studentID, scholarshipID, "", null, "Pending");

            // Extract new fields - these are now in the Student table
            // TODO: Update Student table with these details if necessary
            String major = JsonUtils.extractValue(requestBody, "major");
            String gpaStr = JsonUtils.extractValue(requestBody, "gpa");
            String yearOfStudy = JsonUtils.extractValue(requestBody, "yearOfStudy");
            String expectedGraduation = JsonUtils.extractValue(requestBody, "expectedGraduation");
            String incomeStr = JsonUtils.extractValue(requestBody, "familyIncome");

            app.setPersonalStatement(JsonUtils.extractValue(requestBody, "essay"));
            app.setOtherScholarships(JsonUtils.extractValue(requestBody, "otherScholarships"));

            // Parse documents (simple extraction for the names)
            if (requestBody.contains("\"documents\":")) {
                String docsPart = requestBody.substring(requestBody.indexOf("\"documents\":"));
                String transcript = JsonUtils.extractValue(docsPart, "transcript");
                String recommendation = JsonUtils.extractValue(docsPart, "recommendation");
                String idCard = JsonUtils.extractValue(docsPart, "id");

                if (transcript != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, transcript, "Transcript", null));
                if (recommendation != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, recommendation, "Recommendation", null));
                if (idCard != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, idCard, "ID", null));
            }

            if (applicationDAO.save(app)) {
                System.out.println("[DEBUG] Application SAVED successfully");
                String response = "{\"success\": true, \"message\": \"Application submitted successfully\"}";
                sendResponse(exchange, 200, response);
            } else {
                System.out.println("[ERROR] DAO failed to save application");
                sendError(exchange, 500, "Failed to save application. Check if you already applied.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error: " + e.getMessage());
        }
    }

    private void handleAssignReviewer(HttpExchange exchange, String requestBody) throws IOException {
        System.out.println("[DEBUG] Handling Assign Reviewer...");
        try {
            String appIdStr = JsonUtils.extractValue(requestBody, "appId");
            String reviewerId = JsonUtils.extractValue(requestBody, "reviewerId");

            if (appIdStr != null && reviewerId != null) {
                int appId = Integer.parseInt(appIdStr.trim());
                Evaluation eval = new Evaluation(0, appId, reviewerId, "", 0, "", "Pending", null);
                if (evaluationDAO.save(eval)) {
                    sendResponse(exchange, 200, "{\"success\": true}");
                } else {
                    sendError(exchange, 500, "Failed to assign reviewer");
                }
            } else {
                sendError(exchange, 400, "Missing appId or reviewerId");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error assigning reviewer: " + e.getMessage());
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

    private String getApplicationsJson() {
        StringBuilder json = new StringBuilder("{\"applications\": [");

        try {
            // Fetch all applications for Admin view
            List<Application> apps = applicationDAO.findAll();

            boolean first = true;
            for (Application a : apps) {
                if (!first)
                    json.append(",");
                first = false;

                String reviewerInfo = "";
                if (a.getReviewerID() != null) {
                    reviewerInfo = String.format(", \"reviewerId\": \"%s\", \"reviewerName\": \"%s\"",
                            JsonUtils.escape(a.getReviewerID()), JsonUtils.escape(a.getReviewerName()));
                }

                json.append(String.format(
                        "{\"id\": %d, \"scholarship_title\": \"%s\", \"fullname\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\"%s}",
                        a.getAppID(),
                        JsonUtils.escape(a.getScholarshipTitle()),
                        JsonUtils.escape(a.getApplicantName()),
                        a.getSubmissionDate(),
                        JsonUtils.escape(a.getStatus()),
                        reviewerInfo));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}"; // Return error json
        }

        json.append("]}");
        return json.toString();
    }
}
