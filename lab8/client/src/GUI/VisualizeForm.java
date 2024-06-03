package GUI;

import Collections.*;
import NetInteraction.ClientEvents;
import managers.LocalizationManager;
import managers.Validator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class VisualizeForm extends JFrame {
    private List<Vehicle> vehicles;
    private ClientEvents clientEvents;
    private MainForm mainForm;
    private JPanel visualizationPanel;
    private Map<Long, Color> userColors;
    private Map<Vehicle, Integer> vehicleSizes;
    private Vehicle hoveredVehicle;
    private LocalizationManager localizationManager;
    private JLabel nameLabel;
    private JLabel coordXLabel;
    private JLabel coordYLabel;
    private JLabel enginePowerLabel;
    private JLabel vehicleTypeLabel;
    private JLabel fuelTypeLabel;
    private JButton saveButton;
    private JButton cancelButton;

    private static final int GRID_SIZE = 2000;
    private static final int CELL_SIZE = 40;
    private static final int VEHICLE_SIZE = 20;
    private static final int MIN_DISTANCE = 60; // Увеличенное минимальное расстояние
    private static final int HOVERED_VEHICLE_SIZE = 15;

    public VisualizeForm(ClientEvents clientEvents, MainForm mainForm) {
        this.clientEvents = clientEvents;
        this.mainForm = mainForm;
        this.vehicles = mainForm.getVehiclesCache();
        this.userColors = new HashMap<>();
        this.localizationManager = mainForm.getLocalizationManager();
        this.vehicleSizes = new HashMap<>();

        setTitle(localizationManager.getString("VisualizeTitle"));
        setSize(800, 800);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        visualizationPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawGrid(g);
                drawVehicles(g);
            }
        };
        visualizationPanel.setPreferredSize(new Dimension(GRID_SIZE, GRID_SIZE));
        visualizationPanel.setBackground(new Color(245, 245, 220));
        visualizationPanel.setLayout(null);

        JScrollPane scrollPane = new JScrollPane(visualizationPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(16);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane);

        // Центрирование на координате (0, 0)
        SwingUtilities.invokeLater(() -> {
            scrollPane.getHorizontalScrollBar().setValue((GRID_SIZE - scrollPane.getWidth()) / 2);
            scrollPane.getVerticalScrollBar().setValue((GRID_SIZE - scrollPane.getHeight()) / 2);
        });

        initializeVehicleSizes();
        addMouseListeners();
    }

    private void initializeVehicleSizes() {
        for (Vehicle vehicle : vehicles) {
            vehicleSizes.put(vehicle, VEHICLE_SIZE);
        }
    }

    private Color getColorForUser(long userId) {
        if (userColors.containsKey(userId)) {
            return userColors.get(userId);
        }

        Random random = new Random(userId);
        Color color = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        userColors.put(userId, color);

        return color;
    }

    private void drawGrid(Graphics g) {
        g.setColor(Color.LIGHT_GRAY);
        int width = visualizationPanel.getWidth();
        int height = visualizationPanel.getHeight();
        for (int i = 0; i <= width; i += CELL_SIZE) {
            g.drawLine(i, 0, i, height);
        }
        for (int i = 0; i <= height; i += CELL_SIZE) {
            g.drawLine(0, i, width, i);
        }

        // Отрисовка осей
        g.setColor(Color.BLACK);
        g.drawLine(width / 2, 0, width / 2, height); // Вертикальная ось
        g.drawLine(0, height / 2, width, height / 2); // Горизонтальная ось
    }

    public void updateVisualization(List<Vehicle> updatedVehicles) {
        this.vehicles = updatedVehicles;
        initializeVehicleSizes();
        repaint();
    }

    private void drawVehicles(Graphics g) {
        int width = visualizationPanel.getWidth();
        int height = visualizationPanel.getHeight();

        // Копирование исходных координат
        Map<Vehicle, Point> vehiclePositions = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            int x = (int) ((vehicle.getCoordinates().getX() + 1000) * (width - VEHICLE_SIZE) / 2000);
            int y = (int) ((vehicle.getCoordinates().getY() + 1000) * (height - VEHICLE_SIZE) / 2000);
            vehiclePositions.put(vehicle, new Point(x, y));
        }

        // Отталкивание объектов
        for (int i = 0; i < 10; i++) { // Несколько итераций для лучшего раздвигания
            for (Vehicle v1 : vehicles) {
                for (Vehicle v2 : vehicles) {
                    if (v1 != v2) {
                        Point p1 = vehiclePositions.get(v1);
                        Point p2 = vehiclePositions.get(v2);

                        double dx = p1.x - p2.x;
                        double dy = p1.y - p2.y;
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        if (distance < MIN_DISTANCE) {
                            double angle = Math.atan2(dy, dx);
                            int shiftX = (int) (Math.cos(angle) * (MIN_DISTANCE - distance) / 2);
                            int shiftY = (int) (Math.sin(angle) * (MIN_DISTANCE - distance) / 2);

                            p1.translate(shiftX, shiftY);
                            p2.translate(-shiftX, -shiftY);
                        }
                    }
                }
            }
        }

        // Отрисовка объектов
        for (Map.Entry<Vehicle, Point> entry : vehiclePositions.entrySet()) {
            Vehicle vehicle = entry.getKey();
            Point position = entry.getValue();
            int size = vehicleSizes.get(vehicle);

            Color color = getColorForUser(vehicle.getOwner());
            g.setColor(color);
            g.fillRect(position.x, position.y, size, size);
        }
    }

    private void addMouseListeners() {
        visualizationPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int width = visualizationPanel.getWidth();
                    int height = visualizationPanel.getHeight();
                    Map<Vehicle, Point> vehiclePositions = getVehiclePositions(width, height);

                    for (Vehicle vehicle : vehicles) {
                        Point position = vehiclePositions.get(vehicle);
                        if (position != null && e.getX() >= position.x && e.getX() <= position.x + VEHICLE_SIZE && e.getY() >= position.y && e.getY() <= position.y + VEHICLE_SIZE) {
                            showVehicleInfo(vehicle);
                            break;
                        }
                    }
                } if (SwingUtilities.isLeftMouseButton(e)) {
                    int width = visualizationPanel.getWidth();
                    int height = visualizationPanel.getHeight();
                    Map<Vehicle, Point> vehiclePositions = getVehiclePositions(width, height);



                        for (Vehicle vehicle : vehicles) {
                            Point position = vehiclePositions.get(vehicle);
                            if (position != null && e.getX() >= position.x && e.getX() <= position.x + VEHICLE_SIZE && e.getY() >= position.y && e.getY() <= position.y + VEHICLE_SIZE) {
                                deleteVehicle(vehicle);
                                break;
                            }
                        }
                    }
                }
        });

        visualizationPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int width = visualizationPanel.getWidth();
                int height = visualizationPanel.getHeight();
                Map<Vehicle, Point> vehiclePositions = getVehiclePositions(width, height);

                Vehicle hoveredVehicle = null;
                for (Vehicle vehicle : vehicles) {
                    Point position = vehiclePositions.get(vehicle);
                    if (position != null && e.getX() >= position.x && e.getX() <= position.x + VEHICLE_SIZE && e.getY() >= position.y && e.getY() <= position.y + VEHICLE_SIZE) {
                        visualizationPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                        hoveredVehicle = vehicle;
                        break;
                    } else {
                        visualizationPanel.setCursor(Cursor.getDefaultCursor());
                    }
                }

                if (hoveredVehicle != VisualizeForm.this.hoveredVehicle) {
                    VisualizeForm.this.hoveredVehicle = hoveredVehicle;
                    updateVehicleSizes();
                }
            }
        });
    }

    private Map<Vehicle, Point> getVehiclePositions(int width, int height) {
        Map<Vehicle, Point> vehiclePositions = new HashMap<>();
        for (Vehicle vehicle : vehicles) {
            int x = (int) ((vehicle.getCoordinates().getX() + 1000) * (width - VEHICLE_SIZE) / 2000);
            int y = (int) ((vehicle.getCoordinates().getY() + 1000) * (height - VEHICLE_SIZE) / 2000);
            vehiclePositions.put(vehicle, new Point(x, y));
        }

        // Отталкивание объектов
        for (int i = 0; i < 10; i++) { // Несколько итераций для лучшего раздвигания
            for (Vehicle v1 : vehicles) {
                for (Vehicle v2 : vehicles) {
                    if (v1 != v2) {
                        Point p1 = vehiclePositions.get(v1);
                        Point p2 = vehiclePositions.get(v2);

                        double dx = p1.x - p2.x;
                        double dy = p1.y - p2.y;
                        double distance = Math.sqrt(dx * dx + dy * dy);

                        if (distance < MIN_DISTANCE) {
                            double angle = Math.atan2(dy, dx);
                            int shiftX = (int) (Math.cos(angle) * (MIN_DISTANCE - distance) / 2);
                            int shiftY = (int) (Math.sin(angle) * (MIN_DISTANCE - distance) / 2);

                            p1.translate(shiftX, shiftY);
                            p2.translate(-shiftX, -shiftY);
                        }
                    }
                }
            }
        }
        return vehiclePositions;
    }

    private void updateVehicleSizes() {
        for (Vehicle vehicle : vehicles) {
            if (vehicle == hoveredVehicle) {
                vehicleSizes.put(vehicle, HOVERED_VEHICLE_SIZE);
            } else {
                vehicleSizes.put(vehicle, VEHICLE_SIZE);
            }
        }
        repaint();
    }

    private void deleteVehicle(Vehicle vehicle) {
        // Анимация удаления
        Timer timer = new Timer(10, null);
        timer.addActionListener(new ActionListener() {
            int size = VEHICLE_SIZE;

            @Override
            public void actionPerformed(ActionEvent e) {
                size -= 2;
                if (size <= 0) {
                    ((Timer) e.getSource()).stop();
                    performDelete(vehicle);
                } else {
                    vehicleSizes.put(vehicle, size);
                    repaint();
                }
            }
        });
        timer.start();
    }

    private void performDelete(Vehicle vehicle) {
        try {
            boolean success = clientEvents.removeById(String.valueOf(vehicle.getId()));
            if (success) {
                vehicles.remove(vehicle);
                vehicleSizes.remove(vehicle);
                mainForm.updateTable();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showVehicleInfo(Vehicle vehicle) {
        JDialog infoDialog = new JDialog(this, localizationManager.getString("vehInfo"), true);
        infoDialog.setSize(400, 450);
        infoDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        infoDialog.setLocationRelativeTo(null);
        infoDialog.setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 255, 240));
        panel.setLayout(new GridBagLayout());
        infoDialog.add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel idLabel = new JLabel("ID:");
        idLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(idLabel, gbc);

        JTextField idField = createRoundedTextField();
        idField.setText(String.valueOf(vehicle.getId()));
        idField.setEditable(false);
        gbc.gridx = 1;
        panel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        nameLabel = new JLabel(localizationManager.getString("name") + " :");
        nameLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(nameLabel, gbc);

        JTextField nameField = createRoundedTextField();
        nameField.setText(vehicle.getName());
        gbc.gridx = 1;
        panel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        coordXLabel = new JLabel(localizationManager.getString("coordX") + " :");
        coordXLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(coordXLabel, gbc);

        JTextField coordXField = createRoundedTextField();
        coordXField.setText(String.valueOf(vehicle.getCoordinates().getX()));
        gbc.gridx = 1;
        panel.add(coordXField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        coordYLabel = new JLabel(localizationManager.getString("coordY") + " :");
        coordYLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(coordYLabel, gbc);

        JTextField coordYField = createRoundedTextField();
        coordYField.setText(String.valueOf(vehicle.getCoordinates().getY()));
        gbc.gridx = 1;
        panel.add(coordYField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel creationDateLabel = new JLabel(localizationManager.getString("creationDate") + " :");
        creationDateLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(creationDateLabel, gbc);

        JTextField creationDateField = createRoundedTextField();
        creationDateField.setText(vehicle.getCreationDate().toString());
        creationDateField.setEditable(false);
        gbc.gridx = 1;
        panel.add(creationDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        enginePowerLabel = new JLabel(localizationManager.getString("enginePower") + " :");
        enginePowerLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(enginePowerLabel, gbc);

        JTextField enginePowerField = createRoundedTextField();
        enginePowerField.setText(String.valueOf(vehicle.getEnginePower()));
        gbc.gridx = 1;
        panel.add(enginePowerField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        vehicleTypeLabel = new JLabel(localizationManager.getString("vehicleType") + " :");
        vehicleTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(vehicleTypeLabel, gbc);

        JComboBox<VehicleType> vehicleTypeComboBox = createComboBox(VehicleType.values());
        vehicleTypeComboBox.setSelectedItem(vehicle.getType());
        gbc.gridx = 1;
        panel.add(vehicleTypeComboBox, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        fuelTypeLabel = new JLabel(localizationManager.getString("fuelType") + " :");
        fuelTypeLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(fuelTypeLabel, gbc);

        JComboBox<FuelType> fuelTypeComboBox = createComboBox(FuelType.values());
        fuelTypeComboBox.setSelectedItem(vehicle.getFuelType());
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

        saveButton.addActionListener(e -> {
            try {
                if (!Validator.inputNotEmpty(nameField.getText())) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("NameEMP"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Validator.xGood(Long.parseLong(coordXField.getText()))) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("digitone"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Validator.yGood(Integer.parseInt(coordYField.getText()))) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("digittwo"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!Validator.enginePowerGood(Integer.parseInt(enginePowerField.getText()))) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("enginepowerer"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                vehicle.setName(nameField.getText());
                vehicle.getCoordinates().setX(Long.parseLong(coordXField.getText()));
                vehicle.getCoordinates().setY(Integer.parseInt(coordYField.getText()));
                vehicle.setEnginePower(Integer.parseInt(enginePowerField.getText()));
                vehicle.setType((VehicleType) vehicleTypeComboBox.getSelectedItem());
                vehicle.setFuelType((FuelType) fuelTypeComboBox.getSelectedItem());
                clientEvents.updateVehicle(String.valueOf(vehicle.getId()), vehicle);
                mainForm.updateTable();
                infoDialog.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(infoDialog, localizationManager.getString("updateElFl"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> infoDialog.dispose());

        infoDialog.setVisible(true);
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
        button.setPreferredSize(new Dimension(90, 30));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
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
