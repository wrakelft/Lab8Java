package protocol;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DatagramPart {
    public int totalParts;
    public int partNumber;
    public byte[] data;

    public DatagramPart(int totalParts, int partNumber, byte[] data) {
        this.totalParts = totalParts;
        this.partNumber = partNumber;
        this.data = data;
    }

    public ByteBuffer serialize() {
        ByteBuffer buffer = ByteBuffer.allocate(2 * Integer.BYTES + data.length);
        buffer.putInt(totalParts);
        buffer.putInt(partNumber + 1);
        buffer.put(data);
        buffer.flip();
        return buffer;
    }

    public static DatagramPart deserialize(ByteBuffer buffer) {
        int totalParts = buffer.getInt();
        int partNumber = buffer.getInt();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return new DatagramPart(totalParts, partNumber, data);
    }

    public static List<ByteBuffer> splitDataIntoParts(byte[] data) {
        int partSize = 2048;
        int totalParts = (int) Math.ceil(data.length / (double) partSize);
        List<ByteBuffer> buffers = new ArrayList<>();

        for (int i = 0; i < totalParts; i++) {
            int start = i * partSize;
            int lenght = Math.min(data.length - start, partSize);
            byte[] partData = Arrays.copyOfRange(data, start, start + lenght);

            DatagramPart part = new DatagramPart(totalParts, i, partData);
            ByteBuffer buffer = part.serialize();

            buffers.add(buffer);
        }
        return buffers;
    }


}
