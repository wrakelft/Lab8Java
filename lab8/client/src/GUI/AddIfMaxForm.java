package GUI;

import Collections.Coordinates;
import Collections.FuelType;
import Collections.Vehicle;
import Collections.VehicleType;
import GUI.MainForm;
import GUI.RoundedButton;
import GUI.RoundedTextField;
import NetInteraction.ClientEvents;
import client.Client;
import managers.LocalizationManager;
import managers.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

public class AddIfMaxForm extends JFrame {
    private JTextField nameField;
    private JTextField coordXField;
    private JTextField coordYField;
    private JTextField enginePowerField;
    private JComboBox<VehicleType> vehicleTypeComboBox;
    private JComboBox<FuelType> fuelTypeComboBox;
    private JButton saveButton;
    private JButton cancelButton;
    private ClientEvents clientEvents;
    private MainForm mainForm;
    private LocalizationManager localizationManager;
    private JLabel nameLabel;
    private JLabel coordXLabel;
    private JLabel coordYLabel;
    private JLabel enginePowerLabel;
    private JLabel vehicleTypeLabel;
    private JLabel fuelTypeLabel;

    public AddIfMaxForm(ClientEvents clientEvents, MainForm mainForm, LocalizationManager localizationManager) {
        this.clientEvents = clientEvents;
        this.mainForm = mainForm;
        this.localizationManager = mainForm.getLocalizationManager();

        setTitle(localizationManager.getString("AddIfMaxTitle"));
        setSize(400, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 255, 240));
        panel.setLayout(new GridBagLayout());
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        nameLabel = new JLabel(localizationManager.getString("name") + " :");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(nameLabel, gbc);

        nameField = createRoundedTextField();
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        coordXLabel = new JLabel(localizationManager.getString("coordX") + " :");
        coordXLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(coordXLabel, gbc);

        coordXField = createRoundedTextField();
        gbc.gridx = 1;
        panel.add(coordXField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        coordYLabel = new JLabel(localizationManager.getString("coordY") + " :");
        coordYLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(coordYLabel, gbc);

        coordYField = createRoundedTextField();
        gbc.gridx = 1;
        panel.add(coordYField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        enginePowerLabel = new JLabel(localizationManager.getString("enginePower") + " :");
        enginePowerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(enginePowerLabel, gbc);

        enginePowerField = createRoundedTextField();
        gbc.gridx = 1;
        panel.add(enginePowerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        vehicleTypeLabel = new JLabel(localizationManager.getString("vehicleType") + " :");
        vehicleTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(vehicleTypeLabel, gbc);

        vehicleTypeComboBox = createComboBox(VehicleType.values());
        gbc.gridx = 1;
        panel.add(vehicleTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        fuelTypeLabel = new JLabel(localizationManager.getString("fuelType") + " :");
        fuelTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fuelTypeLabel, gbc);

        fuelTypeComboBox = createComboBox(FuelType.values());
        gbc.gridx = 1;
        panel.add(fuelTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(255, 255, 240));
        panel.add(buttonPanel, gbc);

        saveButton = createButton(localizationManager.getString("save"));
        buttonPanel.add(saveButton);
        cancelButton = createButton(localizationManager.getString("cancel"));
        buttonPanel.add(cancelButton);

        updateTexts();

        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveVehicle();
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }

    private JTextField createRoundedTextField() {
        RoundedTextField textField = new RoundedTextField(30);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textField.setBackground(new Color(255, 255, 255));
        return textField;
    }

    private <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(145, 187, 188), 1));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        return comboBox;
    }

    private JButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setBackground(new Color(145, 187, 188));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(100, 30));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }


    private void saveVehicle() {
        try {
            String name = nameField.getText();
            if (!Validator.inputNotEmpty(name)) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("NameEMP"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            long coordX = Long.parseLong(coordXField.getText());
            if (!Validator.xGood(coordX)) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("digitone"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            int coordY = Integer.parseInt(coordYField.getText());
            if (!Validator.yGood(coordY)) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("digittwo"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }
            int enginePower = Integer.parseInt(enginePowerField.getText());
            if (!Validator.enginePowerGood(enginePower)) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("enginepowerer"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            VehicleType vehicleType = (VehicleType) vehicleTypeComboBox.getSelectedItem();
            FuelType fuelType = (FuelType) fuelTypeComboBox.getSelectedItem();

            Vehicle newVehicle = new Vehicle(0, name, new Coordinates(coordX, coordY), new Date(0), enginePower, vehicleType, fuelType, Client.getInstance().getId());

            boolean success = clientEvents.addIfMaxVehicle(newVehicle);
            if (success) {
                mainForm.updateTable();
                dispose();
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("validNum"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateTexts() {
        setTitle(localizationManager.getString("createVehT"));
        nameLabel.setText(localizationManager.getString("name"));
        coordXLabel.setText(localizationManager.getString("coordX"));
        coordYLabel.setText(localizationManager.getString("coordY"));
        enginePowerLabel.setText(localizationManager.getString("enginePower"));
        vehicleTypeLabel.setText(localizationManager.getString("vehicleType"));
        fuelTypeLabel.setText(localizationManager.getString("fuelType"));

        saveButton.setText(localizationManager.getString("save"));
        cancelButton.setText(localizationManager.getString("cancel"));


    }

}



