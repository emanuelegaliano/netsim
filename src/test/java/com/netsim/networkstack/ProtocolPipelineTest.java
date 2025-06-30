package com.netsim.networkstack;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ProtocolPipelineTest {
    private ProtocolPipeline pipeline;

    // Dummy protocol that adds a prefix/suffix to the byte stream
    private static class DummyProtocol implements Protocol {
        private final byte id;

        public DummyProtocol(byte id) {
            this.id = id;
        }

        @Override
        public byte[] encapsulate(byte[] data) {
            byte[] result = new byte[data.length + 1];
            result[0] = id;
            System.arraycopy(data, 0, result, 1, data.length);
            return result;
        }

        @Override
        public byte[] decapsulate(byte[] data) {
            if (data.length == 0 || data[0] != id)
                throw new IllegalArgumentException("DummyProtocol: invalid prefix");
            byte[] result = new byte[data.length - 1];
            System.arraycopy(data, 1, result, 0, result.length);
            return result;
        }

        @Override
        public Protocol copy() {
            return new DummyProtocol(id);
        }

        @Override
        public com.netsim.addresses.Address getSource() {
            return null;
        }

        @Override
        public com.netsim.addresses.Address getDestination() {
            return null;
        }

        @Override
        public com.netsim.addresses.Address extractSource(byte[] pdu) {
            return null;
        }

        @Override
        public com.netsim.addresses.Address extractDestination(byte[] pdu) {
            return null;
        }
    }

    @Before
    public void setUp() {
        pipeline = new ProtocolPipeline();
    }

    @Test
    public void pushAndPopMaintainLIFOOrder() {
        Protocol p1 = new DummyProtocol((byte) 1);
        Protocol p2 = new DummyProtocol((byte) 2);

        pipeline.push(p1);
        pipeline.push(p2);

        assertEquals(2, pipeline.size());
        assertSame(p2, pipeline.pop());
        assertSame(p1, pipeline.pop());
        assertEquals(0, pipeline.size());
    }

    @Test(expected = RuntimeException.class)
    public void popOnEmptyStackThrows() {
        pipeline.pop();
    }

    @Test(expected = IllegalArgumentException.class)
    public void pushNullThrows() {
        pipeline.push(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateNullThrows() {
        pipeline.encapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void encapsulateEmptyThrows() {
        pipeline.encapsulate(new byte[0]);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateNullThrows() {
        pipeline.decapsulate(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void decapsulateEmptyThrows() {
        pipeline.decapsulate(new byte[0]);
    }

        @Test
        public void encapsulateAndDecapsulateCorrectly() {
            byte[] original = new byte[]{42};

            // Inseriti in ordine logico: 1 → 2 → 3
            pipeline.push(new DummyProtocol((byte) 3));
            pipeline.push(new DummyProtocol((byte) 2));
            pipeline.push(new DummyProtocol((byte) 1));

            byte[] encapsulated = pipeline.encapsulate(original);

            assertEquals(4, encapsulated.length);
            assertEquals(3, encapsulated[0]);
            assertEquals(2, encapsulated[1]);
            assertEquals(1, encapsulated[2]);
            assertEquals(42, encapsulated[3]);

            byte[] decapsulated = pipeline.decapsulate(encapsulated);
            assertArrayEquals(original, decapsulated);
        }


    @Test
    public void sizeReportsCorrectNumberOfProtocols() {
        assertEquals(0, pipeline.size());
        pipeline.push(new DummyProtocol((byte) 10));
        assertEquals(1, pipeline.size());
        pipeline.push(new DummyProtocol((byte) 20));
        assertEquals(2, pipeline.size());
        pipeline.pop();
        assertEquals(1, pipeline.size());
    }
}