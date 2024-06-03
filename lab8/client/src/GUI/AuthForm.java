package GUI;

import managers.LocalizationManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

public class AuthForm extends JFrame {
    private RoundedTextField usernameField;
    private RoundedPasswordField passwordField;
    private RoundedButton loginButton;
    private RoundedButton registerButton;
    private JComboBox<String> languageComboBox;
    private LocalizationManager localizationManager;
    private JLabel welocomeLable;
    private JLabel usernameLabel;
    private JLabel passwordLabel;

    public AuthForm(LocalizationManager localizationManager) {
        this.localizationManager = localizationManager;
        setTitle(localizationManager.getString("loginTitle"));
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBackground(new Color(255,255,240));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6,10,8,10);

        welocomeLable = new JLabel(localizationManager.getString("welcomeMes"));
        welocomeLable.setFont(new Font("Arial", Font.BOLD, 28));
        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(welocomeLable, gbc);

        usernameLabel = new JLabel(localizationManager.getString("usernameLable"));
        gbc.gridx = 0;
        gbc.gridy = 1;
        leftPanel.add(usernameLabel, gbc);

        usernameField = new RoundedTextField(15);
        usernameField.setPreferredSize(new Dimension(200, 20));
        gbc.gridx = 0;
        gbc.gridy = 2;
        leftPanel.add(usernameField, gbc);

        passwordLabel = new JLabel(localizationManager.getString("passwordLable"));
        gbc.gridx = 0;
        gbc.gridy = 3;
        leftPanel.add(passwordLabel, gbc);

        passwordField = new RoundedPasswordField(15);
        passwordField.setPreferredSize(new Dimension(200, 20));
        gbc.gridx = 0;
        gbc.gridy = 4;
        leftPanel.add(passwordField, gbc);

        loginButton = new RoundedButton(localizationManager.getString("loginButton"));
        loginButton.setPreferredSize(new Dimension(200, 30));
        loginButton.setBackground(new Color(145, 187,188));
        loginButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        leftPanel.add(loginButton, gbc);

        registerButton = new RoundedButton(localizationManager.getString("regButton"));
        registerButton.setPreferredSize(new Dimension(200, 30));
        registerButton.setBackground(new Color(145, 187, 188));
        registerButton.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 6;
        leftPanel.add(registerButton, gbc);

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
            }
        });

        languagePanel.add(languageComboBox);

        gbc.gridx = 0;
        gbc.gridy = 10;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        leftPanel.add(languagePanel, gbc);


        JLabel rightPanel = new JLabel(new ImageIcon("C:\\Users\\gleb\\IdeaProjects\\lab8\\client\\pictures\\back.jpg"));
        rightPanel.setPreferredSize(new Dimension(280, 280));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(320);
        splitPane.setResizeWeight(0.5);
        splitPane.setEnabled(false);
        splitPane.setDividerSize(0);

        getContentPane().add(splitPane);

        customizeComponents();



    }

    private void customizeComponents() {
        UIManager.put("Button.font", new Font("Arial", Font.BOLD, 12));
        SwingUtilities.updateComponentTreeUI(loginButton);
        SwingUtilities.updateComponentTreeUI(registerButton);
    }

    private <E> JComboBox<E> createComboBox(E[] items) {
        JComboBox<E> comboBox = new JComboBox<>(items);
        comboBox.setFont(new Font("Arial", Font.PLAIN, 14));
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(145, 187, 188), 1));
        comboBox.setBackground(Color.WHITE);
        comboBox.setForeground(Color.BLACK);
        return comboBox;
    }

    private void updateLocale(Locale locale) {
        Locale.setDefault(locale);
        localizationManager.setLocale(locale);
        updateTexts();
    }

    private void updateTexts() {
        setTitle(localizationManager.getString("loginTitle"));
        loginButton.setText(localizationManager.getString("loginButton"));
        registerButton.setText(localizationManager.getString("regButton"));
        welocomeLable.setText(localizationManager.getString("welcomeMes"));
        usernameLabel.setText(localizationManager.getString("usernameLable"));
        passwordLabel.setText(localizationManager.getString("passwordLable"));
    }

    public String getUsername() {
        return usernameField.getText();
    }

    public char[] getPassword() {
        return passwordField.getPassword();
    }

    public void addLoginListener(ActionListener listener) {
        loginButton.addActionListener(listener);
    }

    public void addRegisterListener(ActionListener listener) {
        registerButton.addActionListener(listener);
    }


}
