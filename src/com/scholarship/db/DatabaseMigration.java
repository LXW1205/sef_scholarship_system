package com.scholarship.db;

import java.sql.Connection;
import java.sql.Statement;

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
            try {
                stmt.execute("ALTER TABLE Criteria ADD COLUMN IF NOT EXISTS mappedField VARCHAR(50) DEFAULT 'none'");
            } catch (Exception e) {
                // If the DB doesn't support IF NOT EXISTS in ALTER (older Postgres), it might
                // fail if already exists
                // We'll just ignore and assume it's there
            }

            System.out.println("Database migrations completed successfully.");

        } catch (Exception e) {
            System.err.println("Database migration warning: " + e.getMessage());
            // Don't fail startup if migrations fail - tables might already exist
        }
    }
}
