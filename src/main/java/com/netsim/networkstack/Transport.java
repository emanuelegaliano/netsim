package com.netsim.networkstack;

import com.netsim.utils.Logger;
import com.netsim.utils.ExitStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;

public class Transport implements Protocol {
    private int sourcePort;
    private int destinationPort;
    private int segmentSize;

    public Transport(int segmentSize, int sourcePort) throws IllegalArgumentException {
        if(segmentSize <= 0)
            throw new IllegalArgumentException("SegmentSize must be greater than 0");

        this.segmentSize = segmentSize;
        this.sourcePort = sourcePort;
    }

    private byte[] toByte(Payload p) 
    throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(p);
        return baos.toByteArray();
    }

    public void bindTo(int newPort) {
        this.destinationPort = newPort;
    }

    public List<PDU> encapsulate(PDU payload) throws IllegalArgumentException {
        if(payload.getDestination() == null)
            throw new IllegalArgumentException("Destination port is null");
        
        try {
            byte[] bytePayload = this.toByte(payload.getPayload());
            
            return null;

        } catch(IOException e) {
            String errMsg = ExitStatus.TRANSPORT_ERROR 
                         + "Unable to segment payload in trasnport layer due to: " 
                         + e;

            Logger.getInstance().error(errMsg);
            System.exit(ExitStatus.TRANSPORT_ERROR);
        } catch(ClassNotFoundException e) {
            String errMsg = ExitStatus.TRANSPORT_ERROR 
                         + "Unable to segment payload in trasnport layer due to: " 
                         + e;

            Logger.getInstance().error(errMsg);
            System.exit(ExitStatus.TRANSPORT_ERROR);
        }

        @SuppressWarnings("unused")
        assert true: "Unreacheable piece of code";
    }

    public PDU decapsulate(PDU payload) {
        return null;
    }

    public int getSourcePort() {
        return this.sourcePort;
    }

    public int getDestinationPort() {
        return this.destinationPort;
    }
}
