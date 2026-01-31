package scholarship.test;

import org.junit.jupiter.api.Test;
import scholarship.utils.StatusValidator;
import static org.junit.jupiter.api.Assertions.*;

public class StatusValidatorTest {

    @Test
    void testApplicationTransitions() {
        // Legal transitions
        assertTrue(StatusValidator.isValidTransition("Pending", "Reviewing", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewing", "Reviewed", "application"));
        assertTrue(StatusValidator.isValidTransition("Reviewed", "Shortlisted", "application"));
        assertTrue(StatusValidator.isValidTransition("Shortlisted", "Interviewed", "application"));
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
}
