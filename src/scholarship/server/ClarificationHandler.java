package scholarship.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import scholarship.dao.ClarificationDAO;
import scholarship.db.DatabaseConnection;
import scholarship.model.ClarificationRequest;
import scholarship.utils.JsonUtils;
import scholarship.utils.StatusValidator;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.List;
import java.util.Map;

public class ClarificationHandler implements HttpHandler {

    private final ClarificationDAO clarificationDAO = new ClarificationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        if (method.equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        try {
            String authHeader = exchange.getRequestHeaders().getFirst("Authorization");
            String[] userAuth = AuthHandler.verifyToken(authHeader);
            if (userAuth == null) {
                sendError(exchange, 401, "Unauthorized");
                return;
            }

            int userId = Integer.parseInt(userAuth[0]);
            String role = userAuth[1];

            if (method.equalsIgnoreCase("GET")) {
                handleGet(exchange, userId, role);
            } else if (method.equalsIgnoreCase("POST")) {
                handlePost(exchange, path, userId, role);
            } else {
                sendError(exchange, 405, "Method not allowed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    private void handleGet(HttpExchange exchange, int userId, String role) throws SQLException, IOException {
        List<Map<String, Object>> requests;

        if (role.equalsIgnoreCase("Reviewer")) {
            String reviewerID = getReviewerID(userId);
            if (reviewerID == null) {
                sendError(exchange, 403, "Reviewer profile not found.");
                return;
            }
            requests = clarificationDAO.getRequestsForReviewer(reviewerID);

        } else if (role.equalsIgnoreCase("Committee") || role.equalsIgnoreCase("CommitteeMember")) {
            requests = clarificationDAO.getAllRequestsForCommittee();
        } else {
            sendError(exchange, 403, "Access denied.");
            return;
        }

        sendResponse(exchange, 200, JsonUtils.toJson(requests));
    }

    private void handlePost(HttpExchange exchange, String path, int userId, String role)
            throws IOException, SQLException {
        // POST /api/clarifications (Create - Committee)
        // POST /api/clarifications/{id}/answer (Answer - Reviewer)

        String[] pathParts = path.split("/");

        if (pathParts.length == 3) {
            // Create Request
            if (!role.equalsIgnoreCase("Committee") && !role.equalsIgnoreCase("CommitteeMember")) {
                sendError(exchange, 403, "Only Committee can request clarification.");
                return;
            }
            createRequest(exchange);
        } else if (pathParts.length == 5 && pathParts[4].equals("answer")) {
            // Answer Request
            if (!role.equalsIgnoreCase("Reviewer")) {
                sendError(exchange, 403, "Only Reviewers can answer clarification requests.");
                return;
            }
            int reqId = Integer.parseInt(pathParts[3]);
            answerRequest(exchange, reqId, userId);
        } else {
            sendError(exchange, 404, "Endpoint not found");
        }
    }

    private void createRequest(HttpExchange exchange) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.parseBody(exchange.getRequestBody());
        String evalIDStr = body.get("evalID");
        String question = body.get("question");

        if (evalIDStr == null || question == null || question.trim().isEmpty()) {
            sendError(exchange, 400, "evalID and question are required");
            return;
        }

        int evalID = Integer.parseInt(evalIDStr);

        ClarificationRequest req = new ClarificationRequest(0, evalID, question, null, "Pending", null, null);

        if (clarificationDAO.create(req)) {
            scholarship.dao.AuditLogDAO.log(null, "Committee", "Clarification Requested", "Clarification",
                    String.valueOf(evalID), "Committee requested clarification for Evaluation #" + evalID, null);
            sendResponse(exchange, 201, "{\"message\": \"Clarification requested successfully\"}");
        } else {
            sendError(exchange, 500, "Failed to request clarification");
        }
    }

    private void answerRequest(HttpExchange exchange, int reqId, int userId) throws IOException, SQLException {
        Map<String, String> body = JsonUtils.parseBody(exchange.getRequestBody());
        String answer = body.get("answer");

        if (answer == null || answer.trim().isEmpty()) {
            sendError(exchange, 400, "Answer is required");
            return;
        }

        String reviewerID = getReviewerID(userId);
        if (reviewerID == null) {
            sendError(exchange, 403, "Reviewer profile not found.");
            return;
        }

        // Check ownership
        if (!clarificationDAO.isReviewerOwner(reqId, reviewerID)) {
            sendError(exchange, 403, "You can only answer requests on your own evaluations.");
            return;
        }

        // Check current status
        String currentStatus = getClarificationStatus(reqId);
        if (!StatusValidator.isValidTransition(currentStatus, StatusValidator.CLAR_ANSWERED, "clarification")) {
            sendError(exchange, 400, "Invalid status transition from " + currentStatus + " to Answered");
            return;
        }

        if (clarificationDAO.updateAnswer(reqId, answer)) {
            scholarship.dao.AuditLogDAO.log(userId, reviewerID, "Clarification Answered", "Clarification",
                    String.valueOf(reqId), "Reviewer " + reviewerID + " answered clarification request #" + reqId,
                    null);
            sendResponse(exchange, 200, "{\"message\": \"Clarification answered successfully\"}");
        } else {
            sendError(exchange, 404, "Request not found");
        }
    }

    private String getReviewerID(int userId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT reviewerID FROM Reviewer WHERE userID = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("reviewerID");
            }
        }
        return null;
    }

    private String getClarificationStatus(int reqId) throws SQLException {
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT status FROM ClarificationRequest WHERE reqID = ?")) {
            stmt.setInt(1, reqId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next())
                    return rs.getString("status");
            }
        }
        return null;
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
        String jsonResponse = "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}";
        sendResponse(exchange, statusCode, jsonResponse);
    }
}
