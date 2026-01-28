package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.AuditLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuditLogDAO {

    /**
     * Log a new audit entry
     */
    public void logAction(AuditLog log) {
        String sql = "INSERT INTO AuditLog (userID, userEmail, action, entityType, entityID, details, ipAddress) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            if (log.getUserID() != null) {
                stmt.setInt(1, log.getUserID());
            } else {
                stmt.setNull(1, Types.INTEGER);
            }
            stmt.setString(2, log.getUserEmail());
            stmt.setString(3, log.getAction());
            stmt.setString(4, log.getEntityType());
            stmt.setString(5, log.getEntityID());
            stmt.setString(6, log.getDetails());
            stmt.setString(7, log.getIpAddress());

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error logging audit action: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convenience method to log an action with minimal parameters
     */
    public static void log(Integer userID, String userEmail, String action, String entityType,
            String entityID, String details, String ipAddress) {
        AuditLogDAO dao = new AuditLogDAO();
        AuditLog log = new AuditLog(userID, userEmail, action, entityType, entityID, details, ipAddress);
        dao.logAction(log);
    }

    /**
     * Get all audit logs, ordered by most recent first
     */
    public List<AuditLog> getAll() {
        return getAll(100); // Default limit
    }

    /**
     * Get audit logs with a limit
     */
    public List<AuditLog> getAll(int limit) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog ORDER BY createdAt DESC LIMIT ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, limit);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by user ID
     */
    public List<AuditLog> getByUser(int userID) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE userID = ? ORDER BY createdAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs by user: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by date range
     */
    public List<AuditLog> getByDateRange(Timestamp start, Timestamp end) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE createdAt BETWEEN ? AND ? ORDER BY createdAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs by date range: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    /**
     * Get audit logs by action type
     */
    public List<AuditLog> getByAction(String action) {
        List<AuditLog> logs = new ArrayList<>();
        String sql = "SELECT * FROM AuditLog WHERE action ILIKE ? ORDER BY createdAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + action + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapResultSetToAuditLog(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching audit logs by action: " + e.getMessage());
            e.printStackTrace();
        }

        return logs;
    }

    private AuditLog mapResultSetToAuditLog(ResultSet rs) throws SQLException {
        AuditLog log = new AuditLog();
        log.setLogID(rs.getInt("logID"));
        log.setUserID(rs.getObject("userID") != null ? rs.getInt("userID") : null);
        log.setUserEmail(rs.getString("userEmail"));
        log.setAction(rs.getString("action"));
        log.setEntityType(rs.getString("entityType"));
        log.setEntityID(rs.getString("entityID"));
        log.setDetails(rs.getString("details"));
        log.setIpAddress(rs.getString("ipAddress"));
        log.setCreatedAt(rs.getTimestamp("createdAt"));
        return log;
    }
}
