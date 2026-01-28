package scholarship.server;

import scholarship.dao.AuditLogDAO;
import scholarship.model.AuditLog;
import scholarship.utils.JsonUtils;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.*;

public class SystemHandler implements HttpHandler {

    private static boolean maintenanceMode = false;

    private static final String BACKUPS_DIR = "backups";

    public static void initScheduledTasks() {
        Timer timer = new Timer("DeadlineChecker", true);
        // Check once a day (start after 30 seconds)
        long period = 24 * 60 * 60 * 1000L;
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    scholarship.dao.ScholarshipDAO sDAO = new scholarship.dao.ScholarshipDAO();
                    scholarship.dao.NotificationDAO nDAO = new scholarship.dao.NotificationDAO();

                    List<scholarship.model.Scholarship> all = sDAO.findAll();
                    long now = System.currentTimeMillis();
                    long days7 = 7L * 24 * 60 * 60 * 1000L;
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy");

                    for (scholarship.model.Scholarship s : all) {
                        if (s.isActive() && s.getDeadline() != null) {
                            long deadline = s.getDeadline().getTime();
                            long diff = deadline - now;
                            // If deadline is within 7 days and in future
                            if (diff > 0 && diff <= days7) {
                                String dateStr = sdf.format(s.getDeadline());
                                // Notify Admin
                                nDAO.createForRole("Admin",
                                        "Scholarship '" + s.getTitle() + "' is closing on " + dateStr
                                                + " (in less than 7 days).");
                                // Notify Students
                                nDAO.createForRole("Student",
                                        "Reminder: Scholarship '" + s.getTitle() + "' is closing on " + dateStr
                                                + ". Don't miss the deadline!");
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Scheduled task error: " + e.getMessage());
                }
            }
        }, 30000L, period);
        System.out.println("Scheduled tasks initialized.");
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();

