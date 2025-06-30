package com.netsim.addresses;

import com.netsim.utils.Logger;

/**
 * Represents a transport‐layer port (0–65535) with a 2‐byte big‐endian encoding.
 */
public class Port extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS    = Port.class.getSimpleName();

    private int port;

    /**
     * Constructs a Port from a decimal string.
     *
     * @param portStr the port number as string (0–65535)
     * @throws IllegalArgumentException if portStr is null, non‐numeric, or out of range
     */
    public Port(String portStr) throws IllegalArgumentException {
        super(portStr, 2);
        this.port = this.parsePort(portStr);
        logger.info("[" + CLS + "] constructed port=" + this.port);
    }

    /**
     * Parses a string into an integer port value.
     *
     * @param input the port number as string
     * @return the parsed port (0–65535)
     * @throws IllegalArgumentException if input is null, non‐numeric, or out of range
     */
    private int parsePort(String input) throws IllegalArgumentException {
        if (input == null) {
            String msg = "Port string cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        int value;
        try {
            value = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            String msg = "Invalid port format: " + input;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg, e);
        }
        if (value < 0 || value > 0xFFFF) {
            String msg = "Port out of range: " + value;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        return value;
    }

    /**
     * Encodes the given port into the internal byte array.
     *
     * @param newPort the port number (0–65535)
     * @throws IllegalArgumentException if newPort is out of range
     */
    public void setAddress(int newPort) throws IllegalArgumentException {
        if (newPort < 0 || newPort > 0xFFFF) {
            String msg = "Port out of range: " + newPort;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.port    = newPort;
        this.address = Port.shortToBytes(newPort);
        logger.info("[" + CLS + "] set port to " + this.port);
    }

    /**
     * Parses a string and updates this port.
     *
     * @param portStr the port number as string
     * @throws IllegalArgumentException if portStr is null, non‐numeric, or out of range
     */
    @Override
    public void setAddress(String portStr) throws IllegalArgumentException {
        int parsed = this.parsePort(portStr);
        this.setAddress(parsed);
    }

    /**
     * Parses the address bytes from the string.
     *
     * @param input the port number as string
     * @return a 2‐byte big‐endian representation
     * @throws IllegalArgumentException if input is invalid
     */
    @Override
    protected byte[] parse(String input) throws IllegalArgumentException {
        int parsed = this.parsePort(input);
        this.port  = parsed;
        byte[] result = Port.shortToBytes(parsed);
        logger.debug("[" + CLS + "] parse(\"" + input + "\") → port=" + parsed);
        return result;
    }

    /**
     * @return the port value (0–65535)
     */
    public int getPort() {
        return this.port;
    }

    /**
     * Converts an integer to a 2‐byte big‐endian array.
     *
     * @param port the port number (0–65535)
     * @return a 2‐byte array
     */
    private static byte[] shortToBytes(int port) {
        return new byte[] {
            (byte) ((port >> 8) & 0xFF),
            (byte) ( port        & 0xFF)
        };
    }

    /**
     * Constructs a Port from a 2‐byte big‐endian array.
     *
     * @param data a 2‐byte array
     * @return a new Port instance
     * @throws IllegalArgumentException if data is null or length≠2
     */
    public static Port fromBytes(byte[] data) throws IllegalArgumentException {
        if (data == null) {
            String msg = "Port.fromBytes: input null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        if (data.length != 2) {
            String msg = "Port.fromBytes: expected 2 bytes but got " + data.length;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        int portValue = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        logger.info("[" + CLS + "] fromBytes → port=" + portValue);
        return new Port(Integer.toString(portValue));
    }
}