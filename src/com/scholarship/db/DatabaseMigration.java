package com.scholarship.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseMigration {

    public static void run() {

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // Add columns to Application table if they don't exist
            // PostgreSQL doesn't support IF NOT EXISTS for ADD COLUMN in older versions,
            // so we'll wrap each individually or check system tables.
            // Simplified approach: try to add and catch exception if it already exists

            executeSilently(stmt, "ALTER TABLE Application ADD COLUMN otherScholarships TEXT");

            // Create Notification table if not exists
            executeSilently(stmt, "CREATE TABLE IF NOT EXISTS Notification (" +
                    "notifID SERIAL PRIMARY KEY, " +
                    "userID INTEGER NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "sentAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "isRead BOOLEAN DEFAULT FALSE)");

        } catch (SQLException e) {
            System.err.println("[ERROR] Migration failed: " + e.getMessage());
        }
    }

    private static void executeSilently(Statement stmt, String sql) {
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            // PostgreSQL Error Codes:
            // 42701: column already exists
            // 42703: column does not exist
            // 42704: table does not exist
            if ("42701".equals(e.getSQLState()) || "42703".equals(e.getSQLState())) {
                // Already done or nothing to do, ignore
            } else {
                System.err.println("[WARNING] Could not execute migration: " + sql + " - " + e.getMessage()
                        + " (SQLState: " + e.getSQLState() + ")");
            }
        }
    }
}
