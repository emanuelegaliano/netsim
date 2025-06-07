package com.netsim.networkstack;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.netsim.addresses.Mac;

public class NetworkAdapterTest {
    private static final String NAME = "eth0";
    private static final int MTU = 1500;
    private static final Mac MAC = new Mac("aa:bb:cc:dd:ee:ff");

    private NetworkAdapter adapter;
    private NetworkAdapter other;

    @Before
    public void setUp() {
        adapter = new NetworkAdapter(NAME, MTU, MAC);
        // give the peer the same MAC so receives will pass
        other   = new NetworkAdapter("eth1", MTU, MAC);
    }

    // -- constructor / getters --

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullName() {
        new NetworkAdapter(null, MTU, MAC);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorRejectsNullMac() {
        new NetworkAdapter(NAME, MTU, null);
    }

    @Test
    public void gettersReturnCorrectValues() {
        assertEquals(NAME, adapter.getName());
        assertEquals(MTU,  adapter.getMTU());
        assertSame(MAC,    adapter.getMacAddress());
        // default flags
        assertTrue(adapter.isUp());
        assertFalse(adapter.promiscuousMode());
    }

    // -- up/down --

    @Test
    public void setDownAndUpFlipIsUp() {
        adapter.setDown();
        assertFalse(adapter.isUp());
        adapter.setUp();
        assertTrue(adapter.isUp());
    }

    // -- promiscuous mode --

    @Test
    public void setPromiscuousOnOff() {
        adapter.setPromiscuosModeOn();
        assertTrue(adapter.promiscuousMode());
        adapter.setPromiscuosModeOff();
        assertFalse(adapter.promiscuousMode());
    }

    // -- receiveFrame / releaseFrames --

    @Test(expected = IllegalArgumentException.class)
    public void receiveFrameRejectsNull() {
        adapter.receiveFrame(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void receiveFrameRejectsEmpty() {
        adapter.receiveFrame(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void receiveFrameWhenDownThrows() {
        adapter.setDown();
        adapter.receiveFrame(new byte[]{1,2,3});
    }

    @Test(expected = RuntimeException.class)
    public void releaseFramesEmptyThrows() {
        adapter.releaseFrames();
    }

    @Test
    public void receiveAndReleaseFrames() {
        byte[] f1 = {0x01,0x02};
        byte[] f2 = {0x03,0x04,0x05};
        adapter.receiveFrame(f1);
        adapter.receiveFrame(f2);

        byte[] all = adapter.releaseFrames();
        // should be concatenation f1||f2
        byte[] expect = new byte[f1.length + f2.length];
        System.arraycopy(f1,0,expect,0,f1.length);
        System.arraycopy(f2,0,expect,f1.length,f2.length);
        assertArrayEquals(expect, all);

        // buffer now cleared
        try {
            adapter.releaseFrames();
            fail("releaseFrames should throw once emptied");
        } catch(RuntimeException e) { /* ok */ }
    }

    // -- collectFrames / sendFrames integration --

    private byte[] makeFrame(Mac dst, Mac src, int totalLen) {
        // build a single DLL frame wrapping an IPv4 packet of totalLen bytes
        if (totalLen < 20) throw new IllegalArgumentException();
        byte[] frame = new byte[12 + totalLen];
        System.arraycopy(dst.byteRepresentation(), 0, frame,   0, 6);
        System.arraycopy(src.byteRepresentation(), 0, frame,   6, 6);
        // IPv4 header: version(4)<<4 | IHL=5  in byte 12
        frame[12] = (byte)( (4<<4) | 5 );
        // total length field at bytes 14,15 (offsets 12+2,12+3)
        int hi = (totalLen >> 8) & 0xFF, lo = totalLen & 0xFF;
        frame[12+2] = (byte) hi;
        frame[12+3] = (byte) lo;
        // rest of IPv4 header bytes 16..31 remain zeros
        return frame;
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsNull() {
        adapter.collectFrames(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsEmpty() {
        adapter.collectFrames(new byte[0]);
    }

    @Test(expected = RuntimeException.class)
    public void collectFramesWhenDownThrows() {
        adapter.setDown();
        byte[] frame = makeFrame(MAC, MAC, 20);
        adapter.collectFrames(frame);
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsTruncatedFrame() {
        // fewer than 12+20 bytes
        byte[] bad = new byte[10 + 20];
        adapter.collectFrames(bad);
    }

    @Test(expected = IllegalArgumentException.class)
    public void collectFramesRejectsSrcMismatch() {
        Mac otherMac = new Mac("11:22:33:44:55:66");
        byte[] frame = makeFrame(MAC, otherMac, 20);
        adapter.collectFrames(frame);
    }

    @Test
    public void collectThenSendAndReleaseRoundTrip() {
        // create two valid back-to-back frames
        byte[] f1 = makeFrame(MAC, MAC, 20);
        byte[] f2 = makeFrame(MAC, MAC, 24);
        byte[] both = concat(f1, f2);

        // adapter.collectFrames splits into two outgoing frames,
        // then adapter.sendFrames delivers to other.inGoingFrames,
        // then other.releaseFrames returns the two frames back-to-back.
        adapter.collectFrames(both);
        // send to 'other'
        adapter.sendFrames(other);
        byte[] delivered = other.releaseFrames();
        assertArrayEquals("should deliver same two frames", both, delivered);
    }

    @Test(expected = IllegalArgumentException.class)
    public void sendFramesRejectsNullOther() {
        adapter.collectFrames(makeFrame(MAC, MAC, 20));
        adapter.sendFrames(null);
    }

    @Test(expected = RuntimeException.class)
    public void sendFramesWhenDownThrows() {
        adapter.collectFrames(makeFrame(MAC, MAC, 20));
        adapter.setDown();
        adapter.sendFrames(other);
    }

    @Test(expected = RuntimeException.class)
    public void sendFramesEmptyBufferThrows() {
        adapter.sendFrames(other);
    }

    // helper to concat two byte arrays
    private static byte[] concat(byte[] a, byte[] b) {
        byte[] r = Arrays.copyOf(a, a.length + b.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }
}