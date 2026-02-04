package dao;

import db.DatabaseConnection;
import model.Scholarship;
import model.Criterion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScholarshipDAO {

    public List<Scholarship> findAllActive() {
        return findFiltered("isActive = true");
    }

    public List<Scholarship> findAll() {
        return findFiltered(null);
    }

    private List<Scholarship> findFiltered(String condition) {
        List<Scholarship> scholarships = new ArrayList<>();
        String sql = "SELECT * FROM Scholarship " + (condition != null ? "WHERE " + condition : "")
                + " ORDER BY deadline";

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
                        rs.getDouble("minCGPA"),
                        rs.getDouble("maxFamilyIncome"),
                        rs.getBoolean("requiresInterview"),
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
                            rs.getDouble("minCGPA"),
                            rs.getDouble("maxFamilyIncome"),
                            rs.getBoolean("requiresInterview"),
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

    public int create(Scholarship s) {
        String sql = "INSERT INTO Scholarship (title, description, amount, forQualification, deadline, minCGPA, maxFamilyIncome, requiresInterview, isActive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING scholarshipID";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getTitle());
            pstmt.setString(2, s.getDescription());
            pstmt.setString(3, s.getAmount());
            pstmt.setString(4, s.getForQualification());
            pstmt.setDate(5, s.getDeadline());
            pstmt.setDouble(6, s.getMinCGPA());
            pstmt.setDouble(7, s.getMaxFamilyIncome());
            pstmt.setBoolean(8, s.requiresInterview());
            pstmt.setBoolean(9, s.isActive());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    // Add criteria
                    for (Criterion c : s.getCriteria()) {
                        c.setScholarshipID(newId); // Ensure linked to new ID
                        createCriterion(c, conn);
                    }
                    return newId;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public boolean update(Scholarship s) {
        String sql = "UPDATE Scholarship SET title = ?, description = ?, amount = ?, forQualification = ?, deadline = ?, minCGPA = ?, maxFamilyIncome = ?, requiresInterview = ?, isActive = ? WHERE scholarshipID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, s.getTitle());
            pstmt.setString(2, s.getDescription());
            pstmt.setString(3, s.getAmount());
            pstmt.setString(4, s.getForQualification());
            pstmt.setDate(5, s.getDeadline());
            pstmt.setDouble(6, s.getMinCGPA());
            pstmt.setDouble(7, s.getMaxFamilyIncome());
            pstmt.setBoolean(8, s.requiresInterview());
            pstmt.setBoolean(9, s.isActive());
            pstmt.setInt(10, s.getScholarshipID());

            int updated = pstmt.executeUpdate();
            if (updated > 0) {
                // Update criteria - simpler to delete all and recreate
                deleteCriteriaByScholarshipId(s.getScholarshipID(), conn);
                for (Criterion c : s.getCriteria()) {
                    c.setScholarshipID(s.getScholarshipID());
                    createCriterion(c, conn);
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void createCriterion(Criterion c, Connection conn) throws SQLException {
        String sql = "INSERT INTO Criteria (scholarshipID, name, weightage, maxScore, mappedField) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, c.getScholarshipID());
            pstmt.setString(2, c.getName());
            pstmt.setInt(3, c.getWeightage());
            pstmt.setDouble(4, c.getMaxScore());
            pstmt.setString(5, c.getMappedField());
            pstmt.executeUpdate();
        }
    }

    private void deleteCriteriaByScholarshipId(int scholarshipId, Connection conn) throws SQLException {
        String sql = "DELETE FROM Criteria WHERE scholarshipID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, scholarshipId);
            pstmt.executeUpdate();
        }
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
                            rs.getDouble("maxScore"),
                            rs.getString("mappedField")));
                }
            }
        }
        return criteria;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM Scholarship WHERE scholarshipID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
