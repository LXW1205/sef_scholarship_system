package com.scholarship.model;

import com.scholarship.db.DatabaseConnection;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class Admin extends User {
    private int adminID;
    private String adminLevel;

    public Admin(int id, String username, String email, boolean isActive, int adminID, String adminLevel) {
        super(id, username, email, "Admin", isActive);
        this.adminID = adminID;
        this.adminLevel = adminLevel;
    }
    
    public Admin() {
        super();
        this.role = "Admin";
    }

    public int getAdminID() { return adminID; }
    public void setAdminID(int adminID) { this.adminID = adminID; }

    public String getAdminLevel() { return adminLevel; }
    public void setAdminLevel(String adminLevel) { this.adminLevel = adminLevel; }
    
    public DashboardData viewAdminAnalytics() {
        int totalUsers = 0;
        int activeScholarships = 0;
        int pendingApps = 0;
        int approvedMonth = 0;

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // users
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM \"User\"")) {
                if (rs.next()) totalUsers = rs.getInt(1);
            }
            // active scholarships
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Scholarship WHERE isActive = true")) {
                if (rs.next()) activeScholarships = rs.getInt(1);
            }
            // pending apps
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Pending'")) {
                if (rs.next()) pendingApps = rs.getInt(1);
            }
            // approved apps
             try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Application WHERE status = 'Approved'")) {
                if (rs.next()) approvedMonth = rs.getInt(1);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return new DashboardData(totalUsers, activeScholarships, pendingApps, approvedMonth);
    }

    @Override
    public boolean login() { return true; }
    @Override
    public void logout() { }
}
