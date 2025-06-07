package com.netsim.standard.IPv4;

import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.Test;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.IdentityProtocol;

/**
 * Unit tests for IPv4Protocol, including encapsulation and decapsulation.
 */
public class IPv4ProtocolTest {
    private static final IPv4 SRC = new IPv4("192.168.0.1", 32);
    private static final IPv4 DST = new IPv4("10.0.0.1",   32);

    /**
     * Extracts the 4‐bit IHL from byte 0 of the IPv4 header.
     */
    private int extractIHL(byte[] packet) {
        return packet[0] & 0x0F;
    }

    /**
     * Extracts the 16‐bit Total Length field (bytes 2–3).
     */
    private int extractTotalLength(byte[] packet) {
        int hi = packet[2] & 0xFF;
        int lo = packet[3] & 0xFF;
        return (hi << 8) | lo;
    }

    /**
     * Given a single IPv4 packet, returns the payload (everything after IHL*4).
     */
    private byte[] extractPayload(byte[] packet) {
        int ihl = extractIHL(packet);
        int headerLen = ihl * 4;
        return Arrays.copyOfRange(packet, headerLen, packet.length);
    }

    // ---------- Exception / edge‐case tests for encapsulate ----------

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsNullPayload() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setNext(new IdentityProtocol());
        ip.encapsulate((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateRejectsEmptyPayload() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setNext(new IdentityProtocol());
        ip.encapsulate(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void encapsulateRequiresNextProtocol() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.encapsulate(new byte[10]); // nextProtocol not set → RuntimeException
    }

    @Test(expected = RuntimeException.class)
    public void encapsulateRejectsTooSmallMTU() {
        // IHL=5 → headerLen=20, MTU=20 ⇒ no space for payload
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 20);
        ip.setNext(new IdentityProtocol());
        ip.encapsulate(new byte[]{1,2,3});
    }

    // ---------- No‐fragment tests ----------

    @Test
    public void noFragmentSinglePacket() {
        int IHL = 5;
        int typeOfService = 0;
        int identification = 0x1234;
        int flags = 0;
        int ttl = 64;
        int protocol = 17; // UDP
        int MTU = 200;     // headerLen=20 → maxData=180

        IPv4Protocol ip = new IPv4Protocol(
            SRC, DST,
            IHL,
            typeOfService,
            identification,
            flags,
            ttl,
            protocol,
            MTU
        );
        ip.setNext(new IdentityProtocol());

        // Payload of length 50 bytes
        byte[] payload = new byte[50];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) i;
        }

        byte[] result = ip.encapsulate(payload);

        // Should be exactly one IPv4 packet of length 20 + 50 = 70 bytes
        assertEquals(20 + 50, result.length);

        // IHL in first byte
        assertEquals(IHL, extractIHL(result));

        // Total Length field = 70
        assertEquals(result.length, extractTotalLength(result));

