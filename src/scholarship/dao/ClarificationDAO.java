package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.ClarificationRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClarificationDAO {

    public boolean create(ClarificationRequest request) {
        String sql = "INSERT INTO ClarificationRequest (evalID, question, status, requestedDate) VALUES (?, ?, 'Pending', CURRENT_TIMESTAMP)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, request.getEvalID());
            pstmt.setString(2, request.getQuestion());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateAnswer(int reqId, String answer) {
        String sql = "UPDATE ClarificationRequest SET answer = ?, status = 'Answered', answeredDate = CURRENT_TIMESTAMP WHERE reqID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, answer);
            pstmt.setInt(2, reqId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isReviewerOwner(int reqId, String reviewerID) {
        String sql = "SELECT count(*) FROM ClarificationRequest c " +
                "JOIN Evaluation e ON c.evalID = e.evalID " +
                "WHERE c.reqID = ? AND e.reviewerID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reqId);
            pstmt.setString(2, reviewerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Map<String, Object>> getRequestsForReviewer(String reviewerID) {
        List<Map<String, Object>> requests = new ArrayList<>();
        String sql = "SELECT c.*, e.appID, s.fullName as studentName, a.title as scholarshipTitle " +
                "FROM ClarificationRequest c " +
                "JOIN Evaluation e ON c.evalID = e.evalID " +
                "JOIN Application app ON e.appID = app.appID " +
                "JOIN Student s ON app.studentID = s.studentID " +
                "JOIN Scholarship a ON app.scholarshipID = a.scholarshipID " +
                "WHERE e.reviewerID = ? " +
                "ORDER BY c.requestedDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reviewerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> req = mapResultSetToMap(rs);
                    req.put("studentName", rs.getString("studentName"));
                    req.put("scholarshipTitle", rs.getString("scholarshipTitle"));
                    requests.add(req);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    public List<Map<String, Object>> getAllRequestsForCommittee() {
        List<Map<String, Object>> requests = new ArrayList<>();
        String sql = "SELECT c.*, e.appID, r.fullName as reviewerName, s.fullName as studentName " +
                "FROM ClarificationRequest c " +
                "JOIN Evaluation e ON c.evalID = e.evalID " +
                "JOIN Reviewer r ON e.reviewerID = r.reviewerID " +
                "JOIN Application app ON e.appID = app.appID " +
                "JOIN Student s ON app.studentID = s.studentID " +
                "ORDER BY c.requestedDate DESC";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Map<String, Object> req = mapResultSetToMap(rs);
                req.put("studentName", rs.getString("studentName"));
                req.put("reviewerName", rs.getString("reviewerName"));
                requests.add(req);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    private Map<String, Object> mapResultSetToMap(ResultSet rs) throws SQLException {
        Map<String, Object> req = new HashMap<>();
        req.put("reqID", rs.getInt("reqID"));
        req.put("evalID", rs.getInt("evalID"));
        req.put("question", rs.getString("question"));
        req.put("answer", rs.getString("answer"));
        req.put("status", rs.getString("status"));
        req.put("requestedDate", rs.getTimestamp("requestedDate").toString());
        if (rs.getTimestamp("answeredDate") != null) {
            req.put("answeredDate", rs.getTimestamp("answeredDate").toString());
        }
        return req;
    }
}
