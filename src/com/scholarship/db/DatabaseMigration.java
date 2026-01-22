package com.scholarship.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

/**
 * Database migration utility to ensure required tables exist
 */
public class DatabaseMigration {

    public static void run() {
        System.out.println("Running database migrations...");

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Create AuditLog table if it doesn't exist
            String createAuditLogTable = "CREATE TABLE IF NOT EXISTS AuditLog (" +
                    "    logID SERIAL PRIMARY KEY," +
                    "    userID INTEGER," +
                    "    userEmail VARCHAR(100)," +
                    "    action VARCHAR(100) NOT NULL," +
                    "    entityType VARCHAR(50)," +
                    "    entityID VARCHAR(50)," +
                    "    details TEXT," +
                    "    ipAddress VARCHAR(45)," +
                    "    createdAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")";

            stmt.execute(createAuditLogTable);

            // Add mappedField to Criteria if it doesn't exist
            addColumnIfNotExists(stmt, "Criteria", "mappedField", "VARCHAR(50) DEFAULT 'none'");

            // Add fileContent to Document if it doesn't exist
            addColumnIfNotExists(stmt, "Document", "fileContent", "TEXT");

            // Add minCGPA and maxFamilyIncome to Scholarship if they don't exist
            addColumnIfNotExists(stmt, "Scholarship", "minCGPA", "DECIMAL(3,2) DEFAULT 0.0");
            addColumnIfNotExists(stmt, "Scholarship", "maxFamilyIncome", "DECIMAL(12,2) DEFAULT 0.0");

            // Alter expectedGraduation to DATE if it's still VARCHAR
            try {
                stmt.execute(
                        "ALTER TABLE Student ALTER COLUMN expectedGraduation TYPE DATE USING expectedGraduation::DATE");
                System.out.println("Altered expectedGraduation to DATE.");
            } catch (Exception e) {
                // Ignore if already changed or no data to convert easily (sample reload will
                // fix it)
            }

            // Check if we should force reload sample data or if tables are empty
            boolean forceReload = Files.exists(Paths.get("sql_queries/.reload_data"));
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Scholarship");
            boolean isEmpty = (rs.next() && rs.getInt(1) == 0);

            if (forceReload || isEmpty) {
                System.out.println("Reloading sample data (Force=" + forceReload + ", Empty=" + isEmpty + ")...");
                // Clear existing data to avoid FK issues and duplicates
                stmt.execute(
                        "TRUNCATE TABLE Criteria, \"User\", Student, Reviewer, CommitteeMember, Admin, Scholarship, Application, Document, Evaluation, Interview, ClarificationRequest, EvaluationScore, AuditLog, Inquiry, Notification, Report CASCADE");
                loadSampleData(stmt);

                if (forceReload) {
                    try {
                        Files.delete(Paths.get("sql_queries/.reload_data"));
                    } catch (Exception e) {
                    }
                }
            }

            System.out.println("Database migrations completed successfully.");

        } catch (Exception e) {
            System.err.println("Database migration warning: " + e.getMessage());
            // Don't fail startup if migrations fail - tables might already exist
        }
    }

    private static void loadSampleData(Statement stmt) {
        try {
            // Check if file exists in standard locations
            String path = "sql_queries/sample_data.sql";
            if (!Files.exists(Paths.get(path))) {
                System.err.println("Sample data file not found at " + path);
                return;
            }

            String sql = Files.lines(Paths.get(path))
                    .filter(line -> !line.trim().startsWith("--"))
                    .collect(Collectors.joining("\n"));

            // Split by semicolon and execute each statement
            for (String statement : sql.split(";")) {
                if (!statement.trim().isEmpty()) {
                    try {
                        stmt.execute(statement.trim());
                    } catch (Exception e) {
                        // Ignore individual statement failures (e.g. duplicate users)
                    }
                }
            }
            System.out.println("Sample data loaded successfully.");
        } catch (Exception e) {
            System.err.println("Error loading sample data: " + e.getMessage());
        }
    }

    private static void addColumnIfNotExists(Statement stmt, String table, String column, String definition) {
        try {
            // PostgreSQL specific "ADD COLUMN IF NOT EXISTS"
            stmt.execute("ALTER TABLE " + table + " ADD COLUMN IF NOT EXISTS " + column + " " + definition);
        } catch (Exception e) {
            System.err.println("Note: Could not add column " + column + " to " + table + ": " + e.getMessage());
        }
    }
}