        if (path.endsWith("/backup")) {
            handleBackup(exchange);
        } else if (path.endsWith("/backups")) {
            handleBackupsList(exchange);
        } else if (path.endsWith("/maintenance")) {
            handleMaintenance(exchange);
        } else if (path.endsWith("/audit-logs")) {
            handleAuditLogs(exchange);
        } else if (path.contains("/backup/download/")) {
            handleBackupDownload(exchange);
        } else {
            sendError(exchange, 404, "Endpoint not found");
        }
    }

    private void handleBackup(HttpExchange exchange) throws IOException {
        if (!"POST".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            // Get client IP for audit logging
            String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

            // Create timestamp for backup filename
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFileName = "backup_" + timestamp + ".zip";
            String backupPath = BACKUPS_DIR + File.separator + backupFileName;

            // Create the backup zip file
            try (FileOutputStream fos = new FileOutputStream(backupPath);
                    ZipOutputStream zos = new ZipOutputStream(fos)) {

                // Add database dump (using pg_dump if available)
                String dbDumpFile = createDatabaseDump(timestamp);
                if (dbDumpFile != null) {
                    addFileToZip(zos, new File(dbDumpFile), "database/");
                    new File(dbDumpFile).delete(); // Clean up temp file
                }

                // Add important directories
                addDirectoryToZip(zos, new File("www"), "www/");
                addDirectoryToZip(zos, new File("sql_queries"), "sql_queries/");
                addDirectoryToZip(zos, new File("src"), "src/");
                addDirectoryToZip(zos, new File("lib"), "lib/");

                // Add config file
                File dbProps = new File("db.properties");
                if (dbProps.exists()) {
                    addFileToZip(zos, dbProps, "config/");
                }
            }

            // Get backup file size
            File backupFile = new File(backupPath);
            long fileSizeKB = backupFile.length() / 1024;

            // Log the backup action
            AuditLogDAO.log(null, "System", "Backup Created", "System", backupFileName,
                    "Backup file created: " + backupFileName + " (" + fileSizeKB + " KB)", clientIP);

            String response = String.format(
                    "{\"success\": true, \"message\": \"Backup created successfully\", \"filename\": \"%s\", \"size\": %d}",
                    backupFileName, fileSizeKB);
            sendResponse(exchange, 200, response);

        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Backup failed: " + e.getMessage());
        }
    }

    private String createDatabaseDump(String timestamp) {
        try {
            // Read database properties
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream("db.properties")) {
                props.load(fis);
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");

            // Extract database name from URL
            String dbName = url.substring(url.lastIndexOf("/") + 1);

            // Extract host and port
            String hostPort = url.substring(url.indexOf("://") + 3, url.lastIndexOf("/"));
            String host = hostPort.contains(":") ? hostPort.split(":")[0] : hostPort;
            String port = hostPort.contains(":") ? hostPort.split(":")[1] : "5432";

            String dumpFile = BACKUPS_DIR + File.separator + "db_dump_" + timestamp + ".sql";

            // Try to run pg_dump
            ProcessBuilder pb = new ProcessBuilder(
                    "pg_dump",
                    "-h", host,
                    "-p", port,
                    "-U", user,
                    "-d", dbName,
                    "-f", dumpFile);
            pb.environment().put("PGPASSWORD", password);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0 && new File(dumpFile).exists()) {
                return dumpFile;
            } else {
                System.err.println("pg_dump failed or not available, skipping database dump");
                return null;
            }
        } catch (Exception e) {
            System.err.println("Could not create database dump: " + e.getMessage());
            return null;
        }
    }

    private void addFileToZip(ZipOutputStream zos, File file, String prefix) throws IOException {
        if (!file.exists())
            return;

        String entryName = prefix + file.getName();
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(file.toPath(), zos);
        zos.closeEntry();
    }

    private void addDirectoryToZip(ZipOutputStream zos, File dir, String prefix) throws IOException {
        if (!dir.exists() || !dir.isDirectory())
            return;

        File[] files = dir.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                addDirectoryToZip(zos, file, prefix + file.getName() + "/");
            } else {
                String entryName = prefix + file.getName();
                zos.putNextEntry(new ZipEntry(entryName));
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    private void handleBackupsList(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        File backupsDir = new File(BACKUPS_DIR);
        File[] backupFiles = backupsDir.listFiles((dir, name) -> name.endsWith(".zip"));

        StringBuilder json = new StringBuilder("[");
        if (backupFiles != null && backupFiles.length > 0) {
            // Sort by last modified (newest first)
            Arrays.sort(backupFiles, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean first = true;
            for (File f : backupFiles) {
                if (!first)
                    json.append(",");
                json.append("{");
                json.append("\"filename\": \"").append(JsonUtils.escape(f.getName())).append("\",");
                json.append("\"size\": ").append(f.length() / 1024).append(",");
                json.append("\"created\": \"").append(sdf.format(new Date(f.lastModified()))).append("\"");
                json.append("}");
                first = false;
            }
        }
        json.append("]");

        sendResponse(exchange, 200, json.toString());
    }

    private void handleBackupDownload(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String filename = path.substring(path.lastIndexOf("/") + 1);

        // Security check: prevent directory traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            sendError(exchange, 400, "Invalid filename");
            return;
        }

        File backupFile = new File(BACKUPS_DIR + File.separator + filename);
        if (!backupFile.exists()) {
            sendError(exchange, 404, "Backup file not found");
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", "application/zip");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        exchange.sendResponseHeaders(200, backupFile.length());

        try (OutputStream os = exchange.getResponseBody();
                FileInputStream fis = new FileInputStream(backupFile)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }
    }

    private void handleAuditLogs(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendError(exchange, 405, "Method Not Allowed");
            return;
        }

        try {
            AuditLogDAO dao = new AuditLogDAO();

            // Parse query parameters for filtering
            String query = exchange.getRequestURI().getQuery();
            int limit = 100;
            if (query != null && query.contains("limit=")) {
                try {
                    String limitStr = query.split("limit=")[1].split("&")[0];
                    limit = Integer.parseInt(limitStr);
                } catch (Exception ignored) {
                }
            }

            List<AuditLog> logs = dao.getAll(limit);

            StringBuilder json = new StringBuilder("[");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            boolean first = true;

            for (AuditLog log : logs) {
                if (!first)
                    json.append(",");
                json.append("{");
                json.append("\"logID\": ").append(log.getLogID()).append(",");
                json.append("\"userID\": ").append(log.getUserID() != null ? log.getUserID() : "null").append(",");
                json.append("\"userEmail\": \"")
                        .append(JsonUtils.escape(log.getUserEmail() != null ? log.getUserEmail() : "")).append("\",");
                json.append("\"action\": \"").append(JsonUtils.escape(log.getAction())).append("\",");
                json.append("\"entityType\": \"")
                        .append(JsonUtils.escape(log.getEntityType() != null ? log.getEntityType() : "")).append("\",");
                json.append("\"entityID\": \"")
                        .append(JsonUtils.escape(log.getEntityID() != null ? log.getEntityID() : "")).append("\",");
                json.append("\"details\": \"")
                        .append(JsonUtils.escape(log.getDetails() != null ? log.getDetails() : "")).append("\",");
                json.append("\"ipAddress\": \"")
                        .append(JsonUtils.escape(log.getIpAddress() != null ? log.getIpAddress() : "")).append("\",");
                json.append("\"createdAt\": \"")
                        .append(log.getCreatedAt() != null ? sdf.format(log.getCreatedAt()) : "").append("\"");
                json.append("}");
                first = false;
            }
            json.append("]");

            sendResponse(exchange, 200, json.toString());
        } catch (Exception e) {
            e.printStackTrace();
            sendError(exchange, 500, "Error fetching audit logs: " + e.getMessage());
        }
    }

    private void handleMaintenance(HttpExchange exchange) throws IOException {
        String clientIP = exchange.getRemoteAddress().getAddress().getHostAddress();

        if ("GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 200, "{\"enabled\": " + maintenanceMode + "}");
        } else if ("POST".equals(exchange.getRequestMethod())) {
            String requestBody = new String(exchange.getRequestBody().readAllBytes());
            String enabledStr = JsonUtils.extractValue(requestBody, "enabled");

            if (enabledStr != null) {
                maintenanceMode = Boolean.parseBoolean(enabledStr);

                // Log the change
                AuditLogDAO.log(null, "System", "Maintenance Mode Changed", "System", null,
                        "Maintenance mode " + (maintenanceMode ? "enabled" : "disabled"), clientIP);

                sendResponse(exchange, 200, "{\"success\": true, \"enabled\": " + maintenanceMode + "}");
            } else {
                sendError(exchange, 400, "Missing 'enabled' field");
            }
        } else {
            sendError(exchange, 405, "Method Not Allowed");
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
}