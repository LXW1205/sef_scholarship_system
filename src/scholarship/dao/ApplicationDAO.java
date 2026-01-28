package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.Application;
import scholarship.model.Document;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ApplicationDAO {

    public ApplicationDAO() {
        migrateTable();
    }

    private void migrateTable() {
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {
            // Check if column exists
            ResultSet rs = conn.getMetaData().getColumns(null, null, "application", "decisioncomments");
            if (!rs.next()) {
                System.out.println("[INFO] Adding decisionComments column to Application table...");
                stmt.execute("ALTER TABLE Application ADD COLUMN decisionComments TEXT");
            }
        } catch (SQLException e) {
            // Might fail if already exists or table doesn't exist yet (handled by creation
            // script)
        }
    }

    public int save(Application app) {
        String sql = "INSERT INTO Application (studentID, scholarshipID, status, personalStatement, otherScholarships, decisionComments) VALUES (?, ?, ?, ?, ?, ?)";
        System.out.println("[DEBUG] ApplicationDAO saving: SQL=" + sql);

        Connection conn = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, app.getStudentID());
                pstmt.setInt(2, app.getScholarshipID());
                pstmt.setString(3, app.getStatus());
                pstmt.setString(4, app.getPersonalStatement());
                pstmt.setString(5, app.getOtherScholarships());
                pstmt.setString(6, app.getDecisionComments());

                int affectedRows = pstmt.executeUpdate();
                System.out.println("[DEBUG] Rows affected: " + affectedRows);

                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            int appId = generatedKeys.getInt(1);
                            System.out.println("[DEBUG] Generated AppID: " + appId);

                            // Save documents (part of transaction)
                            saveDocuments(appId, app.getDocuments(), conn);

                            conn.commit(); // Success!
                            return appId;
                        }
                    }
                }
                conn.rollback();
            }
        } catch (SQLException e) {
            System.err.println("[ERROR] ApplicationDAO save failed: " + e.getMessage());
            e.printStackTrace();
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        return 0;
    }

    private void saveDocuments(int appId, List<Document> documents, Connection conn) throws SQLException {
        if (documents == null || documents.isEmpty())
            return;

        String sql = "INSERT INTO Document (appID, fileName, fileType, fileContent) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Document doc : documents) {
                pstmt.setInt(1, appId);
                pstmt.setString(2, doc.getFileName());
                pstmt.setString(3, doc.getFileType());
                pstmt.setString(4, doc.getFileContent());
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

    public List<Application> findByStudentID(String studentId) {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT a.*, s.title, st.fullName as applicantName, st.email as applicantEmail, st.cgpa, st.major, st.yearOfStudy, st.qualification, st.familyIncome, e.reviewerID, r.fullName as reviewerName "
                +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "LEFT JOIN Evaluation e ON a.appID = e.appID " +
                "LEFT JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "WHERE a.studentID = ? " +
                "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(mapResultSetToApplication(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public List<Application> findByReviewerID(String reviewerId) {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT a.*, s.title, st.fullName as applicantName, st.email as applicantEmail, st.cgpa, st.major, st.yearOfStudy, st.qualification, st.familyIncome, e.reviewerID, r.fullName as reviewerName "
                +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "JOIN Evaluation e ON a.appID = e.appID " +
                "JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "WHERE e.reviewerID = ? " +
                "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reviewerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(mapResultSetToApplication(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public List<Application> findAll() {
        List<Application> apps = new ArrayList<>();
        String sql = "SELECT a.*, s.title, st.fullName as applicantName, st.email as applicantEmail, st.cgpa, st.major, st.yearOfStudy, st.qualification, st.familyIncome, e.reviewerID, r.fullName as reviewerName "
                +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "LEFT JOIN Evaluation e ON a.appID = e.appID " +
                "LEFT JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "ORDER BY a.submissionDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(mapResultSetToApplication(rs, conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return apps;
    }

    public boolean updateStatus(int appId, String status, String comments) {
        String sql = "UPDATE Application SET status = ?, decisionComments = ? WHERE appID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setString(2, comments);
            pstmt.setInt(3, appId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateStatus(int appId, String status) {
        return updateStatus(appId, status, null);
    }

    public Application findById(int appId) {
        String sql = "SELECT a.*, s.title, st.fullName as applicantName, st.email as applicantEmail, st.cgpa, st.major, st.yearOfStudy, st.qualification, st.familyIncome, e.reviewerID, r.fullName as reviewerName "
                +
                "FROM Application a " +
                "JOIN Scholarship s ON a.scholarshipID = s.scholarshipID " +
                "JOIN Student st ON a.studentID = st.studentID " +
                "LEFT JOIN Evaluation e ON a.appID = e.appID " +
                "LEFT JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "WHERE a.appID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToApplication(rs, conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Application mapResultSetToApplication(ResultSet rs, Connection conn) throws SQLException {
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
        app.setApplicantEmail(rs.getString("applicantEmail"));
        app.setCgpa(rs.getDouble("cgpa"));
        app.setMajor(rs.getString("major"));
        app.setYearOfStudy(rs.getString("yearOfStudy"));
        app.setQualification(rs.getString("qualification"));
        app.setFamilyIncome(rs.getDouble("familyIncome"));
        app.setPersonalStatement(rs.getString("personalStatement"));
        app.setOtherScholarships(rs.getString("otherScholarships"));
        app.setDecisionComments(rs.getString("decisionComments"));
        app.getDocuments().addAll(findDocumentsByAppId(app.getAppID(), conn));
        return app;
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
                            rs.getString("fileContent"),
                            rs.getTimestamp("uploadDate")));
                }
            }
        }
        return docs;
    }
}
