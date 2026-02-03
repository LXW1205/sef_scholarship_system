package tests;

import dao.InquiryDAO;
import model.Inquiry;
import db.DatabaseConnection;
import org.junit.jupiter.api.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class InquiryDAOTest {
    private static InquiryDAO inquiryDAO;
    private static final String TEST_STUDENT_ID = "S123456789"; // Existing student from UserDAOTest
    private static int testInquiryID;

    @BeforeAll
    static void setUp() {
        inquiryDAO = new InquiryDAO();
        // Ensure student exists for FK constraint
        createTestStudent();
    }

    @AfterAll
    static void tearDown() {
        cleanUp();
    }

    private static void createTestStudent() {
        String sql = "INSERT INTO Student (fullName, email, password, role, isActive, studentID) " +
                "VALUES ('Test Student', 'inquiry_test@mmu.edu.my', 'pass', 'Student', true, ?) " +
                "ON CONFLICT (studentID) DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, TEST_STUDENT_ID);
            pstmt.executeUpdate();
        } catch (Exception e) {
        }
    }

    private static void cleanUp() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql1 = "DELETE FROM Inquiry WHERE studentID = ?";
            try (PreparedStatement p1 = conn.prepareStatement(sql1)) {
                p1.setString(1, TEST_STUDENT_ID);
                p1.executeUpdate();
            }
            String sql2 = "DELETE FROM Student WHERE studentID = ?";
            try (PreparedStatement p2 = conn.prepareStatement(sql2)) {
                p2.setString(1, TEST_STUDENT_ID);
                p2.executeUpdate();
            }
        } catch (Exception e) {
        }
    }

    @Test
    @Order(1)
    void testCreateInquiry() {
        Inquiry i = new Inquiry(0, TEST_STUDENT_ID, "This is a test question?", null);
        boolean created = inquiryDAO.create(i);
        assertTrue(created);

        List<Inquiry> studentInquiries = inquiryDAO.findByStudentId(TEST_STUDENT_ID);
        assertFalse(studentInquiries.isEmpty());
        testInquiryID = studentInquiries.get(0).getInquiryID();
    }

    @Test
    @Order(2)
    void testFindById() {
        Inquiry found = inquiryDAO.findById(testInquiryID);
        assertNotNull(found);
        assertEquals(TEST_STUDENT_ID, found.getStudentID());
        assertEquals("Pending", found.getStatus());
    }

    @Test
    @Order(3)
    void testUpdateAnswer() {
        String answer = "This is a test answer from admin.";
        boolean updated = inquiryDAO.updateAnswer(testInquiryID, answer);
        assertTrue(updated);

        Inquiry verified = inquiryDAO.findById(testInquiryID);
        assertEquals("Answered", verified.getStatus());
        assertEquals(answer, verified.getAnswer());
        assertNotNull(verified.getAnsweredAt());
    }

    @Test
    @Order(4)
    void testFindAllWithDetails() {
        List<Map<String, Object>> details = inquiryDAO.findAllWithDetails();
        assertFalse(details.isEmpty());
        assertTrue(details.stream().anyMatch(m -> (int) m.get("inquiryID") == testInquiryID));
    }
}
