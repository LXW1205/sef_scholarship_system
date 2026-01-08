package com.scholarship.server;

import com.scholarship.model.Admin;
import com.scholarship.model.DashboardData;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;

public class AdminStatsHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String response = getStatsJson();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getStatsJson() {
        try {
             Admin admin = new Admin();
             DashboardData data = admin.viewAdminAnalytics();
             
             return String.format("{\"totalUsers\": %d, \"activeScholarships\": %d, \"pendingApps\": %d, \"approvedMonth\": %d}",
                data.getTotalUsers(), data.getActiveScholarships(), data.getPendingApps(), data.getApprovedMonth());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
