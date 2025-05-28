package com.netsim.standard.HTTP;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class HTTPRequestTest {
      @Test(expected = IllegalArgumentException.class)
      public void constructorThrowsOnNullPath() {
            new HTTPRequest(HTTPMethods.GET, null, "host", "data".getBytes());
      }

      @Test(expected = IllegalArgumentException.class)
      public void constructorThrowsOnNullHost() {
            new HTTPRequest(HTTPMethods.GET, "/path", null, "data".getBytes());
      }

      @Test(expected = IllegalArgumentException.class)
      public void constructorThrowsOnNullContent() {
            new HTTPRequest(HTTPMethods.GET, "/path", "host", null);
      }

      @Test
      public void getContentReturnsSameArray() {
            byte[] content = "hello".getBytes(StandardCharsets.US_ASCII);
            HTTPRequest req = new HTTPRequest(HTTPMethods.GET, "/foo", "example.com", content);
            assertSame("getContent should return the same byte[] reference",
                        content, req.getContent());
      }

      @Test
      public void getHeaderForGetRequest() {
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.GET,
                  "/index.html",
                  "www.example.com",
                  new byte[0]
            );
            String header = new String(req.getHeader(), StandardCharsets.US_ASCII);

            String expected = ""
                  + "GET /index.html HTTP/1.0\r\n"
                  + "Host: www.example.com\r\n"
                  + "\r\n";

            assertEquals("GET request header must match exactly", expected, header);
      }

      @Test
      public void getHeaderForPostRequestIncludesContentLength() {
            byte[] content = "abc123".getBytes(StandardCharsets.US_ASCII);
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.POST,
                  "/submit",
                  "api.test",
                  content
            );
            String header = new String(req.getHeader(), StandardCharsets.US_ASCII);

            String expectedStart = ""
                  + "POST /submit HTTP/1.0\r\n"
                  + "Host: api.test\r\n"
                  + "Content-Length: 6\r\n"
                  + "\r\n";

            assertEquals("POST header must include correct Content-Length",
                        expectedStart, header);
      }

      @Test
      public void toByteConcatenatesHeaderAndBody() {
            byte[] content = "payload".getBytes(StandardCharsets.US_ASCII);
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.POST,
                  "/data",
                  "svc.local",
                  content
            );
            byte[] raw = req.toByte();

            byte[] headerBytes = req.getHeader();
            assertEquals("raw[] should start with header[]",
                        new String(headerBytes, StandardCharsets.US_ASCII),
                        new String(raw, 0, headerBytes.length, StandardCharsets.US_ASCII));

            byte[] bodyBytes = new byte[content.length];
            System.arraycopy(raw, headerBytes.length, bodyBytes, 0, content.length);
            assertArrayEquals("raw[] should end with the content payload",
                              content, bodyBytes);

            assertEquals("total length must equal header + content length",
                        headerBytes.length + content.length,
                        raw.length);
      }
}
