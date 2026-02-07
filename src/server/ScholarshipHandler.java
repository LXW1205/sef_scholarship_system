package server;

import dao.ScholarshipDAO;
import model.Scholarship;
import model.Criterion;
import utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
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
                String query = exchange.getRequestURI().getQuery();
                boolean all = query != null && query.contains("all=true");
                String studentId = null;
                if (query != null && query.contains("eligibleFor=")) {
                    for (String param : query.split("&")) {
                        if (param.startsWith("eligibleFor=")) {
                            studentId = param.split("=")[1];
                            break;
                        }
                    }
                }
                response = getScholarshipsJson(all, studentId);
                if (response.contains("\"error\"")) {
                    statusCode = 500;
                }
            }

            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
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
            dao.AuditLogDAO.log(null, "Admin", "Scholarship Deleted", "Scholarship",
                    String.valueOf(id), "Scholarship ID: " + id + " deleted", null);
            sendResponse(exchange, 200, "{\"success\": true}");
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
            String eligibility = JsonUtils.extractValue(requestBody, "forQualification");
            String minCGPAStr = JsonUtils.extractValue(requestBody, "minCGPA");
            String maxIncomeStr = JsonUtils.extractValue(requestBody, "maxFamilyIncome");

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
            s.setForQualification(eligibility);
            // Validate Deadline
            java.sql.Date deadline = java.sql.Date.valueOf(deadlineStr);
            if (deadline.toLocalDate().isBefore(java.time.LocalDate.now())) {
                sendError(exchange, 400, "Deadline cannot be in the past");
                return;
            }
            s.setDeadline(deadline);

            s.setMinCGPA(minCGPAStr != null ? Double.parseDouble(minCGPAStr) : 0.0);
            s.setMaxFamilyIncome(maxIncomeStr != null ? Double.parseDouble(maxIncomeStr) : 0.0);
            s.setActive("Open".equalsIgnoreCase(status) || "Active".equalsIgnoreCase(status));

            // Parse Criteria from JSON array manually
            List<Criterion> criteriaList = new java.util.ArrayList<>();
            String criteriaJson = JsonUtils.extractValue(requestBody, "criteria");
            if (criteriaJson != null && criteriaJson.startsWith("[") && criteriaJson.endsWith("]")) {
                // Remove [ and ]
                String inner = criteriaJson.substring(1, criteriaJson.length() - 1).trim();
                if (!inner.isEmpty()) {
                    // Split by objects - this is a crude split, but should work for our simple
                    // objects
                    String[] objects = inner.split("\\},\\s*\\{");
                    for (String obj : objects) {
                        if (!obj.startsWith("{"))
                            obj = "{" + obj;
                        if (!obj.endsWith("}"))
                            obj = obj + "}";

                        String cName = JsonUtils.extractValue(obj, "name");
                        String cWeight = JsonUtils.extractValue(obj, "weightage");
                        String cMax = JsonUtils.extractValue(obj, "maxScore");
                        String cMap = JsonUtils.extractValue(obj, "mappedField");

                        if (cName != null) {
                            Criterion c = new Criterion();
                            c.setName(cName);
                            c.setWeightage(cWeight != null ? Integer.parseInt(cWeight) : 0);
                            // Default max score to 100 if not provided
                            c.setMaxScore(cMax != null ? Double.parseDouble(cMax) : 100.0);
                            c.setMappedField(cMap != null ? cMap : "none");
                            criteriaList.add(c);
                        }
                    }
                }
            }
            s.setCriteria(criteriaList);

            boolean success;
            if (id > 0) {
                success = scholarshipDAO.update(s);
                if (success) {
                    dao.AuditLogDAO.log(null, "Admin", "Scholarship Updated", "Scholarship",
                            String.valueOf(id), "Scholarship '" + title + "' (ID: " + id + ") updated", null);
                }
            } else {
                int newId = scholarshipDAO.create(s);
                success = newId > 0;
                if (success) {
                    dao.AuditLogDAO.log(null, "Admin", "Scholarship Created", "Scholarship",
                            String.valueOf(newId), "New scholarship '" + title + "' created", null);
                }
            }

            if (success) {
                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 500, "Failed to save scholarship");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
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
                        "{\"id\": %d, \"name\": \"%s\", \"weightage\": %d, \"maxscore\": %.2f, \"mappedField\": \"%s\"}",
                        c.getCriteriaID(),
                        JsonUtils.escape(c.getName()),
                        c.getWeightage(),
                        c.getMaxScore(),
                        JsonUtils.escape(c.getMappedField())));
            }
            criteriaJson.append("]");

            return String.format(
                    "{\"scholarship\": {\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": \"%s\", \"forQualification\": \"%s\", \"deadline\": \"%s\", \"minCGPA\": %.2f, \"maxFamilyIncome\": %.2f, \"isactive\": %b, \"applicant_count\": 0}, \"criteria\": %s}",
                    s.getScholarshipID(),
                    JsonUtils.escape(s.getTitle()),
                    JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                    JsonUtils.escape(s.getAmount()),
                    JsonUtils.escape(s.getForQualification() != null ? s.getForQualification() : ""),
                    s.getDeadline(),
                    s.getMinCGPA(),
                    s.getMaxFamilyIncome(),
                    s.isActive(),
                    criteriaJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String getScholarshipsJson(boolean all, String studentId) {
        StringBuilder json = new StringBuilder("{\"scholarships\": [");

        try {
            List<Scholarship> scholarships = all ? scholarshipDAO.findAll() : scholarshipDAO.findAllActive();

            // Filter by eligibility if studentId is provided
            if (studentId != null) {
                dao.UserDAO userDAO = new dao.UserDAO();
                model.User user = userDAO.findUserByRoleID(studentId, "Student");
                if (user instanceof model.Student) {
                    model.Student student = (model.Student) user;
                    scholarships.removeIf(s -> !isEligible(student, s));
                }
            }

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
                            "{\"id\": %d, \"name\": \"%s\", \"weightage\": %d, \"maxscore\": %.2f, \"mappedField\": \"%s\"}",
                            c.getCriteriaID(),
                            JsonUtils.escape(c.getName()),
                            c.getWeightage(),
                            c.getMaxScore(),
                            JsonUtils.escape(c.getMappedField())));
                }
                criteriaJson.append("]");

                json.append(String.format(
                        "{\"id\": %d, \"title\": \"%s\", \"description\": \"%s\", \"amount\": \"%s\", \"forQualification\": \"%s\", \"deadline\": \"%s\", \"minCGPA\": %.2f, \"maxFamilyIncome\": %.2f, \"status\": \"%s\", \"criteria\": %s}",
                        s.getScholarshipID(),
                        JsonUtils.escape(s.getTitle()),
                        JsonUtils.escape(s.getDescription() != null ? s.getDescription() : ""),
                        JsonUtils.escape(s.getAmount()),
                        JsonUtils.escape(s.getForQualification() != null ? s.getForQualification() : ""),
                        s.getDeadline(),
                        s.getMinCGPA(),
                        s.getMaxFamilyIncome(),
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

    public static boolean isEligible(model.Student student, Scholarship scholarship) {
        // 1. Check Qualification
        String reqQual = scholarship.getForQualification();
        String studQual = student.getQualification();

        if (reqQual != null && !reqQual.equalsIgnoreCase("Any") && !reqQual.equalsIgnoreCase("All")
                && !reqQual.isEmpty()) {

            // Normalize for comparison
            String reqNorm = reqQual.equalsIgnoreCase("Bachelor") ? "Degree" : reqQual;
            String studNorm = (studQual != null && studQual.equalsIgnoreCase("Bachelor")) ? "Degree" : studQual;

            if (studNorm == null || !studNorm.equalsIgnoreCase(reqNorm)) {
                return false;
            }
        }

        // 2. Check CGPA
        if (student.getCgpa() < scholarship.getMinCGPA()) {
            return false;
        }

        // 3. Check Family Income
        // If scholarship.maxFamilyIncome is 0, we assume no income limit
        if (scholarship.getMaxFamilyIncome() > 0 && student.getFamilyIncome() > scholarship.getMaxFamilyIncome()) {
            return false;
        }

        return true;
    }
}
