package system;

import Collections.Vehicle;
import NetInteraction.ClientEvents;
import NetInteraction.ClientManager;
import client.Client;
import exceptions.NoArgumentException;
import exceptions.WrongArgumentException;
//import managers.ExecuteScriptCommand;
import managers.HistoryCommand;
import protocol.DatagramPart;
import protocol.MessageAssembler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Server {
    private static Deque<String> lastTwelveCommands = new LinkedList<>();
    private final ClientEvents clientEvents;

    public Server(ClientEvents clientEvents) {
        this.clientEvents = clientEvents;
    }

    public void processCommand(String command) {
        synchronized (lastTwelveCommands) {
            if (lastTwelveCommands.size() >= 12) {
                lastTwelveCommands.removeLast();
            }
            lastTwelveCommands.addFirst(command);
        }

        try {
            Vehicle vehicle = new Vehicle();
            String key = null;
            boolean isClientCommand = false;
            if (command.contains("removeLower") || command.contains("removeById") || command.contains("countByFuelType") || command.contains("countLessThenFuelType")) {
                if (command.split(" ").length == 2) {
                    key = command.split(" ")[1];
                }
            } else if (command.equals("add") || command.equals("addIfMax")) {
//                vehicle = VehicleAsker.createVehicle();
//            } else if (command.split(" ")[0].equals("executeScript")) {
//                ExecuteScriptCommand.execute(command);
//                isClientCommand = true;
            } else if (command.split(" ")[0].equals("updateId")) {
                if (command.split(" ").length == 2) {
                    key = command.split(" ")[1];
                } else {
                    throw new NoArgumentException("id");
                }
//                vehicle = VehicleAsker.createVehicle();
                vehicle.setId(Long.parseLong(key));
            } else if (command.split(" ")[0].equals("history")) {
                if (command.split(" ").length == 1) {
                    HistoryCommand.execute(lastTwelveCommands);
                    isClientCommand = true;
                } else {
                    throw new WrongArgumentException(command.split(" ")[1]);
                }
            }
            if (!isClientCommand) {
                clientEvents.commandMode(command, vehicle, key);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Deque<String> getLastTwelveCommands() {
        return lastTwelveCommands;
    }
}
