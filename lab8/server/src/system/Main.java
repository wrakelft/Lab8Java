package system;

import exceptions.NoArgumentException;
import managers.CommandManager;

import java.io.File;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.logging.Logger;

/**
 * Класс, который запускает программу
 *
 * @author wrakelft
 * @since 1.0
 */
public class Main {
    /**
     * Начало программы
     *
     * @param args аргумент командной строки
     */
    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                CommandManager.startExecutingServerMode(new Request("save", null, null, null, null, null, null));
            } catch (Exception e) {
                Logger.getLogger(Main.class.getName()).info("Perform additional actions before ending the program...");
                System.out.println("Something went wrong");
            }
        }));

        if (args.length != 1) {
            System.out.println("Error: port");
            Logger.getLogger(Main.class.getName()).warning("Something wrong with settings of server or file");
            System.exit(1);
        }
        int port = Integer.parseInt(args[0]);

        try {
            Server server = new Server();
            server.initialize(port);
            server.start();
        } catch (Exception e) {
            Logger.getLogger(Main.class.getName()).warning("Something wrong with settings of server or file" + e.getMessage());
            System.exit(1);
        }

    }
}