package com.scholarship.dao;

import com.scholarship.db.DatabaseConnection;
import com.scholarship.model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    
    public User authenticate(String username, String password) {
        String sql = "SELECT userID, username, email, role, isActive FROM \"User\" WHERE (username = ? OR email = ?) AND password = ? AND isActive = true";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, username);
            pstmt.setString(2, username); 
            pstmt.setString(3, password);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return fetchDetailedUser(rs.getInt("userID"), rs.getString("username"), rs.getString("email"), rs.getString("role"), rs.getBoolean("isActive"), conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userID, username, email, role, isActive FROM \"User\" ORDER BY userID";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                users.add(fetchDetailedUser(rs.getInt("userID"), rs.getString("username"), rs.getString("email"), rs.getString("role"), rs.getBoolean("isActive"), conn));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private User fetchDetailedUser(int userId, String username, String email, String role, boolean isActive, Connection conn) throws SQLException {
        switch (role) {
            case "Student":
                String studentSql = "SELECT studentID, fullName, cgpa FROM Student WHERE userID = ?";
                try (PreparedStatement uStmt = conn.prepareStatement(studentSql)) {
                    uStmt.setInt(1, userId);
                    try (ResultSet uRs = uStmt.executeQuery()) {
                        if (uRs.next()) {
                            return new Student(userId, username, email, isActive, uRs.getString("studentID"), uRs.getString("fullName"), uRs.getDouble("cgpa"));
                        }
                    }
                }
                break;
            case "Reviewer":
                String revSql = "SELECT staffID, department FROM Reviewer WHERE userID = ?";
                try (PreparedStatement rStmt = conn.prepareStatement(revSql)) {
                    rStmt.setInt(1, userId);
                    try (ResultSet rRs = rStmt.executeQuery()) {
                        if (rRs.next()) {
                            return new Reviewer(userId, username, email, isActive, rRs.getString("staffID"), rRs.getString("department"));
                        }
                    }
                }
                break;
            case "CommitteeMember":
                String cmSql = "SELECT memberID, position FROM CommitteeMember WHERE userID = ?";
                try (PreparedStatement cmStmt = conn.prepareStatement(cmSql)) {
                    cmStmt.setInt(1, userId);
                    try (ResultSet cmRs = cmStmt.executeQuery()) {
                        if (cmRs.next()) {
                            return new CommitteeMember(userId, username, email, isActive, cmRs.getInt("memberID"), cmRs.getString("position"));
                        }
                    }
                }
                break;
            case "Admin":
                String adminSql = "SELECT adminID, adminLevel FROM Admin WHERE userID = ?";
                try (PreparedStatement aStmt = conn.prepareStatement(adminSql)) {
                    aStmt.setInt(1, userId);
                    try (ResultSet aRs = aStmt.executeQuery()) {
                        if (aRs.next()) {
                            return new Admin(userId, username, email, isActive, aRs.getInt("adminID"), aRs.getString("adminLevel"));
                        }
                    }
                }
                break;
        }
        // Fallback for generic or missing detail records (should arguably be an error or abstract implementation)
        return new User(userId, username, email, role, isActive) {
            @Override public boolean login() { return true; }
            @Override public void logout() {}
        };
    }
}
