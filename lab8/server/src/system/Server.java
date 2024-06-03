package system;

import Collections.Vehicle;
import clientLog.ClientHandler;
import exceptions.RootException;
import managers.CollectionManager;
import managers.CommandManager;
import managers.multithreading.MultithreadManager;
import protocol.DatagramPart;
import protocol.MessageAssembler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

public class Server {
    private InetSocketAddress address;
    private DatagramChannel channel;
    private final ExecutorService readRequesThread = Executors.newCachedThreadPool();
    private final ForkJoinPool proccesRequestPool = MultithreadManager.getRequestThreadPool();
    private final ForkJoinPool sendResponsePool = MultithreadManager.getResponseThreadPool();
    private Set<SocketAddress> clients = new HashSet<>();

    public void initialize(int port) throws IOException, RootException {
        this.address = new InetSocketAddress(port);
        Logger.getLogger(Server.class.getName()).info("Server was started at address: " + address);
        this.channel = DatagramChannel.open();
        this.channel.bind(address);
        this.channel.configureBlocking(false);

        new CommandManager();
        try {
            Logger.getLogger(Server.class.getName()).info("Downloading data from DB...");
            CollectionManager collectionManager = CollectionManager.getInstance();
            collectionManager.loadCollectionFromDB();
            Logger.getLogger(Server.class.getName()).info("Data was downloaded");
        } catch (Exception e) {
            Logger.getLogger(Server.class.getName()).warning("Error while reading\n");
            System.out.println(e.getMessage());
            System.exit(0);
        }

        Logger.getLogger(Server.class.getName()).info("Server is initialized");
    }

    public void start() {
        Logger.getLogger(Server.class.getName()).info("Server is available");
        try {
            ByteBuffer readBuffer = ByteBuffer.allocate(8192);

            startServerCommandRead();

            while (true) {
//                readBuffer.clear();
                SocketAddress clientAddress = channel.receive(readBuffer);
                if (clientAddress != null) {
                    readBuffer.flip();
                    handleClientRequest(readBuffer, clientAddress);
                    }
                }
        } catch(IOException e){
            Logger.getLogger(Server.class.getName()).severe("IO exception in server: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            Logger.getLogger(Server.class.getName()).severe("Unexpected exception in server: " + e.getMessage());
            System.exit(1);
        }
    }


    private Request readRequest(ByteBuffer readBuffer, SocketAddress clienAddress) throws IOException, ClassNotFoundException {
        MessageAssembler messageAssembler = new MessageAssembler();

        try {
            DatagramPart part = DatagramPart.deserialize(readBuffer);
            boolean isComplete = messageAssembler.addPart(part);

            if (isComplete) {
                byte[] completeMessageData = messageAssembler.assembleMessage();

                try (ByteArrayInputStream bi = new ByteArrayInputStream(completeMessageData);
                     ObjectInputStream oi = new ObjectInputStream(bi)) {
                    Logger.getLogger(Server.class.getName()).info("Request from client");
                    return (Request) oi.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    Logger.getLogger(Server.class.getName()).warning("Something wrong with request");
                }
            }
        } catch (IOException e) {
            Logger.getLogger(Server.class.getName()).warning("Error reading request: " + e.getMessage());
        }
        return null;
    }

    public void sendAnswer(DatagramChannel channel, Request request, SocketAddress clientAddress) {
        if (request != null && clientAddress != null) {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                 ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {

                objectOutputStream.writeObject(request);
                objectOutputStream.flush();
                byte[] bytes = byteArrayOutputStream.toByteArray();

                List<ByteBuffer> parts = DatagramPart.splitDataIntoParts(bytes);

                for (ByteBuffer part : parts) {
                    channel.send(part, clientAddress);
                    Logger.getLogger(Server.class.getName()).info("Answer was sent to client");
                }
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).warning("Error while sending response");
            }
        }
    }

    private void notifyClients() {
        Request updateTableRequest = new Request("changes", null, null, null, null, null, null);
        for (SocketAddress client : clients) {
            sendAnswer(channel, updateTableRequest, client);
        }
    }

    private void handleClientRequest(ByteBuffer readBuffer, SocketAddress clientAddress) throws IOException {
        readRequesThread.submit(() -> {
            try {
                  if (clientAddress != null) {
                      Request request = readRequest(readBuffer, clientAddress);
                      if (request != null) {
                          clients.add(clientAddress);
                          ClientHandler.authUserCommand(request.getName(), request.getPasswd());
                          processClientRequest(request, clientAddress);
                      }
                  }
            } catch (IOException | ClassNotFoundException | InterruptedException | ExecutionException e) {
                Logger.getLogger(Server.class.getName()).warning("Error reading request: " + e.getMessage());
            } finally {
                readBuffer.clear();
            }
        });
    }

    private void processClientRequest(Request request, SocketAddress clientAddress) throws IOException, InterruptedException, ExecutionException {
        proccesRequestPool.submit(() -> {
            Request response = CommandManager.startExecutingClientMode(request);
            sendResponsePool.submit(() -> sendAnswer(channel, response, clientAddress));
        });
    }

    private void startServerCommandRead() {
        new Thread(() -> {
            try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
                String input;
            while ((input = consoleReader.readLine()) != null) {
                if (input.equals("exit") || input.equals("save")) {
                    CommandManager.startExecutingServerMode(new Request(input, null, null, null, null, null, null));
                }
            }
            } catch (IOException e) {
                Logger.getLogger(Server.class.getName()).severe("Error reading from console: " + e.getMessage());
            }
        }).start();
    }


}










