package NetInteraction;

import Collections.Vehicle;
import GUI.MainForm;
import client.Client;
import managers.LocalizationManager;
import system.Request;
import system.Server;

import javax.swing.*;
import java.io.IOException;
import java.util.*;

public class ClientEvents {
    private final ClientManager clientManager;
    private LocalizationManager localizationManager;
    private static Deque<String> lastTwelveCommands = new LinkedList<>();

    public ClientEvents(ClientManager clientManager, LocalizationManager localizationManager) {
        this.clientManager = clientManager;
        this.localizationManager = localizationManager;
    }

    public boolean login(String username, char[] password) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("login", null, null, username, password);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("Login");
        if (response != null && response.getMessage().contains("Authentication successful")) {
            Client.getInstance(username, password, Long.parseLong(response.getMessage().split("User ID: ")[1])).setAuth(true);
            return true;
        } else {
            return false;
        }
    }

    public boolean register(String username, char[] password) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("register", null, null, username, password);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("Register");
        if (response != null && response.getMessage().contains("Registration successful")) {
            Client.getInstance(username, password, 0).setAuth(true);
            return true;
        } else {
            return false;
        }
    }

    public Set<Vehicle> fetchVehicles() throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("show", null, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        if (response != null) {
            return response.getVehicles();
        } else {
            JOptionPane.showMessageDialog(null, "Failed to fetch vehicles", "Error", JOptionPane.ERROR_MESSAGE);
            return new HashSet<>();
        }
    }

//    public void waitForUpdate(MainForm mainForm) {
//        new Thread(() -> {
//            try {
//            while(true) {
//                Request updateRequest = clientManager.getAnswer();
//                if (updateRequest != null && "changes".equals(updateRequest.getMessage())) {
//                    SwingUtilities.invokeLater(mainForm::updateTable);
//                }
//            }
//            } catch (IOException | ClassNotFoundException | InterruptedException e) {
//                e.printStackTrace();
//            }
//        }).start();
//    }

    public void updateLocalizationManager(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
    }

    public String infoSize() throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("info", null, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        String size = null;
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        if (response != null) {
            try {
                size = response.getMessage().split("Size: ")[1];
                return size;
            } catch (ArrayIndexOutOfBoundsException e) {
                size = "0";
                return size;
            }
        }
        return size != null ? size : "Unknown";
    }

    public void commandMode(String command, Vehicle vehicle, String key) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request(command, vehicle, key, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        handleServerResponse(response);
    }

    public boolean deleteVehicles(List<Long> ids) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("remove", null, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, ids);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("Remove");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
              handleServerResponse(response);
              return false;
        }
    }

    public boolean removeById(String id) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("removeById", null, id.trim(), Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("RemoveById");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }
    }

    public boolean removeLower(String id) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("removeLower", null, id.trim(), Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("RemoveLower");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }
    }

    public boolean clear() throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("clear", null, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("Clear");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }

    }

    public boolean updateVehicle(String id, Vehicle newVehicle) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("updateId", newVehicle, id, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }
    }

    public boolean createVehicle(Vehicle newVehicle) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("add", newVehicle, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("Add");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }
    }

    public boolean addIfMaxVehicle(Vehicle newVehicle) throws IOException, ClassNotFoundException, InterruptedException {
        Request request = new Request("addIfMax", newVehicle, null, Client.getInstance().getName(), Client.getInstance().getPasswd(), null, null);
        clientManager.sendRequest(request);
        Request response = clientManager.getAnswer();
        addCommandToHistory("AddIfMax");
        if (response.getMessage().contains("Success!")) {
            handleServerResponse(response);
            return true;
        } else {
            handleServerResponse(response);
            return false;
        }
    }

    private void handleServerResponse(Request response) {
        if (response.getMessage().contains("Success! Vehicle was added")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("addEl"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("new vehicle has lower engine power!")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("hasLowEP"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Success! Element was updated")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("updateElSC"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Failed to update element")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("updateElFl"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Is not your vehicle")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("noOwnerVehicle"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Success! Collection cleared")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("cleared"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Collection already cleared only your elements was removed")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("alreadyCleared"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Success! Your elements removed")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("removed"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("No elements with id: ")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("noElements"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Success! All your lower elements removed")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("removedAllLower"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("ERROR! elements not removed. You don't have enough rights to delete or there are no lower items.")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("noLowerOrRootElements"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("Success! All your elements removed")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("removedAllYrElements"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("You don't have enough rights to delete element.")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("noRightsToDelete"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("removeById command must contain only one required argument")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("WRargRemoveById"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.getMessage().contains("removeLower command must contain only one required argument")) {
            JOptionPane.showMessageDialog(null, localizationManager.getString("WRargRemoveLower"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
        }
    }

    public void addCommandToHistory(String command) {
        synchronized (lastTwelveCommands) {
            if (lastTwelveCommands.size() >= 12) {
                lastTwelveCommands.removeLast();
            }
            lastTwelveCommands.addFirst(command);
        }
    }

    public Deque<String> getLastTwelveCommands() {
        return lastTwelveCommands;
    }
}
