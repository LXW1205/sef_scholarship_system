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
    private com.scholarship.dao.NotificationDAO notificationDAO = new com.scholarship.dao.NotificationDAO();
    private com.scholarship.dao.UserDAO userDAO = new com.scholarship.dao.UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            String studentId = null;
            String reviewerId = null;
            String idStr = null;
            if (query != null) {
                if (query.contains("studentId=")) {
                    studentId = query.split("studentId=")[1].split("&")[0];
                }
                if (query.contains("reviewerId=")) {
                    reviewerId = query.split("reviewerId=")[1].split("&")[0];
                }
                if (query.contains("id=")) {
                    idStr = query.split("id=")[1].split("&")[0];
                }
            }

            String response;
            if (idStr != null) {
                try {
                    int appId = Integer.parseInt(idStr);
                    response = getApplicationJsonById(appId);
                } catch (NumberFormatException e) {
                    response = "{\"error\": \"Invalid application ID\"}";
                }
            } else if (studentId != null) {
                response = getApplicationsJsonByStudentId(studentId);
            } else if (reviewerId != null) {
                response = getApplicationsJsonByReviewerId(reviewerId);
            } else {
                response = getApplicationsJson();
            }

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
            }
            // Check if this is a "submit evaluation" request (has academicScore/comments)
            else if (requestBody.contains("\"comments\"") && requestBody.contains("\"recommendation\"")
                    && requestBody.contains("\"appId\"")) {
                handleEvaluate(exchange, requestBody);
            }
            // Check if this is a "make decision" request (has decision/comments)
            else if (requestBody.contains("\"decision\"") && requestBody.contains("\"appId\"")) {
                handleDecision(exchange, requestBody);
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
            // Extract studentID and scholarshipID from request (handle casing variations)
            String studentID = JsonUtils.extractValue(requestBody, "studentId");
            if (studentID == null)
                studentID = JsonUtils.extractValue(requestBody, "studentID");

            String sIdStr = JsonUtils.extractValue(requestBody, "scholarshipID");
            if (sIdStr == null)
                sIdStr = JsonUtils.extractValue(requestBody, "scholarshipId");
            if (sIdStr == null)
                sIdStr = JsonUtils.extractValue(requestBody, "id");

            int scholarshipID = (sIdStr != null && !sIdStr.equals("null")) ? Integer.parseInt(sIdStr.trim()) : 0;

            if (studentID == null || studentID.isEmpty() || scholarshipID <= 0) {
                System.out.println(
                        "[ERROR] Validation failed: studentID=" + studentID + ", scholarshipID=" + scholarshipID);
                sendError(exchange, 400, "Invalid student ID or scholarship ID");
                return;
            }

            // Fetch scholarship and student to verify eligibility
            com.scholarship.dao.ScholarshipDAO sDAO = new com.scholarship.dao.ScholarshipDAO();
            com.scholarship.model.Scholarship scholarship = sDAO.findById(scholarshipID);
            com.scholarship.model.Student student = (com.scholarship.model.Student) userDAO.findUserByRoleID(studentID,
                    "Student");

            if (scholarship == null || student == null) {
                sendError(exchange, 404, "Scholarship or Student not found");
                return;
            }

            // Perform hard eligibility check
            if (!ScholarshipHandler.isEligible(student, scholarship)) {
                sendError(exchange, 403, "You are not eligible for this scholarship.");
                return;
            }

            Application app = new Application(0, studentID, scholarshipID, "", null, "Pending");

            app.setPersonalStatement(JsonUtils.extractValue(requestBody, "essay"));
            app.setOtherScholarships(JsonUtils.extractValue(requestBody, "otherScholarships"));

            // Parse documents (extraction for the names and contents)
            if (requestBody.contains("\"documents\":")) {
                String docsPart = requestBody.substring(requestBody.indexOf("\"documents\":"));
                String transcript = JsonUtils.extractValue(docsPart, "transcript");
                String transcriptContent = JsonUtils.extractValue(docsPart, "transcriptContent");
                String recommendation = JsonUtils.extractValue(docsPart, "recommendation");
                String recommendationContent = JsonUtils.extractValue(docsPart, "recommendationContent");
                String idCard = JsonUtils.extractValue(docsPart, "id");
                String idContent = JsonUtils.extractValue(docsPart, "idContent");

                if (transcript != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, transcript, "Transcript",
                            transcriptContent, null));
                if (recommendation != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, recommendation, "Recommendation",
                            recommendationContent, null));
                if (idCard != null)
                    app.addDocument(new com.scholarship.model.Document(0, 0, idCard, "ID", idContent, null));
            }

            int appId = applicationDAO.save(app);
            if (appId > 0) {
                System.out.println("[DEBUG] Application SAVED successfully with ID: " + appId);

                // NOTIFY ADMIN
                notificationDAO.createForRole("Admin", "New scholarship application submitted by " + studentID);

                String response = String.format(
                        "{\"success\": true, \"message\": \"Application submitted successfully\", \"application\": {\"appid\": %d}}",
                        appId);
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
                    // UPDATE STATUS
                    applicationDAO.updateStatus(appId, "Reviewing");

                    // NOTIFY REVIEWER
                    com.scholarship.model.User reviewer = userDAO.verifyUserForReset(reviewerId, "", "Reviewer"); // Helper-ish
                                                                                                                  // lookup
                    if (reviewer != null) {
                        notificationDAO.create(new com.scholarship.model.Notification(0, reviewer.getId(),
                                "You have been assigned a new application (ID: " + appId + ") for review.", null,
                                false));
                    }

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

    private void handleEvaluate(HttpExchange exchange, String requestBody) throws IOException {
        System.out.println("[DEBUG] Handling Evaluate...");
        try {
            System.out.println("[DEBUG] Request Body: " + requestBody);
            int appId = Integer.parseInt(JsonUtils.extractValue(requestBody, "appId"));
            String comments = JsonUtils.extractValue(requestBody, "comments");
            String recommendation = JsonUtils.extractValue(requestBody, "recommendation");
            String scoresJson = JsonUtils.extractValue(requestBody, "scores"); // Expecting array string

            System.out.println(
                    "[DEBUG] appId: " + appId + ", comments: " + comments + ", recommendation: " + recommendation);
            System.out.println("[DEBUG] scoresJson: " + scoresJson);

            Evaluation eval = evaluationDAO.findByAppId(appId);
            if (eval != null) {
                eval.setScholarshipComments(comments);
                eval.setStatus(recommendation); // Using recommendation as status for simplicity
                if (evaluationDAO.update(eval)) {

                    // Parse and save scores
                    if (scoresJson != null && scoresJson.startsWith("[")) {
                        List<com.scholarship.model.EvaluationScore> scoresList = new java.util.ArrayList<>();
                        // Simple parser for objects in array
                        // Remove [ and ]
                        String clean = scoresJson.replaceAll("^\\[|\\]$", "");
                        if (!clean.isEmpty()) {
                            // Split by "}," which separates objects (assuming no nested objects or strings
                            // containing "},")
                            // This is a naive split but sufficient for this specific JSON structure
                            String[] scoreObjs = clean.split("\\},");
                            for (String obj : scoreObjs) {
                                // Add closing brace back if missing (split eats it except for last one maybe)
                                if (!obj.trim().endsWith("}"))
                                    obj = obj + "}";

                                String cIdStr = JsonUtils.extractValue(obj, "criteriaID");
                                String sStr = JsonUtils.extractValue(obj, "score");

                                if (cIdStr != null && sStr != null) {
                                    int cId = Integer.parseInt(cIdStr);
                                    double sc = Double.parseDouble(sStr);
                                    scoresList.add(
                                            new com.scholarship.model.EvaluationScore(0, eval.getEvalID(), cId, sc));
                                }
                            }
                        }
                        System.out.println(
                                "[DEBUG] Saving " + scoresList.size() + " scores for evalID: " + eval.getEvalID());
                        evaluationDAO.saveScores(eval.getEvalID(), scoresList);
                    }

                    applicationDAO.updateStatus(appId, "Reviewed");
                    notificationDAO.createForRole("Committee", "New evaluation submitted for Application ID: " + appId);
                    sendResponse(exchange, 200, "{\"success\": true}");
                } else {
                    sendError(exchange, 500, "Failed to update evaluation");
                }
            } else {
                sendError(exchange, 404, "Evaluation record not found");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Print stack trace for debugging
            sendError(exchange, 500, "Error submitting evaluation: " + e.getMessage());
        }
    }

    private void handleDecision(HttpExchange exchange, String requestBody) throws IOException {
        System.out.println("[DEBUG] Handling Decision...");
        try {
            int appId = Integer.parseInt(JsonUtils.extractValue(requestBody, "appId"));
            String decision = JsonUtils.extractValue(requestBody, "decision");
            String comments = JsonUtils.extractValue(requestBody, "comments");

            if (applicationDAO.updateStatus(appId, decision)) {
                // NOTIFY STUDENT
                Application app = applicationDAO.findById(appId); // Need findById
                if (app != null) {
                    // Find userID for studentID
                    com.scholarship.model.User student = userDAO.verifyUserForReset(app.getStudentID(), "", "Student");
                    if (student != null) {
                        String msg = "A decision has been made on your application: " + decision;
                        if (comments != null && !comments.isEmpty()) {
                            msg += ". Comments: " + comments;
                        }
                        notificationDAO
                                .create(new com.scholarship.model.Notification(0, student.getId(), msg, null, false));
                    }
                }
                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 500, "Failed to update application status");
            }
        } catch (Exception e) {
            sendError(exchange, 500, "Error submitting decision: " + e.getMessage());
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
        return formatApplicationsJson(applicationDAO.findAll());
    }

    private String getApplicationsJsonByStudentId(String studentId) {
        return formatApplicationsJson(applicationDAO.findByStudentID(studentId));
    }

    private String getApplicationsJsonByReviewerId(String reviewerId) {
        return formatApplicationsJson(applicationDAO.findByReviewerID(reviewerId));
    }

    private String formatApplicationsJson(List<Application> apps) {
        StringBuilder json = new StringBuilder("{\"applications\": [");
        try {
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

                StringBuilder docsJson = new StringBuilder("[");
                List<com.scholarship.model.Document> docs = a.getDocuments();
                for (int i = 0; i < docs.size(); i++) {
                    com.scholarship.model.Document d = docs.get(i);
                    docsJson.append(String.format(
                            "{\"docID\": %d, \"fileName\": \"%s\", \"fileType\": \"%s\", \"fileContent\": \"%s\"}",
                            d.getDocID(), JsonUtils.escape(d.getFileName()), JsonUtils.escape(d.getFileType()),
                            JsonUtils.escape(d.getFileContent() != null ? d.getFileContent() : "")));
                    if (i < docs.size() - 1)
                        docsJson.append(",");
                }
                docsJson.append("]");

                // Add evaluation details if available
                String evalInfo = "";
                Evaluation eval = evaluationDAO.findByAppId(a.getAppID());
                if (eval != null) {
                    StringBuilder scoresJson = new StringBuilder("[");
                    List<com.scholarship.model.EvaluationScore> scores = evaluationDAO
                            .findScoresByEvalId(eval.getEvalID());
                    for (int i = 0; i < scores.size(); i++) {
                        com.scholarship.model.EvaluationScore s = scores.get(i);
                        scoresJson.append(String.format("{\"criteriaName\": \"%s\", \"score\": %.2f}",
                                JsonUtils.escape(s.getCriteriaName()), s.getScore()));
                        if (i < scores.size() - 1)
                            scoresJson.append(",");
                    }
                    scoresJson.append("]");

                    evalInfo = String.format(
                            ", \"evaluation\": {\"evalID\": %d, \"comments\": \"%s\", \"recommendation\": \"%s\", \"scores\": %s}",
                            eval.getEvalID(),
                            JsonUtils.escape(eval.getScholarshipComments()),
                            JsonUtils.escape(eval.getStatus()),
                            scoresJson.toString());
                }

                json.append(String.format(
                        "{\"applicationid\": %d, \"id\": %d, \"scholarship_title\": \"%s\", \"fullname\": \"%s\", \"email\": \"%s\", \"cgpa\": %.2f, \"major\": \"%s\", \"yearOfStudy\": \"%s\", \"studentid\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\", \"personalStatement\": \"%s\", \"otherScholarships\": \"%s\", \"documents\": %s%s%s}",
                        a.getAppID(),
                        a.getAppID(),
                        JsonUtils.escape(a.getScholarshipTitle()),
                        JsonUtils.escape(a.getApplicantName()),
                        JsonUtils.escape(a.getApplicantEmail() != null ? a.getApplicantEmail() : ""),
                        a.getCgpa(),
                        JsonUtils.escape(a.getMajor() != null ? a.getMajor() : ""),
                        JsonUtils.escape(a.getYearOfStudy() != null ? a.getYearOfStudy() : ""),
                        JsonUtils.escape(a.getStudentID()),
                        a.getSubmissionDate(),
                        JsonUtils.escape(a.getStatus()),
                        JsonUtils.escape(a.getPersonalStatement() != null ? a.getPersonalStatement() : ""),
                        JsonUtils.escape(a.getOtherScholarships() != null ? a.getOtherScholarships() : ""),
                        docsJson.toString(),
                        reviewerInfo,
                        evalInfo));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
        json.append("]}");
        return json.toString();
    }

    private String getApplicationJsonById(int appId) {
        Application a = applicationDAO.findById(appId);
        if (a == null)
            return "{\"error\": \"Application not found\"}";

        // Get scholarship details for criteria
        com.scholarship.dao.ScholarshipDAO sDAO = new com.scholarship.dao.ScholarshipDAO();
        com.scholarship.model.Scholarship s = sDAO.findById(a.getScholarshipID());

        StringBuilder json = new StringBuilder();
        json.append(formatApplicationJsonSingle(a, s));
        return json.toString();
    }

    // Helper to format a single application nicely including criteria
    private String formatApplicationJsonSingle(Application a, com.scholarship.model.Scholarship s) {
        String reviewerInfo = "";
        if (a.getReviewerID() != null) {
            reviewerInfo = String.format(", \"reviewerId\": \"%s\", \"reviewerName\": \"%s\"",
                    JsonUtils.escape(a.getReviewerID()), JsonUtils.escape(a.getReviewerName()));
        }

        StringBuilder docsJson = new StringBuilder("[");
        List<com.scholarship.model.Document> docs = a.getDocuments();
        for (int i = 0; i < docs.size(); i++) {
            com.scholarship.model.Document d = docs.get(i);
            docsJson.append(String.format(
                    "{\"docID\": %d, \"fileName\": \"%s\", \"fileType\": \"%s\", \"fileContent\": \"%s\"}",
                    d.getDocID(), JsonUtils.escape(d.getFileName()), JsonUtils.escape(d.getFileType()),
                    JsonUtils.escape(d.getFileContent() != null ? d.getFileContent() : "")));
            if (i < docs.size() - 1)
                docsJson.append(",");
        }
        docsJson.append("]");

        StringBuilder criteriaJson = new StringBuilder("[");
        if (s != null) {
            boolean first = true;
            for (com.scholarship.model.Criterion c : s.getCriteria()) {
                if (!first)
                    criteriaJson.append(",");
                first = false;
                criteriaJson.append(String.format(
                        "{\"id\": %d, \"name\": \"%s\", \"weightage\": %d, \"maxscore\": %.2f, \"mappedField\": \"%s\"}",
                        c.getCriteriaID(),
                        JsonUtils.escape(c.getName()),
                        c.getWeightage(),
                        c.getMaxScore(),
                        JsonUtils.escape(c.getMappedField())));
            }
        }
        criteriaJson.append("]");

        // Add evaluation details if available
        String evalInfo = "";
        Evaluation eval = evaluationDAO.findByAppId(a.getAppID());
        if (eval != null) {
            StringBuilder scoresJsonStr = new StringBuilder("[");
            List<com.scholarship.model.EvaluationScore> scores = evaluationDAO.findScoresByEvalId(eval.getEvalID());
            for (int i = 0; i < scores.size(); i++) {
                com.scholarship.model.EvaluationScore sc = scores.get(i);
                scoresJsonStr.append(String.format("{\"criteriaID\": %d, \"criteriaName\": \"%s\", \"score\": %.2f}",
                        sc.getCriteriaID(), JsonUtils.escape(sc.getCriteriaName()), sc.getScore()));
                if (i < scores.size() - 1)
                    scoresJsonStr.append(",");
            }
            scoresJsonStr.append("]");

            evalInfo = String.format(
                    ", \"evaluation\": {\"comments\": \"%s\", \"recommendation\": \"%s\", \"scores\": %s}",
                    JsonUtils.escape(eval.getScholarshipComments()),
                    JsonUtils.escape(eval.getStatus()),
                    scoresJsonStr.toString());
        }

        return String.format(
                "{\"applicationid\": %d, \"id\": %d, \"scholarship_title\": \"%s\", \"scholarshipID\": %d, \"fullname\": \"%s\", \"email\": \"%s\", \"cgpa\": %.2f, \"major\": \"%s\", \"yearOfStudy\": \"%s\", \"studentid\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\", \"personalStatement\": \"%s\", \"otherScholarships\": \"%s\", \"documents\": %s, \"criteria\": %s%s%s}",
                a.getAppID(),
                a.getAppID(),
                JsonUtils.escape(a.getScholarshipTitle()),
                a.getScholarshipID(),
                JsonUtils.escape(a.getApplicantName()),
                JsonUtils.escape(a.getApplicantEmail() != null ? a.getApplicantEmail() : ""),
                a.getCgpa(),
                JsonUtils.escape(a.getMajor() != null ? a.getMajor() : ""),
                JsonUtils.escape(a.getYearOfStudy() != null ? a.getYearOfStudy() : ""),
                JsonUtils.escape(a.getStudentID()),
                a.getSubmissionDate(),
                JsonUtils.escape(a.getStatus()),
                JsonUtils.escape(a.getPersonalStatement() != null ? a.getPersonalStatement() : ""),
                JsonUtils.escape(a.getOtherScholarships() != null ? a.getOtherScholarships() : ""),
                docsJson.toString(),
                criteriaJson.toString(),
                reviewerInfo,
                evalInfo);
    }
}
