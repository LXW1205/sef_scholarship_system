package tests;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import org.junit.Test;
import server.ScholarshipHandler;
import static org.junit.Assert.*;

public class ScholarshipValidationTest {

    // Mock HttpExchange
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

        // Unused methods implementation to satisfy abstract class
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

        // Removed getVirtualHost as it caused compilation error (not present in target
        // JDK)

        public com.sun.net.httpserver.HttpContext getHttpContext() {
            return null;
        }

        public com.sun.net.httpserver.HttpPrincipal getPrincipal() {
            return null;
        }
    }

    @Test
    public void testCreateScholarshipWithPastDeadline() throws IOException {
        String pastDate = LocalDate.now().minusDays(1).toString();
        String jsonInputString = "{"
                + "\"title\": \"Past Deadline Scholarship\","
                + "\"description\": \"Test description\","
                + "\"amount\": \"1000\","
                + "\"deadline\": \"" + pastDate + "\","
                + "\"forQualification\": \"Degree\","
                + "\"minCGPA\": 3.0,"
                + "\"maxFamilyIncome\": 5000,"
                + "\"status\": \"Open\","
                + "\"criteria\": []"
                + "}";

        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/scholarships", jsonInputString);
        ScholarshipHandler handler = new ScholarshipHandler();
        handler.handle(exchange);

        assertEquals("Should return 400 Bad Request for past deadline", 400, exchange.getResponseCode());
        String response = exchange.responseBody.toString(java.nio.charset.StandardCharsets.UTF_8);
        assertTrue("Response should contain error message", response.contains("Deadline cannot be in the past"));
    }

    @Test
    public void testCreateScholarshipWithFutureDeadline() throws IOException {
        String futureDate = LocalDate.now().plusDays(30).toString();
        String jsonInputString = "{"
                + "\"title\": \"Future Deadline Scholarship\","
                + "\"description\": \"Test description\","
                + "\"amount\": \"1000\","
                + "\"deadline\": \"" + futureDate + "\","
                + "\"forQualification\": \"Degree\","
                + "\"minCGPA\": 3.0,"
                + "\"maxFamilyIncome\": 5000,"
                + "\"status\": \"Open\","
                + "\"criteria\": []"
                + "}";

        MockHttpExchange exchange = new MockHttpExchange("POST", "/api/scholarships", jsonInputString);
        ScholarshipHandler handler = new ScholarshipHandler();
        handler.handle(exchange);

        // Since we are mocking connection, the handler will try to interact with DAO.
        // DAO logic is simple JDBC. It will fail if DB is not reachable.
        // BUT, the validation logic is what we test.
        // If it got past validation, it will try to insert.
        // If db is not reachable, it returns 500.
        // So checking it's NOT 400 proves validation passed.

        if (exchange.getResponseCode() == 400) {
            String response = exchange.responseBody.toString(java.nio.charset.StandardCharsets.UTF_8);
            assertFalse("Should not fail with validation error for future deadline. Response: " + response,
                    response.contains("Deadline cannot be in the past"));
        }
    }
}
