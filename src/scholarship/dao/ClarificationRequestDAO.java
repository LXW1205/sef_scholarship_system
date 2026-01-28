package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.ClarificationRequest;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClarificationRequestDAO {

    public boolean create(ClarificationRequest req) {
        String sql = "INSERT INTO ClarificationRequest (evalID, question) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, req.getEvalID());
            pstmt.setString(2, req.getQuestion());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<ClarificationRequest> findByEvalId(int evalId) {
        List<ClarificationRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM ClarificationRequest WHERE evalID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, evalId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    requests.add(new ClarificationRequest(
                            rs.getInt("reqID"),
                            rs.getInt("evalID"),
                            rs.getString("question"),
                            rs.getString("answer"),
                            rs.getString("status"),
                            rs.getTimestamp("requestedDate"),
                            rs.getTimestamp("answeredDate")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }
}
