package dao;

import db.DatabaseConnection;
import model.Notification;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {

    public boolean create(Notification notif) {
        String sql = "INSERT INTO Notification (userID, message) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, notif.getUserID());
            pstmt.setString(2, notif.getMessage());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createForRole(String role, String message) {
        // "all" -> everyone
        // "student" -> Student
        // "reviewer" -> Reviewer
        // "committee" -> Committee/CommitteeMember
        String sql = "INSERT INTO Notification (userID, message) SELECT userID, ? FROM \"User\" WHERE role ILIKE ? OR ? = 'all'";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Standardize roles for LIKE query if needed, but for now exact match or 'all'
            // logic
            String roleParam = role;
            if ("student".equalsIgnoreCase(role))
                roleParam = "Student";
            else if ("reviewer".equalsIgnoreCase(role))
                roleParam = "Reviewer";
            else if ("committee".equalsIgnoreCase(role))
                roleParam = "Committee%"; // Handles Committee and CommitteeMember

            pstmt.setString(1, message);
            pstmt.setString(2, roleParam); // role filter
            pstmt.setString(3, role); // check for 'all'

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Notification> findByUserId(int userId) {
        List<Notification> notifs = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE userID = ? ORDER BY sentAt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    notifs.add(new Notification(
                            rs.getInt("notifID"),
                            rs.getInt("userID"),
                            rs.getString("message"),
                            rs.getTimestamp("sentAt"),
                            rs.getBoolean("isRead")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return notifs;
    }

    public boolean markAsRead(int notifID) {
        String sql = "UPDATE Notification SET isRead = true WHERE notifID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, notifID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean markAllAsRead(int userId) {
        String sql = "UPDATE Notification SET isRead = true WHERE userID = ? AND isRead = false";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() >= 0; // Returns true even if 0 rows affected
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
