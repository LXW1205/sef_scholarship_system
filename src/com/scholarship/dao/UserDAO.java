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
                            return new Student(userId, uRs.getString("fullName"), uRs.getString("email"),
                                    uRs.getBoolean("isActive"), uRs.getString("studentID"),
                                    uRs.getDouble("cgpa"), uRs.getString("major"), uRs.getString("qualification"),
                                    uRs.getString("yearOfStudy"), uRs.getString("expectedGraduation"),
                                    uRs.getDouble("familyIncome"));
                        }
                    }
                }
                break;
            case "Reviewer":
                String revSql = "SELECT * FROM Reviewer WHERE userID = ?";
                try (PreparedStatement rStmt = conn.prepareStatement(revSql)) {
                    rStmt.setInt(1, userId);
                    try (ResultSet rRs = rStmt.executeQuery()) {
                        if (rRs.next()) {
                            return new Reviewer(userId, rRs.getString("fullName"), rRs.getString("email"),
                                    rRs.getBoolean("isActive"), rRs.getString("reviewerID"),
                                    rRs.getString("department"));
                        }
                    }
                }
                break;
            case "Committee":
            case "CommitteeMember":
                String cmSql = "SELECT * FROM CommitteeMember WHERE userID = ?";
                try (PreparedStatement cmStmt = conn.prepareStatement(cmSql)) {
                    cmStmt.setInt(1, userId);
                    try (ResultSet cmRs = cmStmt.executeQuery()) {
                        if (cmRs.next()) {
                            return new CommitteeMember(userId, cmRs.getString("fullName"), cmRs.getString("email"),
                                    cmRs.getBoolean("isActive"), cmRs.getString("committeeID"),
                                    cmRs.getString("position"));
                        }
                    }
                }
                break;
            case "Admin":
                String adminSql = "SELECT * FROM Admin WHERE userID = ?";
                try (PreparedStatement aStmt = conn.prepareStatement(adminSql)) {
                    aStmt.setInt(1, userId);
                    try (ResultSet aRs = aStmt.executeQuery()) {
                        if (aRs.next()) {
                            return new Admin(userId, aRs.getString("fullName"), aRs.getString("email"),
                                    aRs.getBoolean("isActive"), aRs.getString("adminID"),
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
        String tableName = "\"User\"";
        if (user instanceof Student)
            tableName = "Student";
        else if (user instanceof Reviewer)
            tableName = "Reviewer";
        else if (user instanceof CommitteeMember)
            tableName = "CommitteeMember";
        else if (user instanceof Admin)
            tableName = "Admin";

        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET fullName = ?");
        if (newPassword != null && !newPassword.isEmpty()) {
            sql.append(", password = ?");
        }

        if (user instanceof Student) {
            sql.append(
                    ", cgpa = ?, major = ?, qualification = ?, yearOfStudy = ?, expectedGraduation = ?, familyIncome = ?");
        } else if (user instanceof Reviewer) {
            sql.append(", department = ?");
        } else if (user instanceof CommitteeMember) {
            sql.append(", position = ?");
        } else if (user instanceof Admin) {
            sql.append(", adminLevel = ?");
        }

        sql.append(" WHERE userID = ?");

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int i = 1;
            pstmt.setString(i++, user.getFullName());
            if (newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(i++, newPassword);
            }

            if (user instanceof Student) {
                Student s = (Student) user;
                pstmt.setDouble(i++, s.getCgpa());
                pstmt.setString(i++, s.getMajor());
                pstmt.setString(i++, s.getQualification());
                pstmt.setString(i++, s.getYearOfStudy());
                pstmt.setString(i++, s.getExpectedGraduation());
                pstmt.setDouble(i++, s.getFamilyIncome());
            } else if (user instanceof Reviewer) {
                pstmt.setString(i++, ((Reviewer) user).getDepartment());
            } else if (user instanceof CommitteeMember) {
                pstmt.setString(i++, ((CommitteeMember) user).getPosition());
            } else if (user instanceof Admin) {
                pstmt.setString(i++, ((Admin) user).getAdminLevel());
            }

            pstmt.setInt(i, user.getId());
            return pstmt.executeUpdate() > 0; // 1 or more rows (Postgres updates parent too)
            // Actually it returns 1 for child update.
            // Wait, standard return for child update is 1.
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
