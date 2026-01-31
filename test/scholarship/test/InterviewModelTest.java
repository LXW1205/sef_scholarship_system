package scholarship.test;

import org.junit.jupiter.api.Test;
import scholarship.model.Interview;
import java.sql.Timestamp;
import static org.junit.jupiter.api.Assertions.*;

public class InterviewModelTest {

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
}
