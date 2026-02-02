package tests;

import org.junit.jupiter.api.Test;
import model.*;
import utils.JsonUtils;
import utils.StatusValidator;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

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
        Scholarship s = new Scholarship(1, "Scholarship A", "Desc", "5000", "Degree", null, 3.5, 50000.0, true);
        assertEquals(1, s.getScholarshipID());
        assertEquals("Scholarship A", s.getTitle());
        assertEquals("5000", s.getAmount());
        assertEquals(3.5, s.getMinCGPA());
        assertTrue(s.isActive());
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
}
