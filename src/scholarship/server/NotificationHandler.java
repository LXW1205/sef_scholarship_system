package scholarship.server;

import scholarship.dao.NotificationDAO;
import scholarship.model.Notification;
import scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NotificationHandler implements HttpHandler {

    private final NotificationDAO notificationDAO = new NotificationDAO();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            String query = exchange.getRequestURI().getQuery();
            int userId = -1;
            if (query != null && query.contains("userId=")) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2 && pair[0].equals("userId")) {
                        try {
                            userId = Integer.parseInt(pair[1]);
                        } catch (NumberFormatException e) {
                            userId = -1;
                        }
                        break;
                    }
                }
            }

            if (userId == -1) {
                sendError(exchange, 400, "Missing or invalid userId");
                return;
            }

            String response = getNotificationsJson(userId);
            byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } else if ("POST".equals(exchange.getRequestMethod())) {
            handlePost(exchange);
        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes());
        String action = JsonUtils.extractValue(requestBody, "action");

        if ("markAllAsRead".equals(action)) {
            String userIdStr = JsonUtils.extractValue(requestBody, "userId");
            if (userIdStr != null) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    notificationDAO.markAllAsRead(userId);
                    sendResponse(exchange, 200, "{\"success\": true}");
                    return;
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "Invalid userId");
                    return;
                }
            }
        } else if ("markAsRead".equals(action)) {
            String notifIdStr = JsonUtils.extractValue(requestBody, "notifId");
            if (notifIdStr != null) {
                try {
                    int notifId = Integer.parseInt(notifIdStr);
                    notificationDAO.markAsRead(notifId);
                    sendResponse(exchange, 200, "{\"success\": true}");
                    return;
                } catch (NumberFormatException e) {
                    sendError(exchange, 400, "Invalid notifId");
                    return;
                }
            }
        } else {
            // Original broadcast functionality
            String recipients = JsonUtils.extractValue(requestBody, "recipients");
            String message = JsonUtils.extractValue(requestBody, "message");

            if (recipients != null && message != null && !message.isEmpty()) {
                notificationDAO.createForRole(recipients, message);
                sendResponse(exchange, 200, "{\"success\": true}");
            } else {
                sendError(exchange, 400, "Missing recipients or message");
            }
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

    private String getNotificationsJson(int userId) {
        StringBuilder json = new StringBuilder("{\"notifications\": [");
        try {
            List<Notification> notifs = notificationDAO.findByUserId(userId);
            boolean first = true;
            for (Notification n : notifs) {
                if (!first)
                    json.append(",");
                first = false;
                json.append(String.format(
                        "{\"notifID\": %d, \"message\": \"%s\", \"sentAt\": \"%s\", \"isRead\": %b}",
                        n.getNotifID(),
                        JsonUtils.escape(n.getMessage()),
                        n.getSentAt() != null ? n.getSentAt().toInstant().toString() : "",
                        n.isRead()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"" + JsonUtils.escape(e.getMessage()) + "\"}";
        }
        json.append("]}");
        return json.toString();
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
        String response = String.format("{\"error\": \"%s\"}", JsonUtils.escape(message));
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
