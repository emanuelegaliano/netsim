package com.netsim.standard.HTTP;

import static org.junit.Assert.*;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.netsim.networkstack.Protocol;
import com.netsim.addresses.Address;

public class HTTPTest {
      private HTTP http;
      private final byte[] sampleContent = "Hello".getBytes(StandardCharsets.US_ASCII);

      @Before
      public void setUp() {
            http = new HTTP(HTTPMethods.POST, "/path", "host");
      }

      // --- encapsulate tests ---

      @Test(expected = NullPointerException.class)
      public void encapsulateThrowsOnNull() {
            http.encapsulate(null);
      }

      @Test(expected = IllegalArgumentException.class)
      public void encapsulateThrowsOnEmpty() {
            http.encapsulate(new byte[0]);
      }

      @Test(expected = RuntimeException.class)
      public void encapsulateThrowsWhenNextNotDefined() {
            http.encapsulate(sampleContent);
      }

      @Test
      public void encapsulateBuildsHTTPRequestAndPassesToNext() {
            // stub next protocol
            Protocol stubNext = new Protocol() {
                  public byte[] encapsulate(byte[] pdu) {
                        return pdu;  // echo back
                  }
                  public byte[] decapsulate(byte[] pdu) {
                        return pdu;
                  }
                  public void setNext(Protocol next) {}
                  public void setPrevious(Protocol previous) {}

                  public Address extractSource(byte[] pdu) {
                        return null;
                  }
                  public Address extractDestination(byte[] pdu) {
                        return null;
                  }
            };
            // use setter to define nextProtocolDefined
            http.setNext(stubNext);

            byte[] out = http.encapsulate(sampleContent);
            String outText = new String(out, StandardCharsets.US_ASCII);

            String expectedHeader = ""
                  + "POST /path HTTP/1.0\r\n"
                  + "Host: host\r\n"
                  + "Content-Length: " + sampleContent.length + "\r\n"
                  + "\r\n";

            assertTrue(
                  "Output should start with the generated HTTP header",
                  outText.startsWith(expectedHeader)
            );

            byte[] bodyPart = Arrays.copyOfRange(
                  out,
                  expectedHeader.getBytes(StandardCharsets.US_ASCII).length,
                  out.length
            );
            assertArrayEquals(
                  "Output should end with the original payload",
                  sampleContent,
                  bodyPart
            );
      }

      // --- decapsulate tests ---

      @Test(expected = RuntimeException.class)
      public void decapsulateThrowsOnEmpty() {
            http.decapsulate(new byte[0]);
      }

      @Test(expected = RuntimeException.class)
      public void decapsulateThrowsWhenPreviousNotDefined() {
            // build a minimal HTTP request bytes
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.GET, "/foo", "host", sampleContent);
            http.decapsulate(req.toByte());
      }
      
      @Test
      public void decapsulateExtractsBodyAndPassesToPrevious() throws Exception {
      // Prepare a full HTTPRequest byte array
      byte[] content = "abcde".getBytes(StandardCharsets.US_ASCII);
      HTTPRequest req = new HTTPRequest(
            HTTPMethods.POST, "/test", "h", content);
      byte[] full = req.toByte();

      // Compute where the body starts
      String fullText = new String(full, StandardCharsets.US_ASCII);
      int sep = fullText.indexOf("\r\n\r\n");
      assertTrue("Header/body separator must exist", sep >= 0);
      byte[] expectedBody = Arrays.copyOfRange(full, sep + 4, full.length);

      // Stub previous protocol to echo back whatever it gets
      Protocol stubPrev = new Protocol() {
            public byte[] encapsulate(byte[] pdu) { return pdu; }
            public byte[] decapsulate(byte[] pdu) { return pdu; }
            public void setNext(Protocol next) {}
            public void setPrevious(Protocol prev) {}
            public Address extractSource(byte[] pdu) {
                  return null;
            }
            public Address extractDestination(byte[] pdu) {
                  return null;
            }
      };
      http.setPrevious(stubPrev);

      // Now when we decapsulate, we should get *only* the body
      byte[] out = http.decapsulate(full);
      assertArrayEquals(
            "decapsulate should extract only the body and pass that on",
            expectedBody,
            out
      );
      }

}
