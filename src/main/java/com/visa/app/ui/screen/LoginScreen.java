package com.visa.app.ui.screen;

import com.visa.app.DatabaseManager;
import com.visa.app.model.User;
import com.visa.app.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 *  SCREEN: LoginScreen (Authentication & Account Creation)
 * ============================================================
 */
public class LoginScreen extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel mainCardPanel;

    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;
    private JComboBox<String> loginRoleCombo;

    private JTextField regEmailField;
    private JPasswordField regPasswordField;
    private JPasswordField regConfirmPasswordField;

    public LoginScreen() {
        setTitle("Non-Immigrant VISA Application Portal - Authenticate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(Theme.PRIMARY_BLUE);
        headerPanel.setLayout(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        JLabel titleLabel = new JLabel("VISA PORTAL", JLabel.CENTER);
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.WHITE);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        JLabel subtitleLabel = new JLabel("Non-Immigrant Visa System", JLabel.CENTER);
        subtitleLabel.setFont(Theme.SMALL_FONT);
        subtitleLabel.setForeground(Theme.BORDER_COLOR);
        headerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        cardLayout = new CardLayout();
        mainCardPanel = new JPanel(cardLayout);
        mainCardPanel.setBackground(Theme.BACKGROUND);

        mainCardPanel.add(buildLoginPanel(), "LOGIN");
        mainCardPanel.add(buildRegisterPanel(), "REGISTER");

        setLayout(new BorderLayout());
        add(headerPanel, BorderLayout.NORTH);
        add(mainCardPanel, BorderLayout.CENTER);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(8, 0, 8, 0);

        gbc.gridy = 0;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.BOLD_FONT);
        emailLabel.setForeground(Theme.TEXT_DARK);
        panel.add(emailLabel, gbc);

        gbc.gridy = 1;
        loginEmailField = Theme.createTextField(20);
        panel.add(loginEmailField, gbc);

        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.BOLD_FONT);
        passLabel.setForeground(Theme.TEXT_DARK);
        panel.add(passLabel, gbc);

        gbc.gridy = 3;
        loginPasswordField = Theme.createPasswordField(20);
        panel.add(loginPasswordField, gbc);

        gbc.gridy = 4;
        JLabel roleLabel = new JLabel("Access Level");
        roleLabel.setFont(Theme.BOLD_FONT);
        roleLabel.setForeground(Theme.TEXT_DARK);
        panel.add(roleLabel, gbc);

        gbc.gridy = 5;
        loginRoleCombo = new JComboBox<>(new String[] { "Applicant", "Administrator" });
        loginRoleCombo.setFont(Theme.REGULAR_FONT);
        loginRoleCombo.setBackground(Theme.WHITE);
        loginRoleCombo.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        panel.add(loginRoleCombo, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 0, 8, 0);
        JButton loginBtn = Theme.createPrimaryButton("Login");
        panel.add(loginBtn, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 5, 0);
        JButton switchBtn = new JButton(
                "<html>Don't have an account? <font color='#2980b9'><b>Register</b></font></html>");
        styleLinkButton(switchBtn);
        panel.add(switchBtn, gbc);

        loginBtn.addActionListener(e -> handleLogin());
        switchBtn.addActionListener(e -> {
            clearFields();
            cardLayout.show(mainCardPanel, "REGISTER");
        });

        return panel;
    }

    private JPanel buildRegisterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(6, 0, 6, 0);

        gbc.gridy = 0;
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(Theme.BOLD_FONT);
        emailLabel.setForeground(Theme.TEXT_DARK);
        panel.add(emailLabel, gbc);

        gbc.gridy = 1;
        regEmailField = Theme.createTextField(20);
        panel.add(regEmailField, gbc);

        gbc.gridy = 2;
        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(Theme.BOLD_FONT);
        passLabel.setForeground(Theme.TEXT_DARK);
        panel.add(passLabel, gbc);

        gbc.gridy = 3;
        regPasswordField = Theme.createPasswordField(20);
        panel.add(regPasswordField, gbc);

        gbc.gridy = 4;
        JLabel confirmPassLabel = new JLabel("Confirm Password");
        confirmPassLabel.setFont(Theme.BOLD_FONT);
        confirmPassLabel.setForeground(Theme.TEXT_DARK);
        panel.add(confirmPassLabel, gbc);

        gbc.gridy = 5;
        regConfirmPasswordField = Theme.createPasswordField(20);
        panel.add(regConfirmPasswordField, gbc);

        gbc.gridy = 6;
        gbc.insets = new Insets(15, 0, 8, 0);
        JButton regBtn = Theme.createPrimaryButton("Sign Up");
        panel.add(regBtn, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(5, 0, 5, 0);
        JButton switchBtn = new JButton(
                "<html>Already have an account? <font color='#2980b9'><b>Login</b></font></html>");
        styleLinkButton(switchBtn);
        panel.add(switchBtn, gbc);

        regBtn.addActionListener(e -> handleRegister());
        switchBtn.addActionListener(e -> {
            clearFields();
            cardLayout.show(mainCardPanel, "LOGIN");
        });

        return panel;
    }

    private void styleLinkButton(JButton btn) {
        btn.setFont(Theme.REGULAR_FONT);
        btn.setForeground(Theme.PRIMARY_BLUE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void clearFields() {
        loginEmailField.setText("");
        loginPasswordField.setText("");
        regEmailField.setText("");
        regPasswordField.setText("");
        regConfirmPasswordField.setText("");
    }

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword()).trim();
        String selectedRole = ((String) loginRoleCombo.getSelectedItem()).toUpperCase();

        if ("ADMINISTRATOR".equals(selectedRole)) {
            selectedRole = "ADMIN";
        }

        if (email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both Email and Password.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = DatabaseManager.getInstance().loginUser(email, password);

        if (user != null) {
            if (user.getRole().equalsIgnoreCase(selectedRole)) {
                this.dispose();
                SwingUtilities.invokeLater(() -> {
                    MainFrame mainFrame = new MainFrame(user);
                    mainFrame.setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials for the selected Access Level.",
                        "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Invalid email or password.", "Login Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleRegister() {
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword()).trim();
        String confirmPassword = new String(regConfirmPasswordField.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (password.length() < 4) {
            JOptionPane.showMessageDialog(this, "Password must be at least 4 characters.", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = DatabaseManager.getInstance().registerUser(email, password, "APPLICANT");

        if (success) {
            JOptionPane.showMessageDialog(this, "Registration Successful! You can now log in.", "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            cardLayout.show(mainCardPanel, "LOGIN");
        } else {
            JOptionPane.showMessageDialog(this, "Email is already registered.", "Registration Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
