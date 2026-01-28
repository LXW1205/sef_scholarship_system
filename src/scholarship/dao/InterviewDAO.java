package scholarship.dao;

import scholarship.db.DatabaseConnection;
import scholarship.model.Interview;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAO {

    public int schedule(Interview interview) {
        String sql = "INSERT INTO Interview (evalID, dateTime, venueOrLink, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, interview.getEvalID());
            pstmt.setTimestamp(2, interview.getDateTime());
            pstmt.setString(3, interview.getVenueOrLink());
            pstmt.setString(4, interview.getStatus() != null ? interview.getStatus() : "Scheduled");

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean update(Interview interview) {
        String sql = "UPDATE Interview SET dateTime = ?, venueOrLink = ?, status = ? WHERE interviewID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setTimestamp(1, interview.getDateTime());
            pstmt.setString(2, interview.getVenueOrLink());
            pstmt.setString(3, interview.getStatus());
            pstmt.setInt(4, interview.getInterviewID());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int interviewID) {
        String sql = "DELETE FROM Interview WHERE interviewID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, interviewID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Interview findById(int interviewID) {
        String sql = "SELECT * FROM Interview WHERE interviewID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, interviewID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Interview(
                            rs.getInt("interviewID"),
                            rs.getInt("evalID"),
                            rs.getTimestamp("dateTime"),
                            rs.getString("venueOrLink"),
                            rs.getString("status"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Interview> findAll() {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM Interview ORDER BY dateTime DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql);
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                interviews.add(new Interview(
                        rs.getInt("interviewID"),
                        rs.getInt("evalID"),
                        rs.getTimestamp("dateTime"),
                        rs.getString("venueOrLink"),
                        rs.getString("status")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return interviews;
    }

    public List<Interview> findByEvalId(int evalId) {
        List<Interview> interviews = new ArrayList<>();
        String sql = "SELECT * FROM Interview WHERE evalID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, evalId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    interviews.add(new Interview(
                            rs.getInt("interviewID"),
                            rs.getInt("evalID"),
                            rs.getTimestamp("dateTime"),
                            rs.getString("venueOrLink"),
                            rs.getString("status")));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return interviews;
    }
}
