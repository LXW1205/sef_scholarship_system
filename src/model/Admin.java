package model;

import db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Admin extends User {
    private String adminID;
    private String adminLevel;

    public Admin(int id, String fullName, String email, boolean isActive, String adminID, String adminLevel) {
        super(id, fullName, email, "Admin", isActive);
        this.adminID = adminID;
        this.adminLevel = adminLevel;
    }

    public Admin() {
        super();
        this.role = "Admin";
    }

    public String getAdminID() {
        return adminID;
    }

    public void setAdminID(String adminID) {
        this.adminID = adminID;
    }

    public String getAdminLevel() {
        return adminLevel;
    }

    public void setAdminLevel(String adminLevel) {
        this.adminLevel = adminLevel;
    }

    public DashboardData viewAdminAnalytics(String range) {
        int totalUsers = 0;
        int activeScholarships = 0;
        int pendingApps = 0;
        int approvedMonth = 0;
        java.util.Map<String, Integer> trend = new java.util.LinkedHashMap<>(); // LinkedHashMap for order
        java.util.Map<String, Integer> userRoles = new java.util.HashMap<>();
        java.util.Map<String, Integer> awardDistribution = new java.util.HashMap<>();
        java.util.Map<String, Integer> statusDistribution = new java.util.HashMap<>();

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // users
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"User\"")) {
                if (rs.next())
                    totalUsers = rs.getInt(1);
            }
            // active scholarships
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Scholarship WHERE isActive = true")) {
                if (rs.next())
                    activeScholarships = rs.getInt(1);
            }
            // pending apps
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Pending'")) {
                if (rs.next())
                    pendingApps = rs.getInt(1);
            }
            // approved apps
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Awarded'")) {
                if (rs.next())
                    approvedMonth = rs.getInt(1);
            }

            // Trend data query
            String trendSql = "SELECT DATE(submissionDate) as date, COUNT(*) as count FROM Application ";

            // Basic filtering logic
            if ("week".equalsIgnoreCase(range)) {
                trendSql += "WHERE submissionDate >= CURRENT_DATE - INTERVAL '7 days' ";
            } else if ("month".equalsIgnoreCase(range)) {
                trendSql += "WHERE submissionDate >= CURRENT_DATE - INTERVAL '30 days' ";
            } else if ("year".equalsIgnoreCase(range)) {
                trendSql += "WHERE submissionDate >= CURRENT_DATE - INTERVAL '1 year' ";
            }

            trendSql += "GROUP BY DATE(submissionDate) ORDER BY date ASC";

            try (ResultSet rs = stmt.executeQuery(trendSql)) {
                while (rs.next()) {
                    trend.put(rs.getString("date"), rs.getInt("count"));
                }
            }

            // User Role Distribution
            try (ResultSet rs = stmt.executeQuery("SELECT role, COUNT(*) FROM \"User\" GROUP BY role")) {
                while (rs.next()) {
                    userRoles.put(rs.getString(1), rs.getInt(2));
                }
            }

            // Award Distribution (Awarded Apps per Scholarship)
            try (ResultSet rs = stmt.executeQuery(
                    "SELECT s.title, COUNT(a.appID) FROM Application a JOIN Scholarship s ON a.scholarshipID = s.scholarshipID WHERE a.status = 'Awarded' GROUP BY s.title")) {
                while (rs.next()) {
                    awardDistribution.put(rs.getString(1), rs.getInt(2));
                }
            }

            // Status Distribution (Total Apps per Status)
            try (ResultSet rs = stmt.executeQuery("SELECT status, COUNT(*) FROM Application GROUP BY status")) {
                while (rs.next()) {
                    statusDistribution.put(rs.getString(1), rs.getInt(2));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new DashboardData(totalUsers, activeScholarships, pendingApps, approvedMonth, trend, userRoles,
                awardDistribution, statusDistribution);
    }

    // Overload for backward compatibility
    public DashboardData viewAdminAnalytics() {
        return viewAdminAnalytics("all");
    }

    @Override
    public boolean login() {
        return true;
    }

    @Override
    public void logout() {
    }
}
