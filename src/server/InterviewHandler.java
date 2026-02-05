package server;

import dao.InterviewDAO;
import dao.EvaluationDAO;
import dao.ApplicationDAO;
import dao.NotificationDAO;
import dao.UserDAO;
import model.Interview;
import model.Application;
import model.Evaluation;
import utils.JsonUtils;
import utils.StatusValidator;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

public class InterviewHandler implements HttpHandler {

    private InterviewDAO interviewDAO = new InterviewDAO();
    private EvaluationDAO evaluationDAO = new EvaluationDAO();
    private ApplicationDAO applicationDAO = new ApplicationDAO();
    private NotificationDAO notificationDAO = new NotificationDAO();
    private UserDAO userDAO = new UserDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        if ("GET".equals(method)) {
            handleGet(exchange);
        } else if ("POST".equals(method)) {
            handlePost(exchange);
        } else if ("DELETE".equals(method)) {
            handleDelete(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String idStr = null;
        String evalIdStr = null;

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("id=")) {
                    idStr = param.substring(3);
                } else if (param.startsWith("evalId=")) {
                    evalIdStr = param.substring(7);
                }
            }
        }

        try {
            if (idStr != null) {
                // Get single interview
                int id = Integer.parseInt(idStr);
                Interview interview = interviewDAO.findById(id);
                if (interview != null) {
                    String json = buildInterviewJson(interview);
                    sendResponse(exchange, 200, json);
                } else {
                    sendError(exchange, 404, "Interview not found");
                }
            } else if (evalIdStr != null) {
                // Get interviews by evaluation
                int evalId = Integer.parseInt(evalIdStr);
                List<Interview> interviews = interviewDAO.findByEvalId(evalId);
                String json = buildInterviewsListJson(interviews);
                sendResponse(exchange, 200, json);
            } else {
                // Get all interviews with rich data
                String json = buildAllInterviewsJson();
                sendResponse(exchange, 200, json);
            }
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Internal server error: " + e.getMessage());
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        System.out.println("[DEBUG] InterviewHandler POST: " + requestBody);

        try {
            String action = JsonUtils.extractValue(requestBody, "action");

            if ("update".equals(action)) {
                handleUpdate(exchange, requestBody);
            } else {
                handleCreate(exchange, requestBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error processing request: " + e.getMessage());
        }
    }

    private void handleCreate(HttpExchange exchange, String requestBody) throws IOException {
        try {
            String evalIdStr = JsonUtils.extractValue(requestBody, "evalId");
            String dateTimeStr = JsonUtils.extractValue(requestBody, "dateTime");
            String venueOrLink = JsonUtils.extractValue(requestBody, "venueOrLink");

            if (evalIdStr == null || dateTimeStr == null || venueOrLink == null) {
                sendError(exchange, 400, "Missing required fields");
                return;
            }

            int evalId = Integer.parseInt(evalIdStr);
            Timestamp dateTime = Timestamp.valueOf(dateTimeStr.replace("T", " ") + ":00");

            if (dateTime.before(new Timestamp(System.currentTimeMillis()))) {
                sendError(exchange, 400, "Interview date cannot be in the past");
                return;
            }

            Interview interview = new Interview(0, evalId, dateTime, venueOrLink, "Scheduled");
            int interviewId = interviewDAO.schedule(interview);

            if (interviewId > 0) {
                // Determine Application ID and Update Status
                int appId = getAppIdFromEvalId(evalId);
                if (appId > 0) {
                    applicationDAO.updateStatus(appId, StatusValidator.APP_INTERVIEWED);
                    System.out.println("[INFO] Application " + appId + " transitioned to Interviewed");
                }

                // Notify student
                Evaluation eval = evaluationDAO.findByAppId(appId);
                if (eval != null) {
                    Application app = applicationDAO.findById(eval.getAppID());
                    if (app != null) {
                        model.User student = userDAO.findUserByRoleID(app.getStudentID(), "Student");
                        if (student != null) {
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String msg = String.format(
                                    "An interview has been scheduled for your application '%s' on %s. Venue/Link: %s",
                                    app.getScholarshipTitle(),
                                    sdf.format(dateTime),
                                    venueOrLink);
                            notificationDAO.create(
                                    new model.Notification(0, student.getId(), msg, null, false));
                        }
                    }
                }

                // Audit log
                dao.AuditLogDAO.log(null, "Committee", "Interview Scheduled", "Interview",
                        String.valueOf(interviewId), "Interview scheduled for evalID: " + evalId, null);

                sendResponse(exchange, 200, String.format("{\"success\": true, \"interviewId\": %d}", interviewId));
            } else {
                sendError(exchange, 500, "Failed to schedule interview");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error creating interview: " + e.getMessage());
        }
    }

    private void handleUpdate(HttpExchange exchange, String requestBody) throws IOException {
        try {
            String idStr = JsonUtils.extractValue(requestBody, "id");
            String dateTimeStr = JsonUtils.extractValue(requestBody, "dateTime");
            String venueOrLink = JsonUtils.extractValue(requestBody, "venueOrLink");
            String status = JsonUtils.extractValue(requestBody, "status");

            if (idStr == null) {
                sendError(exchange, 400, "Missing interview ID");
                return;
            }

            int id = Integer.parseInt(idStr);
            Interview interview = interviewDAO.findById(id);

            if (interview == null) {
                sendError(exchange, 404, "Interview not found");
                return;
            }

            if (dateTimeStr != null) {
                Timestamp newDateTime = Timestamp.valueOf(dateTimeStr.replace("T", " ") + ":00");
                if ("Scheduled".equals(status != null ? status : interview.getStatus())
                        && newDateTime.before(new Timestamp(System.currentTimeMillis()))) {
                    sendError(exchange, 400, "Interview date cannot be in the past");
                    return;
                }
                interview.setDateTime(newDateTime);
            }
            if (venueOrLink != null) {
                interview.setVenueOrLink(venueOrLink);
            }
            if (status != null) {
                if (!StatusValidator.isValidTransition(interview.getStatus(), status, "interview")) {
                    sendError(exchange, 400,
                            "Invalid status transition from " + interview.getStatus() + " to " + status);
                    return;
                }
                interview.setStatus(status);
            }

            if (interviewDAO.update(interview)) {
                // Notify student of update
                Evaluation eval = evaluationDAO.findByAppId(getAppIdFromEvalId(interview.getEvalID()));
                if (eval != null) {
                    Application app = applicationDAO.findById(eval.getAppID());
                    if (app != null) {
                        model.User student = userDAO.findUserByRoleID(app.getStudentID(), "Student");
                        if (student != null) {
                            String msg = String.format(
                                    "Your interview for '%s' has been updated. Please check your interviews page for details.",
                                    app.getScholarshipTitle());
                            notificationDAO.create(
                                    new model.Notification(0, student.getId(), msg, null, false));
                        }
                    }
                }

                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 500, "Failed to update interview");
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error updating interview: " + e.getMessage());
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        String idStr = null;

        if (query != null && query.startsWith("id=")) {
            idStr = query.substring(3);
        }

        if (idStr == null) {
            sendError(exchange, 400, "Missing interview ID");
            return;
        }

        try {
            int id = Integer.parseInt(idStr);
            Interview interview = interviewDAO.findById(id);

            if (interview == null) {
                sendError(exchange, 404, "Interview not found");
                return;
            }

            // Notify student before deletion
            Evaluation eval = evaluationDAO.findByAppId(getAppIdFromEvalId(interview.getEvalID()));
            if (eval != null) {
                Application app = applicationDAO.findById(eval.getAppID());
                if (app != null) {
                    model.User student = userDAO.findUserByRoleID(app.getStudentID(), "Student");
                    if (student != null) {
                        String msg = String.format(
                                "Your scheduled interview for '%s' has been cancelled. You will be notified if a new interview is scheduled.",
                                app.getScholarshipTitle());
                        notificationDAO
                                .create(new model.Notification(0, student.getId(), msg, null, false));
                    }
                }
            }

            if (interviewDAO.delete(id)) {
                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 500, "Failed to delete interview");
            }
        } catch (NumberFormatException e) {
            sendError(exchange, 400, "Invalid ID format");
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error deleting interview: " + e.getMessage());
        }
    }

    private String buildAllInterviewsJson() {
        List<Interview> interviews = interviewDAO.findAll();
        StringBuilder json = new StringBuilder("{\"interviews\": [");

        boolean first = true;
        for (Interview interview : interviews) {
            if (!first)
                json.append(",");
            first = false;

            // Get related data
            Evaluation eval = findEvalByEvalId(interview.getEvalID());
            if (eval != null) {
                Application app = applicationDAO.findById(eval.getAppID());
                if (app != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    json.append(String.format(
                            "{\"id\": %d, \"evalId\": %d, \"appId\": %d, \"dateTime\": \"%s\", \"venueOrLink\": \"%s\", \"status\": \"%s\", \"studentName\": \"%s\", \"studentId\": \"%s\", \"scholarshipTitle\": \"%s\"}",
                            interview.getInterviewID(),
                            interview.getEvalID(),
                            app.getAppID(),
                            sdf.format(interview.getDateTime()),
                            JsonUtils.escape(interview.getVenueOrLink()),
                            JsonUtils.escape(interview.getStatus()),
                            JsonUtils.escape(app.getApplicantName() != null ? app.getApplicantName() : "Unknown"),
                            JsonUtils.escape(app.getStudentID()),
                            JsonUtils.escape(app.getScholarshipTitle())));
                }
            }
        }

        json.append("]}");
        return json.toString();
    }

    private String buildInterviewsListJson(List<Interview> interviews) {
        StringBuilder json = new StringBuilder("{\"interviews\": [");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        boolean first = true;
        for (Interview interview : interviews) {
            if (!first)
                json.append(",");
            first = false;
            json.append(buildInterviewJson(interview, sdf));
        }

        json.append("]}");
        return json.toString();
    }

    private String buildInterviewJson(Interview interview) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        return buildInterviewJson(interview, sdf);
    }

    private String buildInterviewJson(Interview interview, SimpleDateFormat sdf) {
        return String.format(
                "{\"id\": %d, \"evalId\": %d, \"dateTime\": \"%s\", \"venueOrLink\": \"%s\", \"status\": \"%s\"}",
                interview.getInterviewID(),
                interview.getEvalID(),
                sdf.format(interview.getDateTime()),
                JsonUtils.escape(interview.getVenueOrLink()),
                JsonUtils.escape(interview.getStatus()));
    }

    private Evaluation findEvalByEvalId(int evalId) {
        // Simple lookup - in real app this would be in EvaluationDAO
        try {
            String sql = "SELECT * FROM Evaluation WHERE evalID = ?";
            java.sql.Connection conn = db.DatabaseConnection.getConnection();
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, evalId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                Evaluation eval = new Evaluation(
                        rs.getInt("evalID"),
                        rs.getInt("appID"),
                        rs.getString("reviewerID"),
                        rs.getString("scholarshipComments"),
                        rs.getFloat("interviewScore"),
                        rs.getString("interviewComments"),
                        rs.getString("status"),
                        rs.getTimestamp("evaluatedDate"));
                rs.close();
                pstmt.close();
                conn.close();
                return eval;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private int getAppIdFromEvalId(int evalId) {
        Evaluation eval = findEvalByEvalId(evalId);
        return eval != null ? eval.getAppID() : 0;
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
