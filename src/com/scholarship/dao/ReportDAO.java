package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.Report;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    public boolean save(Report report) {
        String sql = "INSERT INTO Report (adminID, type, generatedFile) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, report.getAdminID());
            pstmt.setString(2, report.getType());
            pstmt.setString(3, report.getGeneratedFile());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Report> findAll() {
        List<Report> reports = new ArrayList<>();
        String sql = "SELECT * FROM Report ORDER BY generatedDate DESC";
        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                reports.add(new Report(
                        rs.getInt("reportID"),
                        rs.getString("adminID"),
                        rs.getString("type"),
                        rs.getString("generatedFile"),
                        rs.getTimestamp("generatedDate")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return reports;
    }
}
