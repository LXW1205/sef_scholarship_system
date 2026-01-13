package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Application;
import com.scholarship.model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public boolean save(Application app) {
        String sql = "INSERT INTO Application (studentID, scholarshipID, status) VALUES (?, ?, ?)";
        System.out.println("[DEBUG] ApplicationDAO saving: SQL=" + sql);
        System.out.println("[DEBUG] Params: studentID=" + app.getStudentID() + ", scholarshipID="
                + app.getScholarshipID() + ", status=" + app.getStatus());

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, app.getStudentID());
            pstmt.setInt(2, app.getScholarshipID());
            pstmt.setString(3, app.getStatus());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("[DEBUG] Rows affected: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        System.out.println("[DEBUG] Generated AppID: " + generatedKeys.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ApplicationDAO save failed: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    public List<Application> findByStudentId(String studentId) {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT a.*, s.title FROM Application a JOIN Scholarship s ON a.scholarshipID = s.scholarshipID WHERE a.studentID = ? ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Application app = new Application(
                            rs.getInt("appID"),
                            rs.getString("studentID"),
                            rs.getInt("scholarshipID"),
                            rs.getString("title"),
                            rs.getTimestamp("submissionDate"),
                            rs.getString("status"));
                    app.getDocuments().addAll(findDocumentsByAppId(app.getAppID(), conn));
                    apps.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public List<Application> findAll() {
        List<Application> apps = new ArrayList<>();
        // Left join to get reviewer details if assigned
        String sql = "SELECT a.*, s.title, st.fullName, e.reviewerStaffID, u.username as reviewerName " +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "LEFT JOIN Evaluation e ON a.appID = e.appID " +
                "LEFT JOIN Reviewer r ON e.reviewerStaffID = r.staffID " +
                "LEFT JOIN \"User\" u ON r.userID = u.userID " +
                "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Application app = new Application(
                            rs.getInt("appID"),
                            rs.getString("studentID"),
                            rs.getInt("scholarshipID"),
                            rs.getString("title"),
                            rs.getTimestamp("submissionDate"),
                            rs.getString("status"),
                            rs.getString("reviewerStaffID"),
                            rs.getString("reviewerName"),
                            rs.getString("fullName"));
                    app.getDocuments().addAll(findDocumentsByAppId(app.getAppID(), conn));
                    apps.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    private List<Document> findDocumentsByAppId(int appId, Connection conn) throws SQLException {
        List<Document> docs = new ArrayList<>();
        String sql = "SELECT * FROM Document WHERE appID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    docs.add(new Document(
                            rs.getInt("docID"),
                            rs.getInt("appID"),
                            rs.getString("fileName"),
                            rs.getString("fileType"),
                            rs.getTimestamp("uploadDate")));
                }
            }
        }
        return docs;
    }
}
