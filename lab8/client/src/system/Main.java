package system;

import GUI.AuthForm;
import GUI.MainForm;
import NetInteraction.ClientEvents;
import NetInteraction.ClientManager;
import managers.LocalizationManager;

import javax.swing.*;
import java.io.IOException;
import java.util.Locale;

public class Main {
    public static void main(String[] args) throws IOException {
           if (args.length != 2) {
               System.out.println("Wrong argument in address or host");
               return;
           }

           String host = args[0];
           int port;

           try {
               port = Integer.parseInt(args[1]);
           } catch (NumberFormatException e) {
               System.out.println("Port must be a number");
               return;
           }

        SwingUtilities.invokeLater(() -> {
            try {
                LocalizationManager localizationManager = new LocalizationManager(Locale.getDefault());
                ClientManager clientManager = new ClientManager(host, port);
                ClientEvents clientEvents = new ClientEvents(clientManager, localizationManager);

                Server server = new Server(clientEvents);

                AuthForm authForm = new AuthForm(localizationManager);
                authForm.setVisible(true);

                authForm.addLoginListener(e -> {
                    String username = authForm.getUsername();
                    char[] password = authForm.getPassword();
                    try {
                        boolean success = clientEvents.login(username, password);
                        if (success) {
                            authForm.setVisible(false);
                            MainForm mainForm = new MainForm(clientEvents, username, localizationManager);
                            mainForm.setVisible(true);
                        } else {
                            JOptionPane.showMessageDialog(authForm, "Login failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(authForm, "An error occurred during login. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });

                authForm.addRegisterListener(e -> {
                    String username = authForm.getUsername();
                    char[] password = authForm.getPassword();
                    try {
                       boolean success =  clientEvents.register(username, password);
                       if (success) {
                           JOptionPane.showMessageDialog(authForm, "You have registered successfully", "Info", JOptionPane.INFORMATION_MESSAGE);
                       } else {
                           JOptionPane.showMessageDialog(authForm, "Registration failed. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                       }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(authForm, "An error occurred during registration. Please try again.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        });


    }
}
