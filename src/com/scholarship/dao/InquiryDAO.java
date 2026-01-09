package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Inquiry;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InquiryDAO {

    public boolean create(Inquiry inquiry) {
        String sql = "INSERT INTO Inquiry (studentID, message) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, inquiry.getStudentID());
            pstmt.setString(2, inquiry.getMessage());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Inquiry> findByStudentId(String studentId) {
        List<Inquiry> inquiries = new ArrayList<>();
        String sql = "SELECT * FROM Inquiry WHERE studentID = ? ORDER BY submittedAt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    inquiries.add(new Inquiry(
                            rs.getInt("inquiryID"),
                            rs.getString("studentID"),
                            rs.getString("message"),
                            rs.getTimestamp("submittedAt")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }
}
