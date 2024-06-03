package NetInteraction;

import protocol.DatagramPart;
import protocol.MessageAssembler;
import system.Request;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.List;
import java.util.logging.Logger;

public class ClientManager {
    private final InetSocketAddress address;
    private final DatagramChannel channel;

    public ClientManager(String host, int port) throws IOException {
        this.address = new InetSocketAddress(host, port);
        this.channel = DatagramChannel.open();
        this.channel.configureBlocking(false);
    }

    public void sendRequest(Request request) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(request);
        objectOutputStream.flush();
        byte[] data = byteArrayOutputStream.toByteArray();

        List<ByteBuffer> parts = DatagramPart.splitDataIntoParts(data);

        for (ByteBuffer part : parts) {
            channel.send(part, address);
        }
    }

    public Request getAnswer() throws IOException, InterruptedException, ClassNotFoundException {
        MessageAssembler assembler = new MessageAssembler();
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        long timeout = 0;
        try {
            while (true) {
                buffer.clear();
                SocketAddress serverAddres = channel.receive(buffer);
                if (serverAddres != null) {
                    buffer.flip();
                    DatagramPart part = DatagramPart.deserialize(buffer);
                    if(assembler.addPart(part)) {
                        break;
                    }
                }
                Thread.sleep(1000);
            }

            byte[] completeData = assembler.assembleMessage();

            ByteArrayInputStream bi = new ByteArrayInputStream(completeData);
            ObjectInputStream oi = new ObjectInputStream(bi);
            try {
                Request response = (Request) oi.readObject();
                Logger.getLogger(ClientManager.class.getName()).info("Response received from server: " + response.getMessage());
                return response;
            } finally {
                oi.close();
            }
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return null;
    }


}
