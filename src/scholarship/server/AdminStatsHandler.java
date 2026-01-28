package scholarship.server;

import scholarship.model.Admin;
import scholarship.model.DashboardData;
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

            return String.format(
                    "{\"totalUsers\": %d, \"activeScholarships\": %d, \"pendingApps\": %d, \"approvedMonth\": %d, \"trends\": %s, \"userRoles\": %s, \"awardDistribution\": %s, \"statusDistribution\": %s}",
                    data.getTotalUsers(), data.getActiveScholarships(), data.getPendingApps(), data.getApprovedMonth(),
                    mapToJson(data.getTrendData()), mapToJson(data.getUserRoles()),
                    mapToJson(data.getAwardDistribution()), mapToJson(data.getStatusDistribution()));
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String mapToJson(java.util.Map<String, Integer> map) {
        if (map == null)
            return "{}";
        StringBuilder json = new StringBuilder("{");
        boolean first = true;
        for (java.util.Map.Entry<String, Integer> entry : map.entrySet()) {
            if (!first)
                json.append(",");
            first = false;
            // Escape double quotes in key if necessary
            String key = entry.getKey().replace("\"", "\\\"");
            json.append(String.format("\"%s\": %d", key, entry.getValue()));
        }
        json.append("}");
        return json.toString();
    }
}
