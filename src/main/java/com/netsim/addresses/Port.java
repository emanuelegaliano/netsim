package com.netsim.addresses;

/**
 * Represents a transport-layer port (0–65535) using a 2-byte address format.
 */
public class Port extends Address {

    private int port;

    /**
     * Constructs a Port from an integer port value.
     *
     * @param port the port number (0–65535)
     * @throws IllegalArgumentException if the port is out of range
     */
    public Port(String port) {
        super(port, 2);
    }

    /**
     * Sets the port value using an integer and updates the address byte array.
     *
     * @param newPort the port number (0–65535)
     * @throws IllegalArgumentException if the port is out of range
     */
    public void setAddress(int newPort) {
        if(newPort < 0 || newPort > 0xFFFF) {
            throw new IllegalArgumentException("Port out of range: " + newPort);
        }
        this.port = newPort;
        this.address = shortToBytes(newPort);
    }

    /**
     * Sets the port using a string representation of the number.
     *
     * @param portStr the string representation of the port
     * @throws IllegalArgumentException if the string is null, non‐numerico
     *                                  o fuori dal range 0–65535
     */
    @Override
    public void setAddress(String portStr) {
        if(portStr == null) {
            throw new IllegalArgumentException("Port string cannot be null");
        }

        int parsed;
        try {
            parsed = Integer.parseInt(portStr.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port format: " + portStr, e);
        }
        this.setAddress(parsed);
    }

    /**
     * Parses a string port into a 2-byte array (big-endian).
     *
     * @param input the port string
     * @return byte array representing the port
     * @throws IllegalArgumentException se input è null o fuori range
     */
    protected byte[] parse(String input) {
        if(input == null) {
            throw new IllegalArgumentException("Port string cannot be null");
        }

        int parsed;
        try {
            parsed = Integer.parseInt(input.trim());
            this.port = parsed;
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Invalid port format: " + input, e);
        }
        if(parsed < 0 || parsed > 0xFFFF) {
            throw new IllegalArgumentException("Port out of range: " + parsed);
        }
        return shortToBytes(parsed);
    }

    /**
     * @return il valore del port (0–65535)
     */
    public int getPort() {
        return port;
    }

    /**
     * Converte un intero 0–65535 in un array di 2 byte (big-endian).
     */
    private static byte[] shortToBytes(int port) {
        return new byte[] {
            (byte) ((port >> 8) & 0xFF),
            (byte) (port & 0xFF)
        };
    }
    
    /**
    * @param data array di 2 byte che codifica il numero di porta
    * @return una nuova istanza di Port con quel numero
    * @throws IllegalArgumentException se data è null o non di lunghezza 2,
    *         o se il valore risultante è fuori dal range 0–65535
    */
    public static Port fromBytes(byte[] data) {
        if(data == null) 
            throw new IllegalArgumentException("Port.fromBytes: input null");
        if(data.length != 2) {
            throw new IllegalArgumentException(
                "Port.fromBytes: 2 bytes expected but received " 
                + data.length);
        }

        // interpreta in big-endian
        int portValue = ((data[0] & 0xFF) << 8) | (data[1] & 0xFF);

        // il costruttore Port(String) sfrutta parse() interno per validare e settare
        return new Port(Integer.toString(portValue));
    }
}
