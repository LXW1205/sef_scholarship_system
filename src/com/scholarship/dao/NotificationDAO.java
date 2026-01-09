package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Notification;
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
}
