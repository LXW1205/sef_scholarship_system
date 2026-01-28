package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.Inquiry;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InquiryDAO {

    public boolean create(Inquiry inquiry) {
        String sql = "INSERT INTO Inquiry (studentID, message, status, submittedAt) VALUES (?, ?, 'Pending', CURRENT_TIMESTAMP)";
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

    public List<Inquiry> findAll() {
        List<Inquiry> inquiries = new ArrayList<>();
        String sql = "SELECT * FROM Inquiry ORDER BY submittedAt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inquiries.add(mapResultSetToInquiry(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }

    public List<Inquiry> findByStudentId(String studentId) {
        List<Inquiry> inquiries = new ArrayList<>();
        String sql = "SELECT * FROM Inquiry WHERE studentID = ? ORDER BY submittedAt DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    inquiries.add(mapResultSetToInquiry(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }

    public List<Map<String, Object>> findByStudentIdWithDetails(String studentId) {
        List<Map<String, Object>> inquiries = new ArrayList<>();
        String sql = "SELECT i.*, s.fullName as studentName, s.email as studentEmail " +
                "FROM Inquiry i " +
                "JOIN Student s ON i.studentID = s.studentID " +
                "WHERE i.studentID = ? ORDER BY i.submittedAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, studentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    inquiries.add(mapResultSetToMap(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }

    public List<Map<String, Object>> findAllWithDetails() {
        List<Map<String, Object>> inquiries = new ArrayList<>();
        String sql = "SELECT i.*, s.fullName as studentName, s.email as studentEmail " +
                "FROM Inquiry i " +
                "JOIN Student s ON i.studentID = s.studentID " +
                "ORDER BY i.submittedAt DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inquiries.add(mapResultSetToMap(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inquiries;
    }

    public Inquiry findById(int id) {
        String sql = "SELECT * FROM Inquiry WHERE inquiryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInquiry(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean updateAnswer(int inquiryId, String answer) {
        String sql = "UPDATE Inquiry SET answer = ?, status = 'Answered', answeredAt = CURRENT_TIMESTAMP WHERE inquiryID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, answer);
            pstmt.setInt(2, inquiryId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Inquiry mapResultSetToInquiry(ResultSet rs) throws SQLException {
        Inquiry i = new Inquiry(
                rs.getInt("inquiryID"),
                rs.getString("studentID"),
                rs.getString("message"),
                rs.getTimestamp("submittedAt"));
        i.setStatus(rs.getString("status"));
        i.setAnswer(rs.getString("answer"));
        i.setAnsweredAt(rs.getTimestamp("answeredAt"));
        return i;
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> inquiry = new HashMap<>();
        inquiry.put("inquiryID", rs.getInt("inquiryID"));
        inquiry.put("studentID", rs.getString("studentID"));
        inquiry.put("studentName", rs.getString("studentName"));
        inquiry.put("studentEmail", rs.getString("studentEmail"));
        inquiry.put("message", rs.getString("message"));
        inquiry.put("answer", rs.getString("answer"));
        inquiry.put("status", rs.getString("status"));
        inquiry.put("submittedAt", rs.getTimestamp("submittedAt").toString());
        if (rs.getTimestamp("answeredAt") != null) {
            inquiry.put("answeredAt", rs.getTimestamp("answeredAt").toString());
        }
        return inquiry;
    }
}
