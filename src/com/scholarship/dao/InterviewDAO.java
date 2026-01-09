package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Interview;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewDAO {

    public boolean schedule(Interview interview) {
        String sql = "INSERT INTO Interview (evalID, dateTime, venueOrLink) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, interview.getEvalID());
            pstmt.setTimestamp(2, interview.getDateTime());
            pstmt.setString(3, interview.getVenueOrLink());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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
