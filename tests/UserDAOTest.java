package tests;

import dao.UserDAO;
import model.*;
import db.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserDAOTest {
    private static UserDAO userDAO;
    private static final String TEST_EMAIL = "integration_test_user@mmu.edu.my";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Integration Test User";
    private static final String TEST_STUDENT_ID = "S123456789";

    @BeforeAll
    static void setUp() {
        userDAO = new UserDAO();
        cleanUp();
        createTestStudent();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
    }

    private static void cleanUp() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement("DELETE FROM Student WHERE email = ?");
            pstmt.setString(1, TEST_EMAIL);
            pstmt.executeUpdate();

            pstmt = conn.prepareStatement("DELETE FROM \"User\" WHERE email = ?");
            pstmt.setString(1, TEST_EMAIL);
            pstmt.executeUpdate();
        } catch (Exception e) {
        }
    }

    private static void createTestStudent() {
        String sql = "INSERT INTO Student (fullName, email, password, role, isActive, studentID, cgpa, major) " +
                "VALUES (?, ?, ?, 'Student', true, ?, 3.8, 'Computer Science')";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, TEST_NAME);
            pstmt.setString(2, TEST_EMAIL);
            pstmt.setString(3, TEST_PASSWORD);
            pstmt.setString(4, TEST_STUDENT_ID);
            pstmt.executeUpdate();
        } catch (Exception e) {
            System.err.println("Setup failed: " + e.getMessage());
        }
    }

    @Test
    @Order(1)
    void testAuthenticateSuccess() {
        User user = userDAO.authenticate(TEST_EMAIL, TEST_PASSWORD);
        assertNotNull(user, "Auth failed for email: " + TEST_EMAIL);
        assertEquals(TEST_NAME, user.getFullName());
    }

    @Test
    @Order(2)
    void testAuthenticateByStudentID() {
        User user = userDAO.authenticate(TEST_STUDENT_ID, TEST_PASSWORD);
        assertNotNull(user, "Auth failed for ID: " + TEST_STUDENT_ID);
    }
}
