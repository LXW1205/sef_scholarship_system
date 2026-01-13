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
            String query = exchange.getRequestURI().getQuery();
            String range = "all";
            if (query != null && query.contains("range=")) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("range")) {
                        range = pair[1];
                        break;
                    }
                }
            }

            String response = getStatsJson(range);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length());
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response.getBytes());
            }
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private String getStatsJson(String range) {
        try {
            Admin admin = new Admin();
            DashboardData data = admin.viewAdminAnalytics(range);

            // Manually building JSON for trends map
            StringBuilder trendsJson = new StringBuilder("{");
            boolean first = true;
            for (java.util.Map.Entry<String, Integer> entry : data.getTrendData().entrySet()) {
                if (!first)
                    trendsJson.append(",");
                first = false;
                trendsJson.append(String.format("\"%s\": %d", entry.getKey(), entry.getValue()));
            }
            trendsJson.append("}");
            // Manually building JSON for user roles map
            StringBuilder rolesJson = new StringBuilder("{");
            first = true;
            for (java.util.Map.Entry<String, Integer> entry : data.getUserRoles().entrySet()) {
                if (!first)
                    rolesJson.append(",");
                first = false;
                rolesJson.append(String.format("\"%s\": %d", entry.getKey(), entry.getValue()));
            }
            rolesJson.append("}");

            return String.format(
                    "{\"totalUsers\": %d, \"activeScholarships\": %d, \"pendingApps\": %d, \"approvedMonth\": %d, \"trends\": %s, \"userRoles\": %s}",
                    data.getTotalUsers(), data.getActiveScholarships(), data.getPendingApps(), data.getApprovedMonth(),
                    trendsJson.toString(), rolesJson.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }
}
