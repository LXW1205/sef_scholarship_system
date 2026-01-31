package scholarship.test;

import org.junit.jupiter.api.Test;
import scholarship.model.Application;
import scholarship.model.Document;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class ApplicationModelTest {

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
}
