package com.netsim.standard.UDP;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.netsim.addresses.Port;
import com.netsim.standard.HTTP.HTTPMethods;
import com.netsim.standard.HTTP.HTTPRequest;

public class UDPTest {
      @Test(expected = IllegalArgumentException.class)
      public void testConstructorInvalidMSS() {
            new UDP(0, new Port("12345"), new Port("80"));
      }

      @Test(expected = IllegalArgumentException.class)
      public void testConstructorNullSource() {
            new UDP(100, null, new Port("80"));
      }

      @Test(expected = IllegalArgumentException.class)
      public void testConstructorNullDestination() {
            new UDP(100, new Port("12345"), null);
      }

      @Test
      public void testGetters() {
            Port src = new Port("12345");
            Port dst = new Port("80");
            UDP udp = new UDP(512, src, dst);
            assertSame(src, udp.getSourcePort());
            assertSame(dst, udp.getDestinationPort());
            assertEquals(512, udp.getSegmentSize());
      }

      @Test(expected = IllegalArgumentException.class)
      public void testEncapsulateNullRequest() {
            UDP udp = new UDP(256, new Port("1000"), new Port("2000"));
            udp.encapsulate(null);
      }

      @Test
      public void testEncapsulateSingleSegment() {
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.GET, "/path", "host", ""
            );  // empty body

            UDP udp = new UDP(1024, new Port("1000"), new Port("2000"));
            List<UDPSegment> segments = udp.encapsulate(req);

            // Only one segment should be produced
            assertEquals(1, segments.size());

            UDPSegment seg = segments.get(0);
            // Sequence number of first segment is 0
            assertEquals(0, seg.getSequenceNumber());

            // Payload length equals entire HTTP bytes
            int payloadBytes = seg.toByte().length - seg.getHeader().length;
            assertEquals(req.toByte().length, payloadBytes);
      }

      @Test
      public void testEncapsulateMultipleSegments() {
            // Make a request whose byte length exceeds MSS
            String body = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";  // 26 bytes
            HTTPRequest req = new HTTPRequest(
                  HTTPMethods.GET, "/resource", "host", body
            );

            byte[] allBytes = req.toByte();
            int mss = 10;
            UDP udp = new UDP(mss, new Port("1000"), new Port("2000"));

            List<UDPSegment> segments = udp.encapsulate(req);
            assertTrue("Should fragment into multiple segments", segments.size() > 1);

            // Verify sequence numbers and payload sizes
            int totalPayload = 0;
            for (int i = 0; i < segments.size(); i++) {
                  UDPSegment seg = segments.get(i);
                  assertEquals("Sequence number", i, seg.getSequenceNumber());

                  int headerLen  = seg.getHeader().length;
                  int segLen     = seg.toByte().length - headerLen;
                  assertTrue("Payload ≤ MSS", segLen <= mss);
                  totalPayload += segLen;
            }
            // Total payload across segments equals original length
            assertEquals(allBytes.length, totalPayload);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testDecapsulateNullList() {
            UDP udp = new UDP(256, new Port("1000"), new Port("2000"));
            udp.decapsulate(null);
      }

      @Test(expected = IllegalArgumentException.class)
      public void testDecapsulateEmptyList() {
            UDP udp = new UDP(256, new Port("1000"), new Port("2000"));
            udp.decapsulate(new ArrayList<>());
      }

      @Test
      public void testDecapsulateReassemblesExactly() {
            String body = "Hello, UDP fragmentation!";
            HTTPRequest original = new HTTPRequest(
                  HTTPMethods.POST, "/submit", "example.com", body
            );

            UDP udp = new UDP(8, new Port("1000"), new Port("2000"));

            // fragment and then reassemble
            List<UDPSegment> frags = udp.encapsulate(original);
            HTTPRequest reassembled = udp.decapsulate(frags);

            // The reassembled request must match exactly byte-for-byte
            assertArrayEquals(
                  "Full HTTP bytes should round-trip",
                  original.toByte(),
                  reassembled.toByte()
            );
      }
}
