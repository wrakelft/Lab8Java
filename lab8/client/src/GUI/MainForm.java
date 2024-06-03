package GUI;

import Collections.*;
import NetInteraction.ClientEvents;
import client.Client;
import managers.LocalizationManager;
import managers.Validator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class MainForm extends JFrame {
    private JTable vehicleTable;
    private ClientEvents clientEvents;
    private String username;
    private RoundedButton createButton;
    private RoundedButton editButton;
    private RoundedButton deleteButton;
    private RoundedButton visualizeButton;
    private RoundedButton commandsButton;
    private RoundedButton helpButton;
    private JLabel infoLabel;
    private JLabel matchesLabel;
    private JLabel nonMatchesLabel;
    private JLabel userLabel;
    private JLabel filterLabel;
    private Timer timer;
    private boolean isEditing = false;
    private boolean changesPending = false;
    private boolean isUpdating = false;
    private boolean editModeNotificationShown = false;
    private int[] selectedRowsBeforeUpdate;
    private VisualizeForm visualizeForm;

    private JComboBox<String> languageComboBox;

    private JComboBox<String> filterColumnComboBox;
    private JTextField filterTextField;
    private List<Vehicle> vehiclesCache = new ArrayList<>();

    private String currentFilterColumn = "";
    private String currentFilterValue = "";

    private String sortedColumn = "";
    private boolean sortAscending = true;

    private CreateVehicleForm createVehicleForm;
    private CommandForm commandForm;

    private LocalizationManager localizationManager;

    public MainForm(ClientEvents clientEvents, String username, LocalizationManager localizationManager) {
        this.clientEvents = clientEvents;
        this.username = username;
        this.localizationManager = new LocalizationManager(Locale.getDefault());

        setTitle(localizationManager.getString("vehicleTable"));
        setSize(900, 620);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(new Color(255, 255, 240));
        add(mainPanel);

        setupUserPanel(mainPanel);
        setupLanguagePanel(mainPanel); // Переместили вызов перед другими панелями
        setupInfoPanel(mainPanel);
        setupTable(mainPanel);
        setupFilterPanel(mainPanel);
        setupButtonPanel(mainPanel);

        vehicleTable.getModel().addTableModelListener(e -> {
            if (!isEditing) {
                changesPending = true;
            }
        });

        vehicleTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    if (vehicleTable.getSelectedRow() != -1) {
                        isEditing = true;
                        if (!editModeNotificationShown) {
                            JOptionPane.showMessageDialog(MainForm.this, localizationManager.getString("editingModeMessage"), localizationManager.getString("editingModeTitle"), JOptionPane.INFORMATION_MESSAGE);
                            editModeNotificationShown = true; // Показали уведомление
                        }
                    } else {
                        isEditing = false;
                        editModeNotificationShown = false; // Сбрасываем флаг при выходе из режима редактирования
                        updateTable();
                    }
                }
            }
        });

        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!vehicleTable.getBounds().contains(e.getPoint())) {
                    vehicleTable.clearSelection();
                }
            }
        });

        updateTable();
        startAutoUpdate();
    }

    private void setupUserPanel(JPanel mainPanel) {
        JPanel userPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        userPanel.setBackground(new Color(255, 255, 240));
        userLabel = new JLabel(localizationManager.getString("user") + " " + username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 18));
        ImageIcon userIcon = new ImageIcon("C:\\Users\\gleb\\IdeaProjects\\lab8\\client\\pictures\\verify.png");
        JLabel iconLabel = new JLabel(userIcon);
        userPanel.add(userLabel);
        userPanel.add(iconLabel);

        helpButton = createButton(localizationManager.getString("help"), new Dimension(100, 30), e -> showHelp());
        userPanel.add(helpButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;
        gbc.insets = new Insets(5, 10, 0, 10);
        mainPanel.add(userPanel, gbc);
    }

    private void setupLanguagePanel(JPanel mainPanel) {
        JPanel languagePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        languagePanel.setBackground(new Color(255, 255, 240));

        languageComboBox = createComboBox(new String[]{"English", "Русский", "Română", "Magyar", "Español"});
        languageComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedLanguage = (String) languageComboBox.getSelectedItem();
                Locale selectedLocale = Locale.ENGLISH;
                switch (selectedLanguage) {
                    case "Русский":
                        selectedLocale = new Locale("ru", "RU");
                        break;
                    case "Română":
                        selectedLocale = new Locale("ro", "RO");
                        break;
                    case "Magyar":
                        selectedLocale = new Locale("hu", "HU");
                        break;
                    case "Español":
                        selectedLocale = new Locale("es", "CO");
                        break;
                }
                updateLocale(selectedLocale);
                clientEvents.updateLocalizationManager(localizationManager);
            }
        });

        languagePanel.add(languageComboBox);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 1; // Установите gridx = 1 для размещения справа
        gbc.gridy = 0;
        gbc.weightx = 0.1; // Установите небольшое значение веса для выравнивания
        gbc.anchor = GridBagConstraints.NORTHEAST;
        gbc.insets = new Insets(5, 10, 0, 10);
        mainPanel.add(languagePanel, gbc);
    }

    private void setupInfoPanel(JPanel mainPanel) {
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBackground(new Color(255, 255, 240));
        infoLabel = new JLabel("0 " + localizationManager.getString("vehicles"));
        infoLabel.setFont(new Font("Arial", Font.BOLD, 18));
        infoLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK));
        infoPanel.add(infoLabel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 30, 0, 10);
        mainPanel.add(infoPanel, gbc);
    }

    private void setupTable(JPanel mainPanel) {
        String[] columnNames = {localizationManager.getString("id"), localizationManager.getString("name"), localizationManager.getString("coordX"), localizationManager.getString("coordY"), localizationManager.getString("creationDate"), localizationManager.getString("enginePower"), localizationManager.getString("vehicleType"), localizationManager.getString("fuelType")};
        DefaultTableModel model = new DefaultTableModel(null, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0 && column != 4;
            }
        };
        vehicleTable = new JTable(model);
        vehicleTable.setFillsViewportHeight(true);

        vehicleTable.setIntercellSpacing(new Dimension(1, 1));
        vehicleTable.setGridColor(new Color(200, 200, 200));

        JTableHeader tableHeader = vehicleTable.getTableHeader();
        tableHeader.setBackground(new Color(145, 187, 188));
        tableHeader.setForeground(Color.WHITE);
        tableHeader.setFont(new Font("Arial", Font.BOLD, 14));

        vehicleTable.setFont(new Font("Arial", Font.ITALIC, 13));
        vehicleTable.setRowHeight(25);
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer();
        cellRenderer.setHorizontalAlignment(JLabel.CENTER);
        vehicleTable.setDefaultRenderer(Object.class, cellRenderer);

        vehicleTable.setPreferredScrollableViewportSize(new Dimension(830, 400));
        JScrollPane scrollPane = new JScrollPane(vehicleTable);
        scrollPane.setPreferredSize(new Dimension(830, 370));
        scrollPane.setMaximumSize(new Dimension(830, 370));
        scrollPane.setMinimumSize(new Dimension(830, 370));

        JPanel roundedPanel = new JPanel(new BorderLayout());
        roundedPanel.add(scrollPane, BorderLayout.CENTER);
        roundedPanel.setBackground(new Color(245, 245, 220));

        tableHeader.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = vehicleTable.columnAtPoint(e.getPoint());
                String columnName = vehicleTable.getColumnName(col);
                if (columnName.equals(sortedColumn)) {
                    sortAscending = !sortAscending;
                } else {
                    sortedColumn = columnName;
                    sortAscending = true;
                }
                applyFilter();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(5, 20, 5, 20);
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        mainPanel.add(roundedPanel, gbc);
    }


    private void setupFilterPanel(JPanel mainPanel) {
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(new Color(255, 255, 240));

        filterLabel = new JLabel(localizationManager.getString("filterBy"));
        filterColumnComboBox = createComboBox(new String[]{localizationManager.getString("id"), localizationManager.getString("name"), localizationManager.getString("coordX"), localizationManager.getString("coordY"), localizationManager.getString("creationDate"), localizationManager.getString("enginePower"), localizationManager.getString("vehicleType"), localizationManager.getString("fuelType")});
        filterTextField = new RoundedTextField(15);

        matchesLabel = new JLabel(localizationManager.getString("matches") + ": 0");
        nonMatchesLabel = new JLabel(localizationManager.getString("nonMatches") + ": 0");

        filterPanel.add(filterLabel);
        filterPanel.add(filterColumnComboBox);
        filterPanel.add(filterTextField);
        filterPanel.add(matchesLabel);
        filterPanel.add(nonMatchesLabel);

        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyFilter();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyFilter();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 20, 5, 20);
        mainPanel.add(filterPanel, gbc);
    }


    private void setupButtonPanel(JPanel mainPanel) {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBackground(new Color(255, 255, 240));

        Dimension buttonSize = new Dimension(130, 30);

        createButton = createButton(localizationManager.getString("create"), buttonSize, e -> createVehicle());
        editButton = createButton(localizationManager.getString("edit"), buttonSize, e -> {
            if (changesPending) {
                applyChanges();
            } else {
                JOptionPane.showMessageDialog(MainForm.this, localizationManager.getString("updateElFl"), localizationManager.getString("info"), JOptionPane.INFORMATION_MESSAGE);
            }
        });
        deleteButton = createButton(localizationManager.getString("delete"), buttonSize, e -> deleteSelectedVehicles());
        visualizeButton = createButton(localizationManager.getString("visualize"), buttonSize, e -> visualizeVehicle());
        commandsButton = createButton(localizationManager.getString("commands"), buttonSize, e -> showCommands());

        buttonPanel.add(createButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(visualizeButton);
        buttonPanel.add(commandsButton);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weighty = 0.0;
        gbc.insets = new Insets(5, 5, 10, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(buttonPanel, gbc);
    }

    private RoundedButton createButton(String text, Dimension size, ActionListener actionListener) {
        RoundedButton button = new RoundedButton(text);
        button.setBackground(new Color(145, 187, 188));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(size);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.addActionListener(actionListener);
        return button;
    }

    private <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(145, 187, 188), 1));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        return comboBox;
    }

    private void createVehicle() {
        CreateVehicleForm createVehicleForm = new CreateVehicleForm(clientEvents, this);
        createVehicleForm.setVisible(true);
    }

    private void visualizeVehicle() {
        if (visualizeForm == null || !visualizeForm.isVisible()) {
            visualizeForm = new VisualizeForm(clientEvents, this);
            visualizeForm.setVisible(true);
        }
    }

    private void showCommands() {
        CommandForm commandForm = new CommandForm(clientEvents, this, localizationManager);
        commandForm.setVisible(true);
    }

    private void showHelp() {
        JOptionPane.showMessageDialog(this, localizationManager.getString("helpMessage"), localizationManager.getString("helpTitle"), JOptionPane.INFORMATION_MESSAGE);
    }

    public void updateTable() {
        if (isEditing || isUpdating) {
            return;
        }

        try {
            isUpdating = true;
            Set<Vehicle> vehicles = clientEvents.fetchVehicles();
            DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();

            boolean hasChanges = hasChanges(vehicles);

            if (hasChanges) {
                saveSelectionRows();

                model.setRowCount(0);
                vehiclesCache = new ArrayList<>(vehicles);
                applyFilter();  // Вызываем applyFilter здесь, чтобы применить фильтрацию после обновления данных
                String size = clientEvents.infoSize();
                infoLabel.setText(size + " " + localizationManager.getString("vehicles"));

                restoreOriginalSelection();

                // Обновление панели визуализации
                if (visualizeForm != null && visualizeForm.isVisible()) {
                    visualizeForm.updateVisualization(vehiclesCache);
                }
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            isUpdating = false;
        }
    }




    private void saveSelectionRows() {
        selectedRowsBeforeUpdate = vehicleTable.getSelectedRows();
    }

    private void restoreOriginalSelection() {
        if (selectedRowsBeforeUpdate != null) {
            ListSelectionModel selectionModel = vehicleTable.getSelectionModel();
            selectionModel.clearSelection();
            for (int row : selectedRowsBeforeUpdate) {
                if (row < vehicleTable.getRowCount()) {
                    selectionModel.addSelectionInterval(row, row);
                }
            }
        }
    }

    public List<Vehicle> getVehiclesCache() {
        return vehiclesCache;
    }

    private void applyChanges() {
        int[] selectedRows = vehicleTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("noVehiclesSelected"), localizationManager.getString("warning"), JOptionPane.WARNING_MESSAGE);
            return;
        } else if (selectedRows.length > 1) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("selectOneVehicle"), localizationManager.getString("warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmed = JOptionPane.showConfirmDialog(this,
                localizationManager.getString("updateConfirmation"),
                localizationManager.getString("confirmation"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            int selectedRow = selectedRows[0];
            DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();

            Long id = Long.parseLong(vehicleTable.getValueAt(selectedRow, 0).toString());
            String name = (String) model.getValueAt(selectedRow, 1);
            if (!Validator.inputNotEmpty(name)) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("nameEMP"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Date creationDate = (Date) model.getValueAt(selectedRow, 4);

            try {
                long coordX = Long.parseLong(model.getValueAt(selectedRow, 2).toString());
                if (!Validator.xGood(coordX)) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("digitone"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int coordY = Integer.parseInt(model.getValueAt(selectedRow, 3).toString());
                if (!Validator.yGood(coordY)) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("digittwo"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Integer enginePower = Integer.parseInt(model.getValueAt(selectedRow, 5).toString());
                if (!Validator.enginePowerGood(enginePower)) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("enginepowerer"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                Object vehicleTypeObject = model.getValueAt(selectedRow, 6);
                VehicleType vehicleType = (vehicleTypeObject instanceof VehicleType) ? (VehicleType) vehicleTypeObject : VehicleType.valueOf((String) vehicleTypeObject);
                if (!Validator.typeGood(vehicleType.toString())) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("vehicleTypeError"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Object fuelTypeObject = model.getValueAt(selectedRow, 7);
                FuelType fuelType = (fuelTypeObject instanceof FuelType) ? (FuelType) fuelTypeObject : FuelType.valueOf((String) fuelTypeObject);
                if (!Validator.fuelTypeGood(fuelType.toString())) {
                    JOptionPane.showMessageDialog(this, localizationManager.getString("fuelTypeError"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Vehicle newVehicle = new Vehicle(id, name, new Coordinates(coordX, coordY), creationDate, enginePower, vehicleType, fuelType, Client.getInstance().getId());

                try {
                    boolean success = clientEvents.updateVehicle(String.valueOf(id), newVehicle);
                    if (success) {
                        isEditing = false;
                        changesPending = false;
                        updateTable();
                    }
                } catch (IOException | ClassNotFoundException | InterruptedException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(this, localizationManager.getString("updateElFl"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
                } finally {
                    isEditing = false;
                    changesPending = false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("validNum"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException e) {
                JOptionPane.showMessageDialog(this, localizationManager.getString("typeError"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedVehicles() {
        int[] selectedRows = vehicleTable.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("noVehiclesSelected"), localizationManager.getString("warning"), JOptionPane.WARNING_MESSAGE);
            return;
        }
        int confirmed = JOptionPane.showConfirmDialog(this,
                localizationManager.getString("deleteConfirmation"),
                localizationManager.getString("confirmation"),
                JOptionPane.YES_NO_OPTION);

        if (confirmed == JOptionPane.YES_OPTION) {
            List<Long> ids = new ArrayList<>();
            for (int row : selectedRows) {
                ids.add(Long.parseLong(vehicleTable.getValueAt(row, 0).toString()));
            }
            try {
                if (clientEvents.deleteVehicles(ids)) {
                    isEditing = false; // Сбрасываем флаг при удалении
                    updateTable();
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, localizationManager.getString("deleteError"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean hasChanges(Set<Vehicle> newVehicles) {
        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        if (newVehicles.size() != model.getRowCount()) {
            return true;
        }
        int row = 0;
        for (Vehicle vehicle : newVehicles) {
            if (!Long.valueOf(vehicle.getId()).equals(model.getValueAt(row, 0)) ||
                    !vehicle.getName().equals(model.getValueAt(row, 1)) ||
                    !Long.valueOf(vehicle.getCoordinates().getX()).equals(model.getValueAt(row, 2)) ||
                    !Integer.valueOf(vehicle.getCoordinates().getY()).equals(model.getValueAt(row, 3)) ||
                    !vehicle.getCreationDate().equals(model.getValueAt(row, 4)) ||
                    !vehicle.getEnginePower().equals(model.getValueAt(row, 5)) ||
                    !vehicle.getType().equals(model.getValueAt(row, 6)) ||
                    !vehicle.getFuelType().equals(model.getValueAt(row, 7))) {
                return true;
            }
            row++;
        }
        return false;
    }

    public void updateLocale(Locale locale) {
        Locale.setDefault(locale);
        localizationManager.setLocale(locale);
        updateTexts();
        if (createVehicleForm != null) {
            createVehicleForm.updateTexts();
        }
        if (visualizeForm != null) {
            visualizeForm.updateTexts();
        }
        if (commandForm != null) {
            commandForm.updateTexts();
        }
    }

    private List<Vehicle> applyFilterToVehicles(List<Vehicle> vehicles) {
        List<Vehicle> filteredVehicles = vehicles.stream()
                .filter(vehicle -> {
                    if (currentFilterColumn.isEmpty() || currentFilterValue.isEmpty()) {
                        return true;
                    }
                    String value = getColumnValue(vehicle, currentFilterColumn);
                    return value.toLowerCase().contains(currentFilterValue.toLowerCase());
                })
                .sorted((v1, v2) -> {
                    if (sortedColumn.isEmpty()) {
                        return 0;
                    }
                    String value1 = getColumnValue(v1, sortedColumn);
                    String value2 = getColumnValue(v2, sortedColumn);
                    if (value1.isEmpty() || value2.isEmpty()) {
                        return 0;
                    }
                    try {
                        if (sortedColumn.equals("ID") || sortedColumn.equals("Coord X") || sortedColumn.equals("Coord Y") || sortedColumn.equals("Engine Power")) {
                            Long num1 = Long.valueOf(value1);
                            Long num2 = Long.valueOf(value2);
                            return sortAscending ? num1.compareTo(num2) : num2.compareTo(num1);
                        } else if (sortedColumn.equals("Creation Date")) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            Date date1 = sdf.parse(value1);
                            Date date2 = sdf.parse(value2);
                            return sortAscending ? date1.compareTo(date2) : date2.compareTo(date1);
                        } else {
                            return sortAscending ? value1.compareTo(value2) : value2.compareTo(value1);
                        }
                    } catch (NumberFormatException | ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                })
                .collect(Collectors.toList());

        long matchCount = filteredVehicles.size();
        long nonMatchCount = vehicles.size() - matchCount;

        matchesLabel.setText(localizationManager.getString("matches") + ": " + matchCount);
        nonMatchesLabel.setText(localizationManager.getString("nonMatches") + ": " + nonMatchCount);

        return filteredVehicles;
    }





    private String getColumnValue(Vehicle vehicle, String column) {
        switch (column) {
            case "ID":
            case "id":
                return String.valueOf(vehicle.getId());
            case "Name":
            case "name":
                return vehicle.getName();
            case "Coord X":
            case "coordX":
                return String.valueOf(vehicle.getCoordinates().getX());
            case "Coord Y":
            case "coordY":
                return String.valueOf(vehicle.getCoordinates().getY());
            case "Creation Date":
            case "creationDate":
                return new SimpleDateFormat("yyyy-MM-dd").format(vehicle.getCreationDate());
            case "Engine Power":
            case "enginePower":
                return String.valueOf(vehicle.getEnginePower());
            case "Vehicle Type":
            case "vehicleType":
                return vehicle.getType().toString();
            case "Fuel Type":
            case "fuelType":
                return vehicle.getFuelType().toString();
            default:
                return "";
        }
    }



    public void applyFilter() {
        if (vehiclesCache.isEmpty()) {
            return;
        }
        currentFilterColumn = filterColumnComboBox.getSelectedItem().toString();
        currentFilterValue = filterTextField.getText();
        System.out.println("Applying filter: " + currentFilterColumn + " = " + currentFilterValue);

        List<Vehicle> filteredVehicles = applyFilterToVehicles(vehiclesCache);

        DefaultTableModel model = (DefaultTableModel) vehicleTable.getModel();
        model.setRowCount(0);
        filteredVehicles.forEach(vehicle -> {
            Object[] rowData = {
                    vehicle.getId(),
                    vehicle.getName(),
                    vehicle.getCoordinates().getX(),
                    vehicle.getCoordinates().getY(),
                    vehicle.getCreationDate(),
                    vehicle.getEnginePower(),
                    vehicle.getType(),
                    vehicle.getFuelType()
            };
            model.addRow(rowData);
        });
    }




    private void startAutoUpdate() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTable();
            }
        }, 0, 5000);
    }

    private void updateTexts() {
        createButton.setText(localizationManager.getString("create"));
        editButton.setText(localizationManager.getString("edit"));
        deleteButton.setText(localizationManager.getString("delete"));
        visualizeButton.setText(localizationManager.getString("visualize"));
        commandsButton.setText(localizationManager.getString("commands"));
        infoLabel.setText(vehiclesCache.size() + " " + localizationManager.getString("vehicles"));
        matchesLabel.setText(localizationManager.getString("matches") + ": 0");
        nonMatchesLabel.setText(localizationManager.getString("nonMatches") + ": 0");
        filterLabel.setText(localizationManager.getString("filterBy"));

        setTitle(localizationManager.getString("vehicleTable"));
        userLabel.setText(localizationManager.getString("user") + ": " + username);
        helpButton.setText(localizationManager.getString("help"));
        infoLabel.setText(vehicleTable.getRowCount() + " " + localizationManager.getString("vehicles"));

        // Обновление заголовков таблицы
        vehicleTable.getColumnModel().getColumn(0).setHeaderValue(localizationManager.getString("id"));
        vehicleTable.getColumnModel().getColumn(1).setHeaderValue(localizationManager.getString("name"));
        vehicleTable.getColumnModel().getColumn(2).setHeaderValue(localizationManager.getString("coordX"));
        vehicleTable.getColumnModel().getColumn(3).setHeaderValue(localizationManager.getString("coordY"));
        vehicleTable.getColumnModel().getColumn(4).setHeaderValue(localizationManager.getString("creationDate"));
        vehicleTable.getColumnModel().getColumn(5).setHeaderValue(localizationManager.getString("enginePower"));
        vehicleTable.getColumnModel().getColumn(6).setHeaderValue(localizationManager.getString("vehicleType"));
        vehicleTable.getColumnModel().getColumn(7).setHeaderValue(localizationManager.getString("fuelType"));
        vehicleTable.getTableHeader().repaint();

        updateFilterColumnComboBox();  // Вызовите обновление заголовков фильтра
    }


    private void changeLocale(Locale locale) {
        Locale.setDefault(locale);
        localizationManager.setLocale(locale);
        updateTexts();
    }

    private void updateFilterColumnComboBox() {
        filterColumnComboBox.removeAllItems();
        filterColumnComboBox.addItem(localizationManager.getString("id"));
        filterColumnComboBox.addItem(localizationManager.getString("name"));
        filterColumnComboBox.addItem(localizationManager.getString("coordX"));
        filterColumnComboBox.addItem(localizationManager.getString("coordY"));
        filterColumnComboBox.addItem(localizationManager.getString("creationDate"));
        filterColumnComboBox.addItem(localizationManager.getString("enginePower"));
        filterColumnComboBox.addItem(localizationManager.getString("vehicleType"));
        filterColumnComboBox.addItem(localizationManager.getString("fuelType"));
    }

    public LocalizationManager getLocalizationManager() {
        return localizationManager;
    }
}