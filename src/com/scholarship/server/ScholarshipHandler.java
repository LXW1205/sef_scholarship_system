package com.scholarship.server;

import com.scholarship.model.Scholarship;
import com.scholarship.model.Student;
import com.scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public class ScholarshipHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = getScholarshipsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getScholarshipsJson() {
        StringBuilder json = new StringBuilder("{\"scholarships\": [");
        
        try {
            // In a real app, we would get the student from the authenticated user context
            Student student = new Student(); 
            List<Scholarship> scholarships = student.viewAvailableScholarships();
            
            boolean first = true;
            for (Scholarship s : scholarships) {
                if (!first) json.append(",");
                first = false;
                
                json.append(String.format("{\"id\": %d, \"title\": \"%s\", \"deadline\": \"%s\", \"status\": \"%s\"}",
                        s.getScholarshipID(),
                        JsonUtils.escape(s.getTitle()),
                        s.getDeadline(),
                        s.isActive() ? "Active" : "Closed"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
        
        json.append("]}");
        return json.toString();
    }
}
