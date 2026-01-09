package com.scholarship.server;

import com.scholarship.dao.ApplicationDAO;
import com.scholarship.model.Application;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ApplicationHandler implements HttpHandler {

    private ApplicationDAO applicationDAO = new ApplicationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            // In a real app, filtering by user would happen here via token
            // For now, we'll fetch for a placeholder student or all (as per previous logic)
            String response = getApplicationsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getApplicationsJson() {
        StringBuilder json = new StringBuilder("{\"applications\": [");

        try {
            // Placeholder: in a real app, this would be the logged-in student's ID
            List<Application> apps = applicationDAO.findByStudentId("S1001");

            boolean first = true;
            for (Application a : apps) {
                if (!first)
                    json.append(",");
                first = false;

                json.append(String.format(
                        "{\"id\": %d, \"scholarship_title\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\"}",
                        a.getAppID(),
                        JsonUtils.escape(a.getScholarshipTitle()),
                        a.getSubmissionDate(),
                        JsonUtils.escape(a.getStatus())));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}"; // Return error json
        }

        json.append("]}");
        return json.toString();
    }
}
