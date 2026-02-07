package tests;

import org.junit.Test;
import static org.junit.Assert.*;
import model.Application;
import model.Evaluation;
import model.Interview;
import model.Scholarship;
import model.Student;
import dao.ApplicationDAO;
import dao.EvaluationDAO;
import dao.ScholarshipDAO;
import dao.UserDAO;
import server.ApplicationHandler;
import utils.StatusValidator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class InterviewWorkflowTest {

    // Mock HttpExchange (Duplicated for isolation)
    private static class MockHttpExchange extends HttpExchange {
        private String method;
        private URI uri;
        private InputStream requestBody;
        private ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
        private Headers responseHeaders = new Headers();
        private int responseCode;

        public MockHttpExchange(String method, String uriString, String requestData) {
            this.method = method;
            this.uri = URI.create(uriString);
            this.requestBody = new ByteArrayInputStream(requestData.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }

        @Override
        public String getRequestMethod() {
            return method;
        }

        @Override
        public URI getRequestURI() {
            return uri;
        }

        @Override
        public InputStream getRequestBody() {
            return requestBody;
        }

        @Override
        public OutputStream getResponseBody() {
            return responseBody;
        }

        @Override
        public Headers getResponseHeaders() {
            return responseHeaders;
        }

        @Override
        public void sendResponseHeaders(int rCode, long responseLength) throws IOException {
            this.responseCode = rCode;
        }

        @Override
        public Headers getRequestHeaders() {
            return new Headers();
        }

        @Override
        public void close() {
        }

        @Override
        public java.net.InetSocketAddress getRemoteAddress() {
            return null;
        }

        @Override
        public int getResponseCode() {
            return responseCode;
        }

        @Override
        public java.net.InetSocketAddress getLocalAddress() {
            return null;
        }

        @Override
        public String getProtocol() {
            return "HTTP/1.1";
        }

        @Override
        public Object getAttribute(String name) {
            return null;
        }

        @Override
        public void setAttribute(String name, Object value) {
        }

        @Override
        public void setStreams(InputStream i, OutputStream o) {
        }

        public com.sun.net.httpserver.HttpContext getHttpContext() {
            return null;
        }

        public com.sun.net.httpserver.HttpPrincipal getPrincipal() {
            return null;
        }
    }

    @Test
    public void testInterviewAutoUpdate() throws IOException, java.sql.SQLException {
        ApplicationDAO appDAO = new ApplicationDAO();
        EvaluationDAO evalDAO = new EvaluationDAO();
        ScholarshipDAO schDAO = new ScholarshipDAO();
        UserDAO userDAO = new UserDAO();

        // 1. Setup Data
        // Assume Student 'S2024001' exists (from sample data) or create new one
        String studentId = "S_TEST_" + System.currentTimeMillis();
        // Since I can't easily register a student without modifying UserDAO, I will use
        // an existing student.
        String existingStudentId = "S2024001";
        int scholarshipId = 1;

        // Create Application manually
        Application app = new Application(0, existingStudentId, scholarshipId, "", null,
                StatusValidator.APP_PENDING_INTERVIEW);
        int appId = appDAO.save(app);

        // If app failed to save, it might be due to existing application for same
        // student/scholarship.
        // Try deleting existing ones for this student/scholarship first?
        // Or just use a different scholarship ID if possible?
        // For now, assume it saves or creates a duplicates (which is bad generally but
        // allowed by some DAOs).
        // Actually ApplicationDAO often checks for duplicates.
        // Let's assume it works for now or check appId.
        if (appId == 0) {
            // Maybe already exists?
            // Try to find it and update status
            List<Application> studentApps = appDAO.findByStudentID(existingStudentId);
            for (Application a : studentApps) {
                if (a.getScholarshipID() == scholarshipId) {
                    appId = a.getAppID();
                    break;
                }
            }
        }

        if (appId == 0)
            fail("Could not create or find application");

        // Update status forcefully to Pending Interview
        appDAO.updateStatus(appId, StatusValidator.APP_PENDING_INTERVIEW);

        // CREATE or FIND Evaluation
        Evaluation eval = evalDAO.findByAppId(appId);
        if (eval == null) {
            eval = new Evaluation(0, appId, "R001", "Test Comment", 0, "", "Pending", null);
            evalDAO.save(eval);
            eval = evalDAO.findByAppId(appId);
            assertNotNull("Evaluation should be saved", eval);
        }

        // Create Interview with PAST date
        Timestamp pastDate = Timestamp.valueOf(LocalDateTime.now().minusMinutes(10));
        insertInterview(eval.getEvalID(), pastDate);

        // 2. Trigger Auto-Update via Handler
        MockHttpExchange exchange = new MockHttpExchange("GET", "/api/applications", "");
        ApplicationHandler handler = new ApplicationHandler();
        handler.handle(exchange);

        // 3. Verify Status
        Application updatedApp = appDAO.findById(appId);
        assertEquals("Status should be updated to Interviewed", StatusValidator.APP_INTERVIEWED,
                updatedApp.getStatus());

        // Cleanup (Optional)
        // appDAO.delete(appId);
    }

    private void insertInterview(int evalId, Timestamp date) {
        String sql = "INSERT INTO Interview (evalID, dateTime, venueOrLink, status) VALUES (?, ?, ?, ?)";
        try (java.sql.Connection conn = db.DatabaseConnection.getConnection();
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, evalId);
            pstmt.setTimestamp(2, date);
            pstmt.setString(3, "Room 101");
            pstmt.setString(4, "Scheduled");
            pstmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
            // Fail if insert fails, but maybe it already exists?
        }
    }
}
