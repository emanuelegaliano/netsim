package com.netsim.standard.IPv4;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.addresses.Port;
import com.netsim.standard.UDP.UDPSegment;

public class IPv4PacketTest {
        private static final IPv4 SRC = new IPv4("192.168.0.1", 24);
        private static final IPv4 DST = new IPv4("10.0.0.1", 24);
        private static final Port SPORT = new Port("1234");
        private static final Port DPORT = new Port("4321");

        private UDPSegment makeSegment(int size) {
            byte[] payload = new byte[size];
            for (int i = 0; i < size; i++) payload[i] = (byte) i;
            return new UDPSegment(SPORT, DPORT, 0, payload);
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsNullSource() {
            new IPv4Packet(null, DST, 4, 5, 0, 21, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsNullDestination() {
            new IPv4Packet(SRC, null, 4, 5, 0, 21, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTosTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, -1, 21, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTosTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 256, 21, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTotalLengthTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, -1, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTotalLengthTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 0x1_0000, 0, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsIdentificationTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, -1, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsIdentificationTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0x1_0000, 0, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsFlagsTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, -1, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsFlagsTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 8, 0, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsFragmentOffsetTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, -1, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsFragmentOffsetTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0x2000, 0, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTtlTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0, -1, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsTtlTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0, 0x1_0000, 0, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsProtocolTooLow() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0, 0, -1, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsProtocolTooHigh() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0, 0, 0x1_0000, makeSegment(1).toByte());
        }

        @Test(expected = IllegalArgumentException.class)
        public void constructorRejectsNullPayload() {
            new IPv4Packet(SRC, DST, 4, 5, 0, 21, 0, 0, 0, 0, 0, null);
        }

        // --- Header field tests ---

        @Test
        public void testVersionAndIhlNibble() {
            int version = 4, ihl = 7;
            UDPSegment seg = makeSegment(1);
            IPv4Packet pkt = new IPv4Packet(SRC, DST,
                                            version, ihl,
                                            0,  // TOS
                                            ihl*4 + seg.toByte().length,
                                            0,0,0,0,0,
                                            seg.toByte());
            byte[] header = pkt.getHeader();
            int b0 = header[0] & 0xFF;
            assertEquals("Version high 4 bits", version,  (b0 >>> 4) & 0xF);
            assertEquals("IHL low 4 bits",       ihl,      b0 & 0xF);
        }

        @Test
        public void testFlagsAndFragmentOffsetEncoding() {
            int flags = 3, fragOff = 0x0ABC;
            UDPSegment seg = makeSegment(2);
            IPv4Packet pkt = new IPv4Packet(SRC, DST,
                                            4, 5,
                                            0,
                                            5*4 + seg.toByte().length,
                                            0, flags, fragOff,
                                            0,0,
                                            seg.toByte());
            byte[] header = pkt.getHeader();
            int ff = ((header[6] & 0xFF) << 8) | (header[7] & 0xFF);
            assertEquals("Flags extracted",          flags,   (ff >>> 13) & 0x7);
            assertEquals("FragmentOffset extracted", fragOff, ff & 0x1FFF);
        }

        @Test
        public void testAddressesInHeader() {
            UDPSegment seg = makeSegment(3);
            IPv4Packet pkt = new IPv4Packet(SRC, DST,
                                            4, 5, 0,
                                            5*4 + seg.toByte().length,
                                            0,0,0,64,17,
                                            seg.toByte());
            byte[] header = pkt.getHeader();
            byte[] srcBytes = SRC.byteRepresentation();
            byte[] dstBytes = DST.byteRepresentation();
            assertArrayEquals("Source IP in header",
                            srcBytes,
                            Arrays.copyOfRange(header, 12, 16));
            assertArrayEquals("Destination IP in header",
                            dstBytes,
                            Arrays.copyOfRange(header, 16, 20));
        }

        @Test
        public void testPaddingWhenIhlGreaterThan5() {
            int ihl = 6;
            UDPSegment seg = makeSegment(1);
            int headerLen = ihl * 4;
            IPv4Packet pkt = new IPv4Packet(SRC, DST,
                                            4, ihl, 0,
                                            headerLen + seg.toByte().length,
                                            0,0,0,64,17,
                                            seg.toByte());
            byte[] header = pkt.getHeader();
            for (int i = 20; i < headerLen; i++) {
                assertEquals("Padding byte at " + i, 0, header[i]);
            }
        }

        // --- Payload placement test ---

        @Test
        public void testPayloadIsAppendedAfterHeader() {
            int ihl = 5;
            UDPSegment seg = makeSegment(4);
            byte[] segBytes = seg.toByte();
            IPv4Packet pkt = new IPv4Packet(SRC, DST,
                                            4, ihl, 0,
                                            ihl*4 + segBytes.length,
                                            0,0,0,64,17,
                                            seg.toByte());
            byte[] datagram = pkt.toByte();
            int headerLen = ihl * 4;
            byte[] afterHeader = Arrays.copyOfRange(datagram, headerLen, datagram.length);
            assertArrayEquals("Payload equals UDPSegment.toByte()",
                            segBytes,
                            afterHeader);
        }
}
