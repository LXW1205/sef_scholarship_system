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

    public User authenticate(String email, String password) {
        String sql = "SELECT userID, fullName, email, role, isActive FROM \"User\" WHERE email = ? AND password = ? AND isActive = true";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, password);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return fetchDetailedUser(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                            rs.getString("role"), rs.getBoolean("isActive"), conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userID, fullName, email, role, isActive FROM \"User\" ORDER BY userID";

        try (Connection conn = DatabaseConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(fetchDetailedUser(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                        rs.getString("role"), rs.getBoolean("isActive"), conn));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<User> findByRole(String role) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userID, fullName, email, role, isActive FROM \"User\" WHERE role = ? ORDER BY userID";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    users.add(fetchDetailedUser(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                            rs.getString("role"), rs.getBoolean("isActive"), conn));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    private User fetchDetailedUser(int userId, String fullName, String email, String role, boolean isActive,
            Connection conn) throws SQLException {
        switch (role) {
            case "Student":
                String studentSql = "SELECT * FROM Student WHERE userID = ?";
                try (PreparedStatement uStmt = conn.prepareStatement(studentSql)) {
                    uStmt.setInt(1, userId);
                    try (ResultSet uRs = uStmt.executeQuery()) {
                        if (uRs.next()) {
                            return new Student(userId, fullName, email, isActive, uRs.getString("studentID"),
                                    uRs.getDouble("cgpa"), uRs.getString("major"), uRs.getString("qualification"),
                                    uRs.getString("yearOfStudy"), uRs.getString("expectedGraduation"),
                                    uRs.getDouble("familyIncome"));
                        }
                    }
                }
                break;
            case "Reviewer":
                String revSql = "SELECT reviewerID, department FROM Reviewer WHERE userID = ?";
                try (PreparedStatement rStmt = conn.prepareStatement(revSql)) {
                    rStmt.setInt(1, userId);
                    try (ResultSet rRs = rStmt.executeQuery()) {
                        if (rRs.next()) {
                            return new Reviewer(userId, fullName, email, isActive, rRs.getString("reviewerID"),
                                    rRs.getString("department"));
                        }
                    }
                }
                break;
            case "Committee":
            case "CommitteeMember":
                String cmSql = "SELECT committeeID, position FROM CommitteeMember WHERE userID = ?";
                try (PreparedStatement cmStmt = conn.prepareStatement(cmSql)) {
                    cmStmt.setInt(1, userId);
                    try (ResultSet cmRs = cmStmt.executeQuery()) {
                        if (cmRs.next()) {
                            return new CommitteeMember(userId, fullName, email, isActive, cmRs.getString("committeeID"),
                                    cmRs.getString("position"));
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
                            return new Admin(userId, fullName, email, isActive, aRs.getString("adminID"),
                                    aRs.getString("adminLevel"));
                        }
                    }
                }
                break;
        }
        // Fallback for generic or missing detail records (should arguably be an error
        // or abstract implementation)
        return new User(userId, fullName, email, role, isActive) {
            @Override
            public boolean login() {
                return true;
            }

            @Override
            public void logout() {
            }
        };
    }

    public User findById(int id) {
        String sql = "SELECT userID, fullName, email, role, isActive FROM \"User\" WHERE userID = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return fetchDetailedUser(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                            rs.getString("role"), rs.getBoolean("isActive"), conn);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean update(User user, String newPassword) {
        StringBuilder sql = new StringBuilder("UPDATE \"User\" SET fullName = ?");
        if (newPassword != null && !newPassword.isEmpty()) {
            sql.append(", password = ?");
        }
        sql.append(" WHERE userID = ?");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            pstmt.setString(1, user.getFullName());

            int paramIndex = 2;
            if (newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(paramIndex++, newPassword);
            }

            pstmt.setInt(paramIndex, user.getId());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
