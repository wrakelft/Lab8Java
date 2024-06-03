package managers;

import Collections.Vehicle;
import GUI.MainForm;
import NetInteraction.ClientEvents;
import client.Client;
import exceptions.*;
import system.Request;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class ExecuteScriptCommand {
    private static Stack<File> stack = new Stack<>();
    private static LocalizationManager localizationManager;
    private static MainForm mainForm;

    public static void execute(String command, ClientEvents clientEvents, LocalizationManager locManager, MainForm form) throws Exception {
        localizationManager = locManager;
        mainForm = form;
        try {
            String[] argums = command.trim().split("\\s+");
            if (argums.length > 2) {
                throw new WrongArgumentException(argums[2]);
            }
            if (argums.length < 2) {
                throw new NoArgumentException("script name");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        File file = new File(command.trim().split("\\s+")[1]);
        try {
            if (!file.canRead() || !file.exists()) {
                throw new RootException();
            } else if (stack.contains(file)) {
                throw new RecursionException();
            }
            stack.add(file);
        } catch (RecursionException ex) {
            stack.pop();
            JOptionPane.showMessageDialog(null, ex.getMessage(), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            if (!stack.isEmpty()) {
                stack.pop();
            }
            return;
        } catch (RootException e) {
            JOptionPane.showMessageDialog(null, e.getMessage(), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        String path = command.trim().split("\\s+")[1];
        try (var br = new BufferedReader(new InputStreamReader(new FileInputStream(path), StandardCharsets.UTF_8))) {
            String line;
            String[] vehicleData = new String[7];
            boolean isSpecialCommandExecuted = false;

            while ((line = br.readLine()) != null) {
                isSpecialCommandExecuted = false;
                String[] parts = line.split(" ", 2);

                switch (parts[0]) {
                    case "add":
                        handleAddCommand(br, vehicleData, clientEvents);
                        isSpecialCommandExecuted = true;
                        vehicleData = new String[7];
                        break;

                    case "executeScript":
                        handleExecuteScriptCommand(parts, clientEvents);
                        isSpecialCommandExecuted = true;
                        break;

                    case "updateId":
                        vehicleData = new String[6];
                        handleUpdateIdCommand(parts, br, vehicleData, clientEvents);
                        isSpecialCommandExecuted = true;
                        vehicleData = new String[7];
                        break;

                    case "addIfMax":
                        handleAddIfMaxCommand(br, vehicleData, clientEvents);
                        isSpecialCommandExecuted = true;
                        vehicleData = new String[7];
                        break;

                    default:
                        handleDefaultCommand(line, parts, clientEvents);
                        break;
                }
            }
            stack.pop();
            JOptionPane.showMessageDialog(null, localizationManager.getString("scriptCompleted"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("errorInScript") + e.getMessage(), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void handleAddCommand(BufferedReader br, String[] vehicleData, ClientEvents clientEvents) throws Exception {
        for (int i = 0; i < vehicleData.length; i++) {
            if (i == 6) {
                vehicleData[i] = new Date(0).toString();
            } else {
                vehicleData[i] = br.readLine();
                validateVehicleData(i, vehicleData[i]);
            }
        }
        // Send the add request
        clientEvents.createVehicle(new Vehicle(vehicleData));
        mainForm.updateTable();                               // Un-comment this line and replace with actual call
    }

    private static void handleExecuteScriptCommand(String[] parts, ClientEvents clientEvents) throws Exception {
        if (parts.length > 2 || parts.length == 1) {
            throw new WrongArgumentException(parts[0]);
        }

        String scriptPath = parts[1];
        File scriptFile = new File(scriptPath);
        if (!scriptFile.canRead()) {
            throw new RootException();
        }
        if (stack.contains(scriptFile)) {
            throw new RecursionException();
        } else {
            execute(parts[0] + " " + parts[1], clientEvents, localizationManager, mainForm);  // Adjust ClientEvents instance as needed
        }
    }

    private static void handleUpdateIdCommand(String[] parts, BufferedReader br, String[] vehicleData, ClientEvents clientEvents) throws Exception {
        if (parts.length < 2) {
            throw new NoArgumentException("id");
        }

        long id;
        try {
            id = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            throw new WrongArgumentException(parts[1]);
        }

        for (int i = 0; i < vehicleData.length; i++) {
            vehicleData[i] = br.readLine();
            validateVehicleData(i, vehicleData[i]);
        }
        // Send the updateId request
        clientEvents.updateVehicle(String.valueOf(id), new Vehicle(vehicleData));  // Un-comment this line and replace with actual call
        mainForm.updateTable();
    }

    private static void handleAddIfMaxCommand(BufferedReader br, String[] vehicleData, ClientEvents clientEvents) throws Exception {
        for (int i = 0; i < vehicleData.length; i++) {
            if (i == 6) {
                vehicleData[i] = new Date(0).toString();
            } else {
                vehicleData[i] = br.readLine();
                validateVehicleData(i, vehicleData[i]);
            }
        }
        // Send the addIfMax request
        clientEvents.addIfMaxVehicle(new Vehicle(vehicleData));  // Un-comment this line and replace with actual call
        mainForm.updateTable();
    }

    private static void handleDefaultCommand(String line, String[] parts, ClientEvents clientEvents) throws Exception {
        if (line.contains("removeById") || line.contains("removeLower")) {
            if (parts.length < 2) {
                throw new NoArgumentException("key");
            }
            clientEvents.commandMode(line.split(" ")[0], null, parts[1]);
        }
         else if (line.contains("clear")) {
            clientEvents.clear();
        } else {
            throw new WrongCommandException(line);
        }
    }

    private static void validateVehicleData(int index, String data) throws Exception {
        switch (index) {
            case 0:
                if (!Validator.inputNotEmpty(data)) {
                    throw new ValidationException(localizationManager.getString("nameEMP"));
                }
                break;
            case 1:
                if (!Validator.inputNotEmpty(data) || !Validator.xGood(Long.parseLong(data))) {
                    throw new ValidationException(localizationManager.getString("digitone"));
                }
                break;
            case 2:
                if (!Validator.inputNotEmpty(data) || !Validator.yGood(Integer.parseInt(data))) {
                    throw new ValidationException(localizationManager.getString("digittwo"));
                }
                break;
            case 3:
                if (!Validator.inputNotEmpty(data) || !Validator.enginePowerGood(Integer.parseInt(data))) {
                    throw new ValidationException(localizationManager.getString("enginepowerer"));
                }
                break;
            case 4:
                if (!Validator.inputNotEmpty(data) || !Validator.typeGood(data)) {
                    throw new ValidationException(localizationManager.getString("vehicleTypeError"));
                }
                break;
            case 5:
                if (!Validator.inputNotEmpty(data) || !Validator.fuelTypeGood(data)) {
                    throw new ValidationException(localizationManager.getString("fuelTypeError"));
                }
                break;
            default:
                throw new ValidationException(localizationManager.getString("unknownError"));
        }
    }
}
