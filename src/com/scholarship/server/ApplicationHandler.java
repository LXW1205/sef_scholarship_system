package com.scholarship.server;

import com.scholarship.model.Application;
import com.scholarship.model.Student;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ApplicationHandler implements HttpHandler {

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
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getApplicationsJson() {
        StringBuilder json = new StringBuilder("{\"applications\": [");
        
        try {
            Student student = new Student();
            List<Application> apps = student.viewAppHistory();
            
            boolean first = true;
            for (Application a : apps) {
                if (!first) json.append(",");
                first = false;
                
                json.append(String.format("{\"id\": %d, \"scholarship_title\": \"%s\", \"submissiondate\": \"%s\", \"status\": \"%s\"}",
                        a.getAppID(),
                        JsonUtils.escape(a.getScholarshipTitle()),
                        a.getSubmissionDate(),
                        JsonUtils.escape(a.getStatus())
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}"; // Return error json
        }
        
        json.append("]}");
        return json.toString();
    }
}
