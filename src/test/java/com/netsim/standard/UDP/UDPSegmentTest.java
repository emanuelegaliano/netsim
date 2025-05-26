package com.netsim.standard.UDP;

import com.netsim.addresses.Port;
import org.junit.Test;
import static org.junit.Assert.*;

public class UDPSegmentTest {
      /**
       * Ensure the header is exactly 64 bits (8 bytes) long.
       */
      @Test
      public void testHeaderSizeIs64Bits() {
            Port src = new Port("1234");
            Port dst = new Port("5678");
            byte[] payload = new byte[] {0x01, 0x02, 0x03};
            UDPSegment seg = new UDPSegment(src, dst, 42, payload);

            // getHeader is protected; same package allows access
            byte[] header = seg.getHeader();
            int headerBits = header.length * Byte.SIZE;
            assertEquals("Header should be 64 bits", 64, headerBits);
      }

      /**
       * Ensure the serialized segment length matches header+payload length in bits.
       */
      @Test
      public void testSegmentLengthMatchesCalculated() {
            Port src = new Port("1000");
            Port dst = new Port("2000");
            byte[] payload = new byte[] {0x10, 0x20, 0x30, 0x40}; // 4 bytes = 32 bits
            UDPSegment seg = new UDPSegment(src, dst, 7, payload);

            // total bits = header bits + payload bits
            byte[] header = seg.getHeader();
            int expectedBits = header.length * Byte.SIZE + payload.length * Byte.SIZE;

            // getLength returns total length in bits
            assertEquals("getLength() should match header+payload bits",
                        expectedBits, seg.getLength());

            // toByte length in bytes * 8 equals getLength
            byte[] raw = seg.toByte();
            int actualBits = raw.length * Byte.SIZE;
            assertEquals(
                  "Serialized byte array bits should equal length field",
                  seg.getLength(), actualBits
            );
      }
}