        // Payload preserved exactly
        assertArrayEquals(payload, extractPayload(result));
    }

    @Test
    public void boundaryNoFragment() {
        int IHL = 5;              // headerLen=20
        int typeOfService = 0;
        int identification = 0xABCD;
        int flags = 0;
        int ttl = 128;
        int protocol = 6;         // TCP
        int MTU = 100;            // headerLen=20 → maxData=80

        IPv4Protocol ip = new IPv4Protocol(
            SRC, DST,
            IHL,
            typeOfService,
            identification,
            flags,
            ttl,
            protocol,
            MTU
        );
        ip.setNext(new IdentityProtocol());

        // Payload exactly 80 bytes
        byte[] payload = new byte[80];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (0xFF - i);
        }

        byte[] result = ip.encapsulate(payload);

        // Exactly one packet of length 100
        assertEquals(MTU, result.length);
        assertEquals(IHL, extractIHL(result));
        assertEquals(MTU, extractTotalLength(result));
        assertArrayEquals(payload, extractPayload(result));
    }

    // ---------- Fragmentation tests ----------

    @Test
    public void fragmentationIntoThreeFragments() {
        int IHL = 5;              // headerLen = 20
        int typeOfService = 0;
        int identification = 0x1001;
        int flags = 0;            // DF=0 → fragmentation allowed
        int ttl = 64;
        int protocol = 17;        // UDP
        int MTU = 30;             // headerLen=20 → maxData=10, but use multiples of 8

        IPv4Protocol ip = new IPv4Protocol(
            SRC, DST,
            IHL,
            typeOfService,
            identification,
            flags,
            ttl,
            protocol,
            MTU
        );
        ip.setNext(new IdentityProtocol());

        // Payload of length 25 → fragments of 8, 8, 9 bytes
        byte[] payload = new byte[25];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i + 1);
        }

        byte[] all = ip.encapsulate(payload);
        ByteBuffer buf = ByteBuffer.wrap(all);

        // --- First fragment: length = 20 + 8 = 28 bytes
        byte[] frag1 = new byte[28];
        buf.get(frag1);
        assertEquals(28, extractTotalLength(frag1));
        int ff1 = ((frag1[6] & 0xFF) << 8) | (frag1[7] & 0xFF);
        int flags1 = (ff1 >>> 13) & 0x7;
        assertEquals(2, flags1);  // flagsValue = 2 for MF=1 under (mf<<1)
        int off1 = ff1 & 0x1FFF;
        assertEquals(0, off1);
        byte[] p1 = extractPayload(frag1);
        assertArrayEquals(Arrays.copyOfRange(payload, 0, 8), p1);

        // --- Second fragment: length = 20 + 8 = 28 bytes
        byte[] frag2 = new byte[28];
        buf.get(frag2);
        assertEquals(28, extractTotalLength(frag2));
        int ff2 = ((frag2[6] & 0xFF) << 8) | (frag2[7] & 0xFF);
        int flags2 = (ff2 >>> 13) & 0x7;
        assertEquals(2, flags2);  // still MF=1 → flagsValue = 2
        int off2 = ff2 & 0x1FFF;
        assertEquals(1, off2);     // offset = 8/8 = 1
        byte[] p2 = extractPayload(frag2);
        assertArrayEquals(Arrays.copyOfRange(payload, 8, 16), p2);

        // --- Third fragment: length = 20 + 9 = 29 bytes
        byte[] frag3 = new byte[29];
        buf.get(frag3);
        assertEquals(29, extractTotalLength(frag3));
        int ff3 = ((frag3[6] & 0xFF) << 8) | (frag3[7] & 0xFF);
        int flags3 = (ff3 >>> 13) & 0x7;
        assertEquals(0, flags3);  // MF=0 → flagsValue = 0
        int off3 = ff3 & 0x1FFF;
        assertEquals(2, off3);     // offset = 16/8 = 2
        byte[] p3 = extractPayload(frag3);
        assertArrayEquals(Arrays.copyOfRange(payload, 16, 25), p3);

        // No extra bytes remaining
        assertFalse(buf.hasRemaining());
    }

    // ---------- Tests for decapsulate ----------

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsNullInput() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setPrevious(new IdentityProtocol());
        ip.decapsulate((byte[]) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateRejectsEmptyInput() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setPrevious(new IdentityProtocol());
        ip.decapsulate(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void decapsulateRequiresPreviousProtocol() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        // previousProtocol not set → RuntimeException
        byte[] dummy = new byte[20 + 1];
        ip.decapsulate(dummy);
    }

    @Test
    public void decapsulateReassemblesSinglePacket() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setNext(new IdentityProtocol());
        ip.setPrevious(new IdentityProtocol());

        byte[] payload = new byte[60];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (i * 2);
        }

        byte[] wire = ip.encapsulate(payload);
        byte[] reassembled = ip.decapsulate(wire);

        assertArrayEquals("Single-packet payload must match after decapsulation",
                        payload, reassembled);
    }

    @Test
    public void decapsulateReassemblesMultipleFragments() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 30);
        ip.setNext(new IdentityProtocol());
        ip.setPrevious(new IdentityProtocol());

        byte[] payload = new byte[25];
        for (int i = 0; i < payload.length; i++) {
            payload[i] = (byte) (100 + i);
        }

        byte[] allFragments = ip.encapsulate(payload);
        byte[] reassembled = ip.decapsulate(allFragments);

        assertArrayEquals("Reassembled payload must match original",
                        payload, reassembled);
    }

    // ---------- Tests for extractSource / extractDestination ----------

    @Test(expected = IllegalArgumentException.class)
    public void extractSourceRejectsNull() {
        new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100)
            .extractSource(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void extractDestinationRejectsTooShort() {
        new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100)
            .extractDestination(new byte[19]);
    }

    @Test
    public void extractSourceAndDestinationReturnCorrectAddresses() {
        IPv4Protocol ip = new IPv4Protocol(SRC, DST, 5, 0, 0, 0, 64, 17, 100);
        ip.setNext(new IdentityProtocol());
        ip.setPrevious(new IdentityProtocol());

        byte[] payload = new byte[]{0x01,0x02,0x03,0x04};
        byte[] packet = ip.encapsulate(payload);

        IPv4 extractedSrc = ip.extractSource(packet);
        IPv4 extractedDst = ip.extractDestination(packet);

        assertEquals("Source must match", SRC.stringRepresentation(), extractedSrc.stringRepresentation());
        assertEquals("Destination must match", DST.stringRepresentation(), extractedDst.stringRepresentation());
    }
}
