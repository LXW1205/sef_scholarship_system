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
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, app.getStudentID());
            pstmt.setInt(2, app.getScholarshipID());
            pstmt.setString(3, app.getStatus());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // app.setAppID(generatedKeys.getInt(1)); // If setter was added
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
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
