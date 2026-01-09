package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Evaluation;
import java.sql.*;

public class EvaluationDAO {

    public boolean save(Evaluation eval) {
        String sql = "INSERT INTO Evaluation (appID, reviewerStaffID, scholarshipComments, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, eval.getAppID());
            pstmt.setString(2, eval.getReviewerStaffID());
            pstmt.setString(3, eval.getScholarshipComments());
            pstmt.setString(4, eval.getStatus());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Evaluation findByAppId(int appId) {
        String sql = "SELECT * FROM Evaluation WHERE appID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Evaluation(
                            rs.getInt("evalID"),
                            rs.getInt("appID"),
                            rs.getString("reviewerStaffID"),
                            rs.getFloat("scholarshipScore"),
                            rs.getString("scholarshipComments"),
                            rs.getFloat("interviewScore"),
                            rs.getString("interviewComments"),
                            rs.getString("status"),
                            rs.getTimestamp("evaluatedDate"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
