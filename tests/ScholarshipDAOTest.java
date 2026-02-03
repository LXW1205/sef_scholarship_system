package tests;

import dao.ScholarshipDAO;
import model.Scholarship;
import model.Criterion;
import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ScholarshipDAOTest {
    private static ScholarshipDAO scholarshipDAO;
    private static int testScholarshipID;
    private static final String TEST_TITLE = "Integration Test Scholarship";

    @BeforeAll
    static void setUp() {
        scholarshipDAO = new ScholarshipDAO();
    }

    @Test
    @Order(1)
    void testCreateScholarship() {
        Scholarship s = new Scholarship(0, TEST_TITLE, "Test Description", "1000", "Degree", null, 3.5, 5000.0, true);

        List<Criterion> criteria = new ArrayList<>();
        criteria.add(new Criterion(0, 0, "Academic", 60, 4.0, "cgpa"));
        criteria.add(new Criterion(0, 0, "Financial", 40, 1000.0, "familyIncome"));
        s.setCriteria(criteria);

        testScholarshipID = scholarshipDAO.create(s);
        assertTrue(testScholarshipID > 0, "Scholarship should be created and return valid ID");
    }

    @Test
    @Order(2)
    void testFindById() {
        Scholarship found = scholarshipDAO.findById(testScholarshipID);
        assertNotNull(found);
        assertEquals(TEST_TITLE, found.getTitle());
        assertEquals(2, found.getCriteria().size(), "Should have 2 criteria");
    }

    @Test
    @Order(3)
    void testFindAllActive() {
        List<Scholarship> active = scholarshipDAO.findAllActive();
        assertTrue(active.stream().anyMatch(s -> s.getScholarshipID() == testScholarshipID));
    }

    @Test
    @Order(4)
    void testUpdateScholarship() {
        Scholarship s = scholarshipDAO.findById(testScholarshipID);
        assertNotNull(s);

        String updatedTitle = "Updated Integration Scholarship";
        s.setTitle(updatedTitle);
        s.setActive(false);

        boolean updated = scholarshipDAO.update(s);
        assertTrue(updated);

        Scholarship verified = scholarshipDAO.findById(testScholarshipID);
        assertEquals(updatedTitle, verified.getTitle());
        assertFalse(verified.isActive());
    }

    @Test
    @Order(5)
    void testDeleteScholarship() {
        boolean deleted = scholarshipDAO.delete(testScholarshipID);
        assertTrue(deleted);

        assertNull(scholarshipDAO.findById(testScholarshipID));
    }
}
