package com.netsim.standard.HTTP;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

/**
 * Unit tests for HTTPRequest
 */
public class HTTPRequestTest {

    /**
     * Test that a GET request generates the correct headers only,
     * without a Content-Length header and no body.
     */
    @Test
    public void testGetRequestHeaderOnly() {
        HTTPRequest req = new HTTPRequest(
            HTTPMethods.GET,
            "/index.html",
            "www.example.com",
            ""  // GET requests have no body
        );

        String raw = new String(req.toByte(), StandardCharsets.US_ASCII);

        // Should start with the correct request-line
        assertTrue("Request-line should start with GET method",
                   raw.startsWith("GET /index.html HTTP/1.0\r\n"));

        // Should contain the Host header
        assertTrue("Header should contain Host field",
                   raw.contains("Host: www.example.com\r\n"));

        // Should not contain Content-Length for GET
        assertFalse("GET requests must not include Content-Length",
                    raw.contains("Content-Length:"));

        // Should end with CRLF CRLF and no body
        assertTrue("Header should end with CRLFCRLF",
                   raw.endsWith("\r\n\r\n"));

        // Verify total length matches expected header length
        String expectedHeader = "GET /index.html HTTP/1.0\r\n" +
                                "Host: www.example.com\r\n" +
                                "\r\n";
        assertEquals("Raw header length should match expected header length",
                     expectedHeader.length(),
                     raw.length());
    }

    /**
     * Test that a POST request includes Content-Length and body correctly.
     */
    @Test
    public void testPostRequestWithBody() {
        String body = "field1=value1&f2=v2";
        HTTPRequest req = new HTTPRequest(
            HTTPMethods.POST,
            "/submit",
            "api.example.com",
            body
        );

        String raw = new String(req.toByte(), StandardCharsets.US_ASCII);

        // Should start with POST request-line
        assertTrue("Request-line should start with POST method",
                   raw.startsWith("POST /submit HTTP/1.0\r\n"));

        // Should contain the Host header
        assertTrue("Header should contain Host field",
                   raw.contains("Host: api.example.com\r\n"));

        // Content-Length must match body length
        int expectedLen = body.getBytes(StandardCharsets.US_ASCII).length;
        assertTrue("Header should contain correct Content-Length",
                   raw.contains("Content-Length: " + expectedLen + "\r\n"));

        // Ensure CRLFCRLF separates header and body
        int splitIndex = raw.indexOf("\r\n\r\n");
        assertTrue("Header and body must be separated by CRLFCRLF",
                   splitIndex > 0);

        String headerPart = raw.substring(0, splitIndex + 4);
        String bodyPart   = raw.substring(splitIndex + 4);

        // Body must be accurately appended
        assertEquals("Body content should match the input content",
                     body, bodyPart);

        // Combining header and body should reconstruct raw
        assertEquals("Reconstructed message should match raw",
                     headerPart + bodyPart,
                     raw);
    }

    /**
     * Test custom path and host fields in the request.
     */
    @Test
    public void testCustomPathAndHost() {
        String path = "/api/data";
        String host = "localhost:8080";
        HTTPRequest req = new HTTPRequest(
            HTTPMethods.GET,
            path,
            host,
            ""  // GET requests have no body
        );
        String raw = new String(req.toByte(), StandardCharsets.US_ASCII);

        // Verify request-line uses the custom path
        assertTrue("Request-line should include the custom path",
                   raw.startsWith("GET " + path + " HTTP/1.0\r\n"));

        // Verify Host header uses the custom host
        assertTrue("Header should include the custom host",
                   raw.contains("Host: " + host + "\r\n"));
    }
}
