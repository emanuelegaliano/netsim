package com.netsim.addresses;

import com.netsim.utils.Logger;

/**
 * Represents a transport-layer port (0–65535) using a 2-byte address format.
 */
public class Port extends Address {
    private static final Logger logger = Logger.getInstance();
    private static final String CLS = Port.class.getSimpleName();

    private int port;

    /**
     * Constructs a Port from a string port value.
     *
     * @param portStr the port number (0–65535)
     * @throws IllegalArgumentException if the port is out of range or invalid
     */
    public Port(String portStr) {
        super(portStr, 2);
        this.port = parsePort(portStr);
        logger.info("[" + CLS + "] constructed port=" + this.port);
    }

    /**
     * Sets the port value using an integer and updates the address byte array.
     */
    public void setAddress(int newPort) {
        if (newPort < 0 || newPort > 0xFFFF) {
            String msg = "Port out of range: " + newPort;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        this.port = newPort;
        this.address = shortToBytes(newPort);
        logger.info("[" + CLS + "] set port to " + this.port);
    }

    /**
     * Sets the port using a string representation of the number.
     */
    @Override
    public void setAddress(String portStr) {
        if (portStr == null) {
            String msg = "Port string cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        int parsed = parsePort(portStr);
        setAddress(parsed);
    }

    /**
     * Parses a string port into a 2-byte array (big-endian),
     * storing the numeric port in `this.port`.
     */
    @Override
    protected byte[] parse(String input) {
        int parsed = parsePort(input);
        this.port = parsed;
        byte[] bytes = shortToBytes(parsed);
        logger.debug("[" + CLS + "] parse(\"" + input + "\") → port=" + parsed);
        return bytes;
    }

    private int parsePort(String input) {
        if (input == null) {
            String msg = "Port string cannot be null";
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        int parsed;
        try {
            parsed = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            String msg = "Invalid port format: " + input;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg, e);
        }
        if (parsed < 0 || parsed > 0xFFFF) {
            String msg = "Port out of range: " + parsed;
            logger.error("[" + CLS + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        return parsed;
    }

    /** @return port value (0–65535) */
    public int getPort() {
        return port;
    }

    /** Converts an integer (0–65535) to a 2 bytes array (big-endian). */
    private static byte[] shortToBytes(int port) {
        return new byte[] {
            (byte) ((port >> 8) & 0xFF),
            (byte) (port & 0xFF)
        };
    }

    /**
     * @param data 2 bytes array of Port
     * @return a new instance of Port using that number
     * @throws IllegalArgumentException if data is null or its length != 2
     */
    public static Port fromBytes(byte[] data) {
        Logger logger = Logger.getInstance();
        String cls = Port.class.getSimpleName();

        if (data == null) {
            String msg = "Port.fromBytes: input null";
            logger.error("[" + cls + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        if (data.length != 2) {
            String msg = "Port.fromBytes: 2 bytes expected but received " + data.length;
            logger.error("[" + cls + "] " + msg);
            throw new IllegalArgumentException(msg);
        }
        int portValue = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);
        logger.info("[" + cls + "] fromBytes → port=" + portValue);
        return new Port(Integer.toString(portValue));
    }
}