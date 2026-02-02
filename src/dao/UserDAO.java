package dao;

import db.DatabaseConnection;
import model.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User authenticate(String identifier, String password) {
        if (identifier == null || password == null)
            return null;

        // Determine if logging in by Email or ID
        if (identifier.contains("@")) {
            // Login by Email
            String sql = "SELECT userID, fullName, email, password, role, isActive FROM \"User\" WHERE LOWER(email) = LOWER(?) AND password = ? AND isActive = true";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, identifier);
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
        } else {
            String table = "";
            String idCol = "";

            identifier = identifier.trim();
            char prefix = identifier.isEmpty() ? ' ' : identifier.toUpperCase().charAt(0);
            switch (prefix) {
                case 'S':
                    table = "Student";
                    idCol = "studentID";
                    break;
                case 'R':
                    table = "Reviewer";
                    idCol = "reviewerID";
                    break;
                case 'C':
                    table = "CommitteeMember";
                    idCol = "committeeID";
                    break;
                case 'A':
                    table = "Admin";
                    idCol = "adminID";
                    break;
                default:
                    // Unknown prefix, potentially fail or try generic?
                    // But IDs are strict. Return null.
                    return null;
            }

            String sql = "SELECT userID, fullName, email, password, role, isActive FROM " + table + " WHERE UPPER("
                    + idCol
                    + ") = UPPER(?) AND password = ? AND isActive = true";
            try (Connection conn = DatabaseConnection.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, identifier);
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
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT userID, fullName, email, password, role, isActive FROM \"User\" ORDER BY userID";

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
        String sql = "SELECT userID, fullName, email, password, role, isActive FROM \"User\" WHERE role = ? ORDER BY userID";

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
                            System.out.println("[DEBUG] Detailed Student found: ID=" + uRs.getString("studentID") +
                                    ", Qual=" + uRs.getString("qualification") +
                                    ", Year=" + uRs.getString("yearOfStudy"));
                            String rawGrad = uRs.getString("expectedGraduation");
                            String fmtGrad = (rawGrad != null && rawGrad.length() >= 7) ? rawGrad.substring(0, 7)
                                    : rawGrad;

                            Student s = new Student(userId, uRs.getString("fullName"), uRs.getString("email"),
                                    uRs.getBoolean("isActive"), uRs.getString("studentID"),
                                    uRs.getDouble("cgpa"), uRs.getString("major"), uRs.getString("qualification"),
                                    uRs.getString("yearOfStudy"), fmtGrad,
                                    uRs.getDouble("familyIncome"));
                            s.setPassword(uRs.getString("password"));
                            return s;
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
                            Reviewer r = new Reviewer(userId, rRs.getString("fullName"), rRs.getString("email"),
                                    rRs.getBoolean("isActive"), rRs.getString("reviewerID"),
                                    rRs.getString("department"));
                            r.setPassword(rRs.getString("password"));
                            return r;
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
                            CommitteeMember c = new CommitteeMember(userId, cmRs.getString("fullName"),
                                    cmRs.getString("email"),
                                    cmRs.getBoolean("isActive"), cmRs.getString("committeeID"),
                                    cmRs.getString("position"));
                            c.setPassword(cmRs.getString("password"));
                            return c;
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
                            Admin a = new Admin(userId, aRs.getString("fullName"), aRs.getString("email"),
                                    aRs.getBoolean("isActive"), aRs.getString("adminID"),
                                    aRs.getString("adminLevel"));
                            a.setPassword(aRs.getString("password"));
                            return a;
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
        String sql = "SELECT userID, fullName, email, password, role, isActive FROM \"User\" WHERE userID = ?";

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

        StringBuilder sql = new StringBuilder("UPDATE " + tableName + " SET fullName = ?, isActive = ?");
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
            pstmt.setBoolean(i++, user.isActive()); // Set isActive

            if (newPassword != null && !newPassword.isEmpty()) {
                pstmt.setString(i++, newPassword);
            }

            if (user instanceof Student) {
                Student s = (Student) user;
                pstmt.setDouble(i++, s.getCgpa());
                pstmt.setString(i++, s.getMajor());
                pstmt.setString(i++, s.getQualification());
                pstmt.setString(i++, s.getYearOfStudy());
                String gradDate = s.getExpectedGraduation();
                if (gradDate != null && gradDate.length() == 7 && gradDate.contains("-")) {
                    gradDate += "-01"; // Convert YYYY-MM to YYYY-MM-DD
                }
                if (gradDate != null && !gradDate.isEmpty()) {
                    try {
                        pstmt.setDate(i++, java.sql.Date.valueOf(gradDate));
                    } catch (IllegalArgumentException e) {
                        System.err.println("Invalid date format for expectedGraduation: " + gradDate);
                        pstmt.setNull(i++, java.sql.Types.DATE);
                    }
                } else {
                    pstmt.setNull(i++, java.sql.Types.DATE);
                }
                pstmt.setDouble(i++, s.getFamilyIncome());
            } else if (user instanceof Reviewer) {
                pstmt.setString(i++, ((Reviewer) user).getDepartment());
            } else if (user instanceof CommitteeMember) {
                pstmt.setString(i++, ((CommitteeMember) user).getPosition());
            } else if (user instanceof Admin) {
                pstmt.setString(i++, ((Admin) user).getAdminLevel());
            }

            pstmt.setInt(i, user.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM \"User\" WHERE userID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT COUNT(*) FROM \"User\" WHERE LOWER(email) = LOWER(?)";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public User verifyUserForReset(String identifier, String fullName, String role) {
        // identifier is studentID, reviewerID, committeeID, or adminID
        String table = "\"User\"";
        String idColumn = "";

        switch (role) {
            case "Student":
                table = "Student";
                idColumn = "studentID";
                break;
            case "Reviewer":
                table = "Reviewer";
                idColumn = "reviewerID";
                break;
            case "Committee":
            case "CommitteeMember":
                table = "CommitteeMember";
                idColumn = "committeeID";
                break;
            case "Admin":
                table = "Admin";
                idColumn = "adminID";
                break;
            default:
                return null;
        }

        String sql = "SELECT userID, fullName, email, role, isActive FROM " + table + " WHERE " + idColumn
                + " = ? AND fullName = ? AND role = ?";

        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, identifier);
            pstmt.setString(2, fullName);
            pstmt.setString(3, role);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("userID"), rs.getString("fullName"), rs.getString("email"),
                            rs.getString("role"), rs.getBoolean("isActive")) {
                        @Override
                        public boolean login() {
                            return true;
                        }

                        @Override
                        public void logout() {
                        }
                    };
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findUserByRoleID(String id, String role) {
        String table = "";
        String idColumn = "";

        switch (role) {
            case "Student":
                table = "Student";
                idColumn = "studentID";
                break;
            case "Reviewer":
                table = "Reviewer";
                idColumn = "reviewerID";
                break;
            case "Committee":
            case "CommitteeMember":
                table = "CommitteeMember";
                idColumn = "committeeID";
                break;
            case "Admin":
                table = "Admin";
                idColumn = "adminID";
                break;
            default:
                return null;
        }

        String sql = "SELECT userID, fullName, email, role, isActive FROM " + table + " WHERE " + idColumn + " = ?";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, id);
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
}
