package com.netsim.protocols.IPv4;

import java.nio.ByteBuffer;

import com.netsim.addresses.IPv4;
import com.netsim.networkstack.PDU;
import com.netsim.utils.Logger;

/**
 * Represents a minimal IPv4 datagram (base header, no options),
 * with TTL and Protocol fields expanded to 16 bits each,
 * and checksum not computed.
 */
public class IPv4Packet extends PDU {
      private static final Logger logger = Logger.getInstance();
      private static final String CLS = IPv4Packet.class.getSimpleName();

      private final VersionIHL versionAndIHL;
      private final byte tos;
      private final short totalLength;
      private final short identification;
      private final short flagsAndFragmentOffset;
      private final short ttl;       // now 16-bit
      private final short protocol;  // now 16-bit
      private final byte[] payload;

      /**
       * Constructs a new IPv4 packet (header base, without options).
       *
       * @param source IPv4 source address (non-null)
       * @param destination IPv4 destination address (non-null)
       * @param version IP version (must be 4)
       * @param IHL Internet Header Length in 32-bit words (5…15)
       * @param typeOfService Type-Of-Service (0…255)
       * @param totalLength total length (header + payload) in bytes (0…65535)
       * @param identification Identification field (0…65535)
       * @param flags flags (3 bits, 0…7)
       * @param fragmentOffset fragment offset (13 bits, 0…8191)
       * @param ttl Time-To-Live (0…65535)
       * @param protocol upper-layer protocol number (0…65535)
       * @param payload the UDP segment payload (non-null)
       * @throws IllegalArgumentException if any argument is out of range or null
       */
      public IPv4Packet(IPv4 source,
                        IPv4 destination,
                        int version,
                        int IHL,
                        int typeOfService,
                        int totalLength,
                        int identification,
                        int flags,
                        int fragmentOffset,
                        int ttl,
                        int protocol,
                        byte[] payload) {
            super(source, destination);
            try {
                  if (source == null || destination == null)
                  throw new IllegalArgumentException("IPv4Packet: source/destination cannot be null");

                  this.versionAndIHL = new VersionIHL(version, IHL);

                  if (typeOfService < 0 || typeOfService > 0xFF)
                  throw new IllegalArgumentException("IPv4Packet: TOS must be 0…255");
                  this.tos = (byte) typeOfService;

                  if (totalLength < 0 || totalLength > 0xFFFF)
                  throw new IllegalArgumentException("IPv4Packet: totalLength must be 0…65535");
                  this.totalLength = (short) totalLength;

                  if (identification < 0 || identification > 0xFFFF)
                  throw new IllegalArgumentException("IPv4Packet: identification must be 0…65535");
                  this.identification = (short) identification;

                  if (flags < 0 || flags > 0x7)
                  throw new IllegalArgumentException("IPv4Packet: flags must be 0…7");
                  if (fragmentOffset < 0 || fragmentOffset > 0x1FFF)
                  throw new IllegalArgumentException("IPv4Packet: fragmentOffset must be 0…8191");
                  this.flagsAndFragmentOffset = (short) (((flags & 0x7) << 13)
                                                      | (fragmentOffset & 0x1FFF));

                  if (ttl < 0 || ttl > 0xFFFF)
                  throw new IllegalArgumentException("IPv4Packet: TTL must be 0…65535");
                  this.ttl = (short) ttl;

                  if (protocol < 0 || protocol > 0xFFFF)
                  throw new IllegalArgumentException("IPv4Packet: protocol must be 0…65535");
                  this.protocol = (short) protocol;

                  if (payload == null || payload.length == 0)
                  throw new IllegalArgumentException("IPv4Packet: payload cannot be null or 0");
                  this.payload = payload;

                  logger.info("[" + CLS + "] constructed: src=" 
                              + source.stringRepresentation()
                              + " dst=" + destination.stringRepresentation()
                              + " ttl=" + this.ttl);
            } catch (IllegalArgumentException e) {
                  logger.error("[" + CLS + "] constructor error: " + e.getMessage());
                  throw e;
            }
      }

      /**
       * Builds the IPv4 header (version/IHL, TOS, total length, identification,
       * flags+offset, TTL, protocol, source & destination) in network order,
       * not computing checksum.
       *
       * @return a byte[] of length IHL*4 (20 bytes when IHL=5)
       */
      public byte[] getHeader() {
            logger.debug("[" + CLS + "] getHeader()");
            int headerLen = versionAndIHL.getIhl() * 4;
            ByteBuffer buf = ByteBuffer.allocate(headerLen);
            buf.put(versionAndIHL.toByte());
            buf.put(tos);
            buf.putShort(totalLength);
            buf.putShort(identification);
            buf.putShort(flagsAndFragmentOffset);
            buf.putShort(ttl);
            buf.putShort(protocol);
            buf.put(this.getSource().byteRepresentation());
            buf.put(this.getDestination().byteRepresentation());
            byte[] header = buf.array();
            logger.debug("[" + CLS + "] header built, length=" + header.length);
            return header;
      }

      /**
       * Serializes the entire IPv4 packet (header + UDP payload) in network order.
       *
       * @return the full datagram as a byte array
       */
      @Override
      public byte[] toByte() {
            logger.info("[" + CLS + "] toByte()");
            byte[] headerBytes = getHeader();
            ByteBuffer buf = ByteBuffer.allocate(headerBytes.length + payload.length);
            buf.put(headerBytes).put(payload);
            byte[] packet = buf.array();
            logger.info("[" + CLS + "] serialized packet, total length=" + packet.length);
            return packet;
      }
}
