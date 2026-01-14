package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Application;
import com.scholarship.model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public boolean save(Application app) {
        String sql = "INSERT INTO Application (studentID, scholarshipID, status, personalStatement, otherScholarships) VALUES (?, ?, ?, ?, ?)";
        System.out.println("[DEBUG] ApplicationDAO saving: SQL=" + sql);

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, app.getStudentID());
            pstmt.setInt(2, app.getScholarshipID());
            pstmt.setString(3, app.getStatus());
            pstmt.setString(4, app.getPersonalStatement());
            pstmt.setString(5, app.getOtherScholarships());

            int affectedRows = pstmt.executeUpdate();
            System.out.println("[DEBUG] Rows affected: " + affectedRows);

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int appId = generatedKeys.getInt(1);
                        System.out.println("[DEBUG] Generated AppID: " + appId);

                        // Save documents
                        saveDocuments(appId, app.getDocuments(), conn);
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

    private void saveDocuments(int appId, List<Document> documents, Connection conn) throws SQLException {
        if (documents == null || documents.isEmpty())
            return;

        String sql = "INSERT INTO Document (appID, fileName, fileType) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Document doc : documents) {
                pstmt.setInt(1, appId);
                pstmt.setString(2, doc.getFileName());
                pstmt.setString(3, doc.getFileType());
                pstmt.addBatch();
            }
            pstmt.executeBatch();
        }
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
                    app.setPersonalStatement(rs.getString("personalStatement"));
                    app.setOtherScholarships(rs.getString("otherScholarships"));
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
        String sql = "SELECT a.*, s.title, u_app.fullName as applicantName, e.reviewerID, u_rev.fullName as reviewerName "
                +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "JOIN \"User\" u_app ON st.userID = u_app.userID " +
                "LEFT JOIN Evaluation e ON a.appID = e.appID " +
                "LEFT JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "LEFT JOIN \"User\" u_rev ON r.userID = u_rev.userID " +
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
                            rs.getString("reviewerID"),
                            rs.getString("reviewerName"),
                            rs.getString("applicantName"));
                    app.setPersonalStatement(rs.getString("personalStatement"));
                    app.setOtherScholarships(rs.getString("otherScholarships"));
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
