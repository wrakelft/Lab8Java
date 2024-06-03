package protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public class MessageAssembler {
    private HashMap<Integer, DatagramPart> parts = new HashMap<>();
    private int expectedParts = -1;

    public boolean addPart(DatagramPart part) {
        if(expectedParts == -1) expectedParts = part.totalParts;
        if (part.totalParts != expectedParts) return false;

        parts.put(part.partNumber, part);

        return parts.size() == expectedParts;
    }

    public byte[] assembleMessage() throws IOException {
        if(parts.size() != expectedParts) {
            throw new IOException("No parts in message");
        }
        ByteBuffer assembledData = ByteBuffer.allocate(expectedParts * 8192);
        for (int i = 1; i <= expectedParts; i++) {
            DatagramPart part = parts.get(i);
            if (part == null) {
                throw new IOException("No part number " + i);
            }
            assembledData.put(part.data);
        }
        assembledData.flip();
        byte[] completeMessage = new byte[assembledData.remaining()];
        assembledData.get(completeMessage);
        return completeMessage;
    }
}
