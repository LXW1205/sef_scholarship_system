package tests;

import org.junit.jupiter.api.*;
import model.*;
import utils.JsonUtils;
import utils.StatusValidator;
import dao.UserDAO;
import dao.InquiryDAO;
import dao.ScholarshipDAO;
import db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DigitalScholarshipSystemTest {

    // --- Application Model Tests ---

    @Test
    void testApplicationInitialization() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Application app = new Application(1, "S123", 101, "Scholarship A", now, "Pending");

        assertEquals(1, app.getAppID());
        assertEquals("S123", app.getStudentID());
        assertEquals(101, app.getScholarshipID());
        assertEquals("Scholarship A", app.getScholarshipTitle());
        assertEquals(now, app.getSubmissionDate());
        assertEquals("Pending", app.getStatus());
    }

    @Test
    void testDocumentManagement() {
        Application app = new Application(1, "S123", 101, "Scholarship A", null, "Pending");
        assertTrue(app.getDocuments().isEmpty());

        Document doc1 = new Document(1, 1, "transcript.pdf", "PDF", "base64", null);
        app.addDocument(doc1);

        assertEquals(1, app.getDocuments().size());
        assertEquals(doc1, app.getDocuments().get(0));

        List<Document> docList = new ArrayList<>();
        docList.add(new Document(2, 1, "id.jpg", "JPG", "base64", null));
        app.setDocuments(docList);

        assertEquals(1, app.getDocuments().size());
        assertEquals("id.jpg", app.getDocuments().get(0).getFileName());
    }

    @Test
    void testSettersAndGetters() {
        Application app = new Application(1, "S123", 101, "Scholarship A", null, "Pending");

        app.setApplicantEmail("test@example.com");
        assertEquals("test@example.com", app.getApplicantEmail());

        app.setCgpa(3.85);
        assertEquals(3.85, app.getCgpa());

        app.setMajor("Computer Science");
        assertEquals("Computer Science", app.getMajor());

        app.setYearOfStudy("3");
        assertEquals("3", app.getYearOfStudy());

        app.setQualification("Degree");
        assertEquals("Degree", app.getQualification());

        app.setFamilyIncome(5000.0);
        assertEquals(5000.0, app.getFamilyIncome());

        app.setPersonalStatement("Personal Statement");
        assertEquals("Personal Statement", app.getPersonalStatement());

        app.setOtherScholarships("Other Scholarships");
        assertEquals("Other Scholarships", app.getOtherScholarships());

        app.setDecisionComments("Comments");
        assertEquals("Comments", app.getDecisionComments());
    }

    // --- Interview Model Tests ---

    @Test
    void testInterviewInitialization() {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        Interview interview = new Interview(1, 10, now, "Zoom Link", "Scheduled");

        assertEquals(1, interview.getInterviewID());
        assertEquals(10, interview.getEvalID());
        assertEquals(now, interview.getDateTime());
        assertEquals("Zoom Link", interview.getVenueOrLink());
        assertEquals("Scheduled", interview.getStatus());
    }

    @Test
    void testInterviewSetters() {
        Interview interview = new Interview(1, 10, null, "Old Link", "Scheduled");

        Timestamp future = new Timestamp(System.currentTimeMillis() + 3600000);
        interview.setDateTime(future);
        assertEquals(future, interview.getDateTime());

        interview.setVenueOrLink("New Link");
        assertEquals("New Link", interview.getVenueOrLink());

        interview.setStatus("Completed");
        assertEquals("Completed", interview.getStatus());
    }

    // --- JsonUtils Tests ---

    @Test
    void testExtractValue() {
        String json = "{\"name\": \"John\", \"age\": 30, \"isStudent\": false, \"hobbies\": [\"reading\", \"coding\"]}";

        assertEquals("John", JsonUtils.extractValue(json, "name"));
        assertEquals("30", JsonUtils.extractValue(json, "age"));
        assertEquals("false", JsonUtils.extractValue(json, "isStudent"));
        assertEquals("[\"reading\", \"coding\"]", JsonUtils.extractValue(json, "hobbies"));
        assertNull(JsonUtils.extractValue(json, "nonexistent"));
    }

    @Test
    void testExtractValueNested() {
        String json = "{\"user\": {\"id\": 1, \"username\": \"jdoe\"}, \"status\": \"active\"}";
        assertEquals("{\"id\": 1, \"username\": \"jdoe\"}", JsonUtils.extractValue(json, "user"));
        assertEquals("active", JsonUtils.extractValue(json, "status"));
    }

    @Test
    void testEscape() {
        assertEquals("Hello \\\"World\\\"", JsonUtils.escape("Hello \"World\""));
        assertEquals("Line1\\nLine2", JsonUtils.escape("Line1\nLine2"));
        assertEquals("\\\\path\\\\to\\\\file", JsonUtils.escape("\\path\\to\\file"));
    }

    @Test
    void testToJson() {
        assertEquals("\"Hello\"", JsonUtils.toJson("Hello"));
        assertEquals("123", JsonUtils.toJson(123));
        assertEquals("true", JsonUtils.toJson(true));

        List<String> list = Arrays.asList("a", "b", "c");
        assertEquals("[\"a\",\"b\",\"c\"]", JsonUtils.toJson(list));

        Map<String, Object> map = new HashMap<>();
        map.put("key", "value");
        assertEquals("{\"key\":\"value\"}", JsonUtils.toJson(map));

        assertNull(JsonUtils.extractValue(null, "key"));
    }

    // --- StatusValidator Tests ---

    @Test
    void testApplicationTransitions() {
        // Legal transitions
        assertTrue(StatusValidator.isValidTransition("Pending", "Reviewing", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewing", "Reviewed", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewed", "Shortlisted", "application"));
        assertTrue(StatusValidator.isValidTransition("Shortlisted", "Interviewed", "application"));
        assertTrue(StatusValidator.isValidTransition("Shortlisted", "Awarded", "application"));
        assertTrue(StatusValidator.isValidTransition("Interviewed", "Awarded", "application"));
        assertTrue(StatusValidator.isValidTransition("Interviewed", "Waitlisted", "application"));
        assertTrue(StatusValidator.isValidTransition("Waitlisted", "Awarded", "application"));

        // Legal withdrawals
        assertTrue(StatusValidator.isValidTransition("Pending", "Withdrawn", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewing", "Withdrawn", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewed", "Withdrawn", "application"));
        assertTrue(StatusValidator.isValidTransition("Shortlisted", "Withdrawn", "application"));
        assertTrue(StatusValidator.isValidTransition("Interviewed", "Withdrawn", "application"));
        assertTrue(StatusValidator.isValidTransition("Waitlisted", "Withdrawn", "application"));

        // Illegal transitions
        assertFalse(StatusValidator.isValidTransition("Pending", "Awarded", "application"));
        assertFalse(StatusValidator.isValidTransition("Pending", "Shortlisted", "application"));
        assertFalse(StatusValidator.isValidTransition("Awarded", "Withdrawn", "application"));
        assertFalse(StatusValidator.isValidTransition("Rejected", "Withdrawn", "application"));
        assertFalse(StatusValidator.isValidTransition("Withdrawn", "Pending", "application"));
    }

    @Test
    void testEvaluationTransitions() {
        assertTrue(StatusValidator.isValidTransition("Pending", "Completed", "evaluation"));
        assertTrue(StatusValidator.isValidTransition("Pending", "Approved", "evaluation"));
        assertTrue(StatusValidator.isValidTransition("Pending", "Rejected", "evaluation"));

        assertFalse(StatusValidator.isValidTransition("Completed", "Pending", "evaluation"));
    }

    @Test
    void testInterviewTransitions() {
        assertTrue(StatusValidator.isValidTransition("Scheduled", "Completed", "interview"));
        assertTrue(StatusValidator.isValidTransition("Scheduled", "Cancelled", "interview"));
        assertTrue(StatusValidator.isValidTransition("Scheduled", "Rescheduled", "interview"));
        assertTrue(StatusValidator.isValidTransition("Rescheduled", "Completed", "interview"));

        assertFalse(StatusValidator.isValidTransition("Completed", "Scheduled", "interview"));
        assertFalse(StatusValidator.isValidTransition("Cancelled", "Scheduled", "interview"));
    }

    @Test
    void testClarificationTransitions() {
        assertTrue(StatusValidator.isValidTransition("Pending", "Answered", "clarification"));
        assertFalse(StatusValidator.isValidTransition("Answered", "Pending", "clarification"));
    }

    @Test
    void testSameStatusTransition() {
        assertTrue(StatusValidator.isValidTransition("Pending", "Pending", "application"));
        assertTrue(StatusValidator.isValidTransition("Awarded", "Awarded", "application"));
        assertTrue(StatusValidator.isValidTransition("Scheduled", "Scheduled", "interview"));
    }

    @Test
    void testInvalidEntityType() {
        assertFalse(StatusValidator.isValidTransition("Pending", "Reviewing", "unknown"));
    }

    @Test
    void testNullInputs() {
        assertFalse(StatusValidator.isValidTransition(null, "Reviewing", "application"));
        assertFalse(StatusValidator.isValidTransition("Pending", null, "application"));
    }

    @Test
    void testScholarshipModel() {
        Scholarship s = new Scholarship(1, "Scholarship A", "Desc", "5000", "Degree", null, 3.5, 50000.0, true, true);
        assertEquals(1, s.getScholarshipID());
        assertEquals("Scholarship A", s.getTitle());
        assertEquals("5000", s.getAmount());
        assertEquals("5000", s.getAmount());
        assertEquals(3.5, s.getMinCGPA());
        assertTrue(s.requiresInterview());
        assertTrue(s.isActive());
    }

    @Test
    void testScholarshipRequiresInterview() {
        Scholarship s = new Scholarship(1, "S", "D", "100", "D", null, 3.0, 1000.0, false, true);
        assertFalse(s.requiresInterview());

        s.setRequiresInterview(true);
        assertTrue(s.requiresInterview());
    }

    @Test
    void testNotificationModel() {
        Notification n = new Notification(1, 10, "Test Message", null, false);
        assertEquals(1, n.getNotifID());
        assertEquals(10, n.getUserID());
        assertEquals("Test Message", n.getMessage());
        assertFalse(n.isRead());
    }

    @Test
    void testCriterionModel() {
        Criterion c = new Criterion(1, 101, "Academic", 40, 100.0, "cgpa");
        assertEquals(1, c.getCriteriaID());
        assertEquals(101, c.getScholarshipID());
        assertEquals("Academic", c.getName());
        assertEquals(100.0, c.getMaxScore());
    }

    @Test
    void testInquiryModel() {
        Inquiry i = new Inquiry(1, "S123", "Question?", null);
        assertEquals(1, i.getInquiryID());
        assertEquals("S123", i.getStudentID());
        assertEquals("Question?", i.getMessage());
        i.setStatus("Answered");
        assertEquals("Answered", i.getStatus());
    }

    @Test
    void testAuditLogModel() {
        AuditLog log = new AuditLog(10, "user@test.com", "UPDATE", "Application", "101", "Details", "127.0.0.1");
        assertEquals(10, log.getUserID());
        assertEquals("UPDATE", log.getAction());
        assertEquals("Application", log.getEntityType());
        assertEquals("101", log.getEntityID());
    }

    // --- UserDAO Integration Tests ---

    private static UserDAO userDAO;
    private static final String TEST_EMAIL = "integration_test_user@mmu.edu.my";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NAME = "Integration Test User";
    private static final String TEST_STUDENT_ID = "S123456789";

    @BeforeAll
    static void setUp() {
        // Initialize all DAOs
        userDAO = new UserDAO();
        inquiryDAO = new InquiryDAO();
        scholarshipDAO = new ScholarshipDAO();

        // Set up test data
        cleanUpTestData();
        createTestStudent();
        createInquiryTestStudent();
    }

    @AfterAll
    static void tearDown() {
        cleanUpTestData();
        cleanUpInquiryData();
    }

    private static void cleanUpTestData() {
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
    @Order(100)
    void testAuthenticateSuccess() {
        User user = userDAO.authenticate(TEST_EMAIL, TEST_PASSWORD);
        assertNotNull(user, "Auth failed for email: " + TEST_EMAIL);
        assertEquals(TEST_NAME, user.getFullName());
    }

    @Test
    @Order(101)
    void testAuthenticateByStudentID() {
        User user = userDAO.authenticate(TEST_STUDENT_ID, TEST_PASSWORD);
        assertNotNull(user, "Auth failed for ID: " + TEST_STUDENT_ID);
    }

    // --- InquiryDAO Integration Tests ---

    private static InquiryDAO inquiryDAO;
    private static final String INQUIRY_TEST_STUDENT_ID = "S987654321";
    private static int testInquiryID;

    private static void createInquiryTestStudent() {
        String sql = "INSERT INTO Student (fullName, email, password, role, isActive, studentID) " +
                "VALUES ('Inquiry Test Student', 'inquiry_test@mmu.edu.my', 'pass', 'Student', true, ?) " +
                "ON CONFLICT (studentID) DO NOTHING";
        try (Connection conn = DatabaseConnection.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, INQUIRY_TEST_STUDENT_ID);
            pstmt.executeUpdate();
        } catch (Exception e) {
        }
    }

    private static void cleanUpInquiryData() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql1 = "DELETE FROM Inquiry WHERE studentID = ?";
            try (PreparedStatement p1 = conn.prepareStatement(sql1)) {
                p1.setString(1, INQUIRY_TEST_STUDENT_ID);
                p1.executeUpdate();
            }
            String sql2 = "DELETE FROM Student WHERE studentID = ?";
            try (PreparedStatement p2 = conn.prepareStatement(sql2)) {
                p2.setString(1, INQUIRY_TEST_STUDENT_ID);
                p2.executeUpdate();
            }
        } catch (Exception e) {
        }
    }

    @Test
    @Order(200)
    void testCreateInquiry() {
        Inquiry i = new Inquiry(0, INQUIRY_TEST_STUDENT_ID, "This is a test question?", null);
        boolean created = inquiryDAO.create(i);
        assertTrue(created);

        List<Inquiry> studentInquiries = inquiryDAO.findByStudentId(INQUIRY_TEST_STUDENT_ID);
        assertFalse(studentInquiries.isEmpty());
        testInquiryID = studentInquiries.get(0).getInquiryID();
    }

    @Test
    @Order(201)
    void testInquiryFindById() {
        if (testInquiryID == 0)
            testCreateInquiry();
        Inquiry found = inquiryDAO.findById(testInquiryID);
        assertNotNull(found);
        assertEquals(INQUIRY_TEST_STUDENT_ID, found.getStudentID());
        assertEquals("Pending", found.getStatus());
    }

    @Test
    @Order(202)
    void testUpdateInquiryAnswer() {
        if (testInquiryID == 0)
            testCreateInquiry();
        String answer = "This is a test answer from admin.";
        boolean updated = inquiryDAO.updateAnswer(testInquiryID, answer);
        assertTrue(updated);

        Inquiry verified = inquiryDAO.findById(testInquiryID);
        assertEquals("Answered", verified.getStatus());
        assertEquals(answer, verified.getAnswer());
        assertNotNull(verified.getAnsweredAt());
    }

    @Test
    @Order(203)
    void testFindAllInquiriesWithDetails() {
        if (testInquiryID == 0)
            testCreateInquiry();
        List<Map<String, Object>> details = inquiryDAO.findAllWithDetails();
        assertFalse(details.isEmpty());
        assertTrue(details.stream().anyMatch(m -> (int) m.get("inquiryID") == testInquiryID));
    }

    // --- ScholarshipDAO Integration Tests ---

    private static ScholarshipDAO scholarshipDAO;
    private static int testScholarshipID;
    private static final String TEST_SCHOLARSHIP_TITLE = "Integration Test Scholarship";

    @Test
    @Order(300)
    void testCreateScholarship() {
        Scholarship s = new Scholarship(0, TEST_SCHOLARSHIP_TITLE, "Test Description", "1000", "Degree", null, 3.5,
                5000.0, true, true);

        List<Criterion> criteria = new ArrayList<>();
        criteria.add(new Criterion(0, 0, "Academic", 60, 4.0, "cgpa"));
        criteria.add(new Criterion(0, 0, "Financial", 40, 1000.0, "familyIncome"));
        s.setCriteria(criteria);

        testScholarshipID = scholarshipDAO.create(s);
        assertTrue(testScholarshipID > 0, "Scholarship should be created and return valid ID");
    }

    @Test
    @Order(301)
    void testScholarshipFindById() {
        if (testScholarshipID == 0)
            testCreateScholarship();
        Scholarship found = scholarshipDAO.findById(testScholarshipID);
        assertNotNull(found);
        assertEquals(TEST_SCHOLARSHIP_TITLE, found.getTitle());
        assertEquals(2, found.getCriteria().size(), "Should have 2 criteria");
    }

    @Test
    @Order(302)
    void testFindAllActiveScholarships() {
        if (testScholarshipID == 0)
            testCreateScholarship();
        List<Scholarship> active = scholarshipDAO.findAllActive();
        assertTrue(active.stream().anyMatch(s -> s.getScholarshipID() == testScholarshipID));
    }

    @Test
    @Order(303)
    void testUpdateScholarship() {
        if (testScholarshipID == 0)
            testCreateScholarship();
        Scholarship s = scholarshipDAO.findById(testScholarshipID);
        assertNotNull(s);

        String updatedTitle = "Updated Integration Scholarship";
        s.setTitle(updatedTitle);
        s.setActive(false);
        s.setRequiresInterview(false);

        boolean updated = scholarshipDAO.update(s);
        assertTrue(updated);

        Scholarship verified = scholarshipDAO.findById(testScholarshipID);
        assertEquals(updatedTitle, verified.getTitle());
        assertFalse(verified.isActive());
        assertFalse(verified.requiresInterview());
    }

    @Test
    @Order(304)
    void testDeleteScholarship() {
        if (testScholarshipID == 0)
            testCreateScholarship();
        boolean deleted = scholarshipDAO.delete(testScholarshipID);
        assertTrue(deleted);

        assertNull(scholarshipDAO.findById(testScholarshipID));
    }
}
