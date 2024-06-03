package GUI;

import NetInteraction.ClientEvents;
import managers.ExecuteScriptCommand;
import managers.LocalizationManager;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Deque;

public class CommandForm extends JFrame {
    private JTextField idField;
    private JTextField idLowerField;
    private JButton removeByIdButton;
    private JButton clearButton;
    private JButton historyButton;
    private JButton addIfMaxButton;
    private JButton removeLowerButton;
    private JButton executeScriptButton;
    private ClientEvents clientEvents;
    private MainForm mainForm;
    private LocalizationManager localizationManager;

    public CommandForm(ClientEvents clientEvents, MainForm mainForm, LocalizationManager localizationManager) {
        this.clientEvents = clientEvents;
        this.mainForm = mainForm;
        this.localizationManager = localizationManager;

        setTitle(localizationManager.getString("commands"));
        setSize(500, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(255, 255, 240));
        panel.setLayout(new GridBagLayout());
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel titleLabel = new JLabel(localizationManager.getString("commands"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);

        clearButton = createButton(localizationManager.getString("clearCollection"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        panel.add(clearButton, gbc);

        idField = createRoundedTextField();
        removeByIdButton = createButton(localizationManager.getString("removeByIdButton"));
        JPanel removeByIdPanel = new JPanel(new BorderLayout(10, 0));
        removeByIdPanel.setBackground(new Color(255, 255, 240));
        removeByIdPanel.add(removeByIdButton, BorderLayout.WEST);
        removeByIdPanel.add(idField, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(removeByIdPanel, gbc);

        historyButton = createButton(localizationManager.getString("historyButton"));
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        panel.add(historyButton, gbc);

        idLowerField = createRoundedTextField();
        removeLowerButton = createButton(localizationManager.getString("removeLowerButton"));
        JPanel removeLowerPanel = new JPanel(new BorderLayout(10, 0));
        removeLowerPanel.setBackground(new Color(255, 255, 240));
        removeLowerPanel.add(removeLowerButton, BorderLayout.WEST);
        removeLowerPanel.add(idLowerField, BorderLayout.CENTER);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        panel.add(removeLowerPanel, gbc);

        addIfMaxButton = createButton(localizationManager.getString("addIfMaxButton"));
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        panel.add(addIfMaxButton, gbc);

        executeScriptButton = createButton(localizationManager.getString("executeScriptButton"));
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        panel.add(executeScriptButton, gbc);

        clearButton.addActionListener(e -> clearCollection());
        removeByIdButton.addActionListener(e -> removeById());
        historyButton.addActionListener(e -> showHistory());
        removeLowerButton.addActionListener(e -> removeLower());
        addIfMaxButton.addActionListener(e -> addIfMax());
        executeScriptButton.addActionListener(e -> executeScript());
    }

    private RoundedButton createButton(String text) {
        RoundedButton button = new RoundedButton(text);
        button.setBackground(new Color(145, 187, 188));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(200, 30));
        button.setFont(new Font("Arial", Font.BOLD, 14));
        return button;
    }

    private JTextField createRoundedTextField() {
        RoundedTextField textField = new RoundedTextField(15);
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        textField.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textField.setBackground(new Color(255, 255, 255));
        return textField;
    }

    private void clearCollection() {
        try {
            boolean success = clientEvents.clear();
            if (success) {
                mainForm.updateTable();
            } else {
                JOptionPane.showMessageDialog(this, localizationManager.getString("failedClearing"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removeById() {
        try {
            if (!idField.getText().isEmpty()) {
                boolean success = clientEvents.removeById(idField.getText());
                if (success) {
                    mainForm.updateTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, localizationManager.getString("idFieldEmpty"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("validNum"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showHistory() {
        JDialog historyDialog = new JDialog(this, localizationManager.getString("commandsHistory"), true);
        historyDialog.setSize(400, 300);
        historyDialog.setLayout(new BorderLayout());
        historyDialog.setLocationRelativeTo(this);

        JTextArea historyTextArea = new JTextArea();
        historyTextArea.setEditable(false);
        historyTextArea.setFont(new Font("Arial", Font.PLAIN, 14));

        JScrollPane scrollPane = new JScrollPane(historyTextArea);
        historyDialog.add(scrollPane, BorderLayout.CENTER);

        Deque<String> commandsHistory = clientEvents.getLastTwelveCommands();
        for (String command : commandsHistory) {
            historyTextArea.append(command + "\n");
        }

        RoundedButton okButton = new RoundedButton("OK");
        okButton.setBackground(new Color(145, 187, 188));
        okButton.setForeground(Color.WHITE);
        okButton.setPreferredSize(new Dimension(70, 30));
        okButton.setFont(new Font("Arial", Font.BOLD, 14));
        okButton.addActionListener(e -> historyDialog.dispose());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(okButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);

        historyDialog.setVisible(true);
    }

    private void addIfMax() {
        AddIfMaxForm addIfMaxForm = new AddIfMaxForm(clientEvents, mainForm, localizationManager);
        addIfMaxForm.setVisible(true);
    }

    private void executeScript() {
        File file = chooseFile();
        if (file != null) {
            try {
                ExecuteScriptCommand.execute("executeScript " + file.getAbsolutePath(), clientEvents, localizationManager, mainForm);
                mainForm.updateTable();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void removeLower() {
        try {
            if (!idLowerField.getText().isEmpty()) {
                boolean success = clientEvents.removeLower(idLowerField.getText());
                if (success) {
                    mainForm.updateTable();
                }
            } else {
                JOptionPane.showMessageDialog(this, localizationManager.getString("idFieldEmpty"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException | ClassNotFoundException | InterruptedException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, localizationManager.getString("something"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, localizationManager.getString("validNum"), localizationManager.getString("error"), JOptionPane.ERROR_MESSAGE);
        }
    }



    private File chooseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(localizationManager.getString("chooseFile"));
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }
        return null;
    }

    public void updateTexts() {
        setTitle(localizationManager.getString("commands"));
        clearButton.setText(localizationManager.getString("clearCollection"));
        removeByIdButton.setText(localizationManager.getString("removeById"));
        historyButton.setText(localizationManager.getString("commandsHistory"));
        removeLowerButton.setText(localizationManager.getString("removeLower"));
        addIfMaxButton.setText(localizationManager.getString("addIfMax"));
        executeScriptButton.setText(localizationManager.getString("executeScript"));
    }
}
