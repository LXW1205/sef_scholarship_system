package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Scholarship;
import com.scholarship.model.Criterion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipDAO {

    public List<Scholarship> findAllActive() {
        List<Scholarship> scholarships = new ArrayList<>();
        String sql = "SELECT * FROM Scholarship WHERE isActive = true ORDER BY deadline";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Scholarship s = new Scholarship(
                        rs.getInt("scholarshipID"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getString("amount"),
                        rs.getString("forQualification"),
                        rs.getDate("deadline"),
                        rs.getBoolean("isActive"));
                s.setCriteria(findCriteriaByScholarshipId(s.getScholarshipID(), conn));
                scholarships.add(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return scholarships;
    }

    public Scholarship findById(int id) {
        String sql = "SELECT * FROM Scholarship WHERE scholarshipID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Scholarship s = new Scholarship(
                            rs.getInt("scholarshipID"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("amount"),
                            rs.getString("forQualification"),
                            rs.getDate("deadline"),
                            rs.getBoolean("isActive"));
                    s.setCriteria(findCriteriaByScholarshipId(id, conn));
                    return s;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private List<Criterion> findCriteriaByScholarshipId(int scholarshipId, Connection conn) throws SQLException {
        List<Criterion> criteria = new ArrayList<>();
        String sql = "SELECT * FROM Criteria WHERE scholarshipID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scholarshipId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    criteria.add(new Criterion(
                            rs.getInt("criteriaID"),
                            rs.getInt("scholarshipID"),
                            rs.getString("name"),
                            rs.getInt("weightage"),
                            rs.getDouble("maxScore")));
                }
            }
        }
        return criteria;
    }
}
