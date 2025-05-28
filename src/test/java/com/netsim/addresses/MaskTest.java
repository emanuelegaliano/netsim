package com.netsim.addresses;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

public class MaskTest {
        @Test
        public void parseTest() {
            Mask m = new Mask(24, 4);
            Mask t = new Mask("255.255.255.0", 4);

            assertArrayEquals(
                "Either mask prefix or string notation constructor are wrong",
                m.byteRepresentation(),
                t.byteRepresentation());
        }

        @Test
        public void byteTest() {
            // /24 on 4-byte mask should be 255.255.255.0
            Mask m = new Mask(24, 4);
            byte[] expected = new byte[] {
                (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00
            };
            assertArrayEquals(
                "Mask /24 should correspond to [255,255,255,0]",
                expected,
                m.byteRepresentation()
            );
        }

        @Test
        public void stringConstructorTest() {
            // /16 on 4-byte mask should be 255.255.0.0
            Mask fromString = new Mask("255.255.0.0", 4);
            Mask fromPrefix = new Mask(16, 4);
            byte[] expected = new byte[] {
                (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00
            };
            assertArrayEquals(
                "String constructor for \"255.255.0.0\" should yield /16 mask",
                expected,
                fromString.byteRepresentation()
            );
            assertArrayEquals(
                "String and prefix constructors should agree for /16 mask",
                fromPrefix.byteRepresentation(),
                fromString.byteRepresentation()
            );
        }
}
