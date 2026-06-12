package com.visa.app;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import com.visa.app.utils.BackendBridge;

// Dummy public class to represent the file
public class VisaUI {
    public static final String VERSION = "1.0.0";
}

// ==========================================
// 1. THEME STYLING TOKENS & HELPERS
// ==========================================
class Theme {
    // Colors
    public static final Color PRIMARY_BLUE = new Color(15, 76, 129); // Classic Deep Blue
    public static final Color ACCENT_BLUE = new Color(41, 128, 185); // Vibrant Sky Blue
    public static final Color BACKGROUND = new Color(245, 247, 250); // Soft Off-white
    public static final Color WHITE = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(210, 220, 235);
    public static final Color TEXT_DARK = new Color(45, 55, 72);
    public static final Color TEXT_LIGHT = Color.WHITE;
    public static final Color TEXT_MUTED = new Color(113, 128, 150);

    // Status Colors
    public static final Color STATUS_APPROVED = new Color(46, 204, 113); // Emerald Green
    public static final Color STATUS_PENDING = new Color(241, 196, 15); // Yellow
    public static final Color STATUS_DENIED = new Color(231, 76, 60); // Alizarin Red

    // Fonts
    public static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 11);

    public static JButton createPrimaryButton(String text) {
        return createButton(text, PRIMARY_BLUE, WHITE);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, WHITE, PRIMARY_BLUE);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, STATUS_DENIED, WHITE);
    }

    public static JButton createButton(String text, Color bgColor, Color fgColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color paintColor = bgColor;
                if (getModel().isPressed()) {
                    paintColor = bgColor.darker();
                } else if (getModel().isRollover()) {
                    if (bgColor == WHITE) {
                        paintColor = new Color(240, 244, 250);
                    } else {
                        paintColor = bgColor.brighter();
                    }
                }

                g2.setColor(paintColor);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        button.setFont(BOLD_FONT);
        button.setForeground(fgColor);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setRolloverEnabled(true);

        if (bgColor == WHITE) {
            button.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(PRIMARY_BLUE, 1),
                    BorderFactory.createEmptyBorder(8, 16, 8, 16)));
        } else {
            button.setBorder(BorderFactory.createEmptyBorder(9, 17, 9, 17));
        }

        return button;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        return panel;
    }

    public static void setComponentSizes(JComponent comp, int width, int height) {
        Dimension d = new Dimension(width, height);
        comp.setPreferredSize(d);
        comp.setMinimumSize(d);
        comp.setMaximumSize(d);
    }

    public static JTextField createTextField(int columns) {
        JTextField textField = new JTextField(columns);
        styleInputComponent(textField);
        return textField;
    }

    public static JPasswordField createPasswordField(int columns) {
        JPasswordField passwordField = new JPasswordField(columns);
        styleInputComponent(passwordField);
        return passwordField;
    }

    private static void styleInputComponent(JComponent comp) {
        comp.setFont(REGULAR_FONT);
        comp.setForeground(TEXT_DARK);
        comp.setBackground(WHITE);
        comp.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    }

    public static void showDetailsDialog(JFrame parentFrame, VisaApplication app) {
        JDialog dialog = new JDialog(parentFrame, "Visa Application #" + app.getId() + " Details", true);
        dialog.setSize(500, 600);
        dialog.setLocationRelativeTo(parentFrame);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        mainPanel.setBackground(Theme.WHITE);

        JPanel hPanel = new JPanel(new BorderLayout());
        hPanel.setBackground(Theme.PRIMARY_BLUE);
        hPanel.setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
        JLabel titleLabel = new JLabel("DETAILS: " + app.getFullName().toUpperCase());
        titleLabel.setFont(Theme.HEADER_FONT);
        titleLabel.setForeground(Theme.WHITE);

        JLabel statusLabel = new JLabel("Status: " + app.getStatus());
        statusLabel.setFont(Theme.BOLD_FONT);
        if ("APPROVED".equalsIgnoreCase(app.getStatus())) {
            statusLabel.setForeground(Theme.STATUS_APPROVED);
        } else if ("PENDING".equalsIgnoreCase(app.getStatus())) {
            statusLabel.setForeground(Theme.STATUS_PENDING);
        } else {
            statusLabel.setForeground(Theme.STATUS_DENIED);
        }

        hPanel.add(titleLabel, BorderLayout.WEST);
        hPanel.add(statusLabel, BorderLayout.EAST);
        mainPanel.add(hPanel, BorderLayout.NORTH);

        StringBuilder html = new StringBuilder(
                "<html><body style='font-family: Segoe UI, sans-serif; font-size: 11px; margin: 10px;'>");

        html.append(
                "<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>1. Personal Information</h3>");
        html.append("<b>Sex:</b> ").append(app.getSex()).append("<br>");
        html.append("<b>Citizenship:</b> ").append(app.getCitizenship()).append("<br>");
        html.append("<b>Civil Status:</b> ").append(app.getCivilStatus()).append("<br>");
        html.append("<b>Birth Date:</b> ").append(app.getBirthDate()).append("<br>");
        html.append("<b>Place of Birth:</b> ").append(app.getPlaceOfBirth()).append("<br>");
        html.append("<b>Email:</b> ").append(app.getEmail()).append("<br>");
        html.append("<b>Contact:</b> ").append(app.getContactNumber()).append("<br>");
        html.append("<b>Home Address:</b> ").append(app.getHomeAddress()).append("<br>");

        html.append(
                "<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>2. Application Details</h3>");
        html.append("<b>Requested Entry Type:</b> ").append(app.getEntryType()).append("<br>");
        html.append("<b>Length of Stay:</b> ").append(app.getLengthOfStay()).append(" days<br>");
        html.append("<b>Port of Entry:</b> ").append(app.getPortOfEntry()).append("<br>");
        html.append("<b>Destination After Philippines:</b> ").append(app.getDestinationAfter()).append("<br>");
        html.append("<b>Age Upon Application:</b> ").append(app.getAgeUponApp()).append("<br>");
        html.append("<b>Date of Application:</b> ").append(app.getDateOfApp()).append("<br>");
        html.append("<b>Purpose Type:</b> ").append(app.getPurposeType()).append("<br>");
        html.append("<b>Sponsor Name:</b> ").append(app.getSponsorName().isEmpty() ? "N/A" : app.getSponsorName())
                .append("<br>");
        html.append("<b>Sponsor Contact Number:</b> ")
                .append(app.getSponsorContact().isEmpty() ? "N/A" : app.getSponsorContact()).append("<br>");

        html.append(
                "<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>3. Family Information</h3>");
        html.append("<b>Father's Name:</b> ").append(app.getFatherName().isEmpty() ? "N/A" : app.getFatherName())
                .append("<br>");
        html.append("<b>Mother's Name:</b> ").append(app.getMotherName().isEmpty() ? "N/A" : app.getMotherName())
                .append("<br>");
        html.append("<b>Spouse's Name:</b> ").append(app.getSpouseName().isEmpty() ? "N/A" : app.getSpouseName())
                .append("<br>");
        html.append("<b>With Children:</b> ").append(app.isWithChildren() ? "Yes" : "No").append("<br>");

        if (app.isWithChildren() && app.getChildren() != null && !app.getChildren().isEmpty()) {
            html.append("<b>Children Profiles:</b><ul style='margin-top: 3px;'>");
            for (Child c : app.getChildren()) {
                html.append("<li>").append(c.getName()).append(" (Age: ").append(c.getAge()).append(")</li>");
            }
            html.append("</ul>");
        }

        html.append(
                "<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>4. Employment Information</h3>");
        html.append("<b>Occupation:</b> ").append(app.getOccupation().isEmpty() ? "N/A" : app.getOccupation())
                .append("<br>");
        html.append("<b>Employer/Address:</b> ")
                .append(app.getEmployerAddress().isEmpty() ? "N/A" : app.getEmployerAddress()).append("<br>");

        html.append(
                "<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>5. Attached Travel Documents</h3>");
        if (app.getDocuments() != null && !app.getDocuments().isEmpty()) {
            html.append(
                    "<table border='1' style='border-collapse: collapse; font-size: 10px; width: 100%; text-align: left;'>");
            html.append(
                    "<tr style='background-color: #f5f7fa;'><th>Type</th><th>Number</th><th>Authority</th><th>Dates</th></tr>");
            for (Document d : app.getDocuments()) {
                html.append("<tr>");
                html.append("<td>").append(d.getDocumentType()).append("</td>");
                html.append("<td>").append(d.getPassportNumber().isEmpty() ? "N/A" : d.getPassportNumber())
                        .append("</td>");
                html.append("<td>").append(d.getIssuingAuthority().isEmpty() ? "N/A" : d.getIssuingAuthority())
                        .append("</td>");
                if ("Original Passport".equalsIgnoreCase(d.getDocumentType())) {
                    html.append("<td>").append(d.getDateIssued()).append(" - ").append(d.getValidityDate())
                            .append("</td>");
                } else {
                    html.append("<td>N/A</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
        } else {
            html.append("No travel documents attached.");
        }

        html.append("</body></html>");

        JLabel contentLabel = new JLabel(html.toString());
        JScrollPane scroll = new JScrollPane(contentLabel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(10);
        mainPanel.add(scroll, BorderLayout.CENTER);

        JButton closeBtn = Theme.createPrimaryButton("Close");
        closeBtn.addActionListener(e -> dialog.dispose());
        mainPanel.add(closeBtn, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.setVisible(true);
    }

    public static boolean isValidDateString(String dateStr) {
        if (dateStr == null || !dateStr.matches("\\d{4}/\\d{2}/\\d{2}")) {
            return false;
        }
        String[] parts = dateStr.split("/");
        try {
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            if (month < 1 || month > 12) {
                return false;
            }

            int maxDays = 31;
            if (month == 4 || month == 6 || month == 9 || month == 11) {
                maxDays = 30;
            } else if (month == 2) {
                if ((year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)) {
                    maxDays = 29;
                } else {
                    maxDays = 28;
                }
            }
            return day >= 1 && day <= maxDays;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void setupAutomaticDateField(JTextField textField) {
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            private boolean isDeleting = false;

            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                isDeleting = (e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE
                        || e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE);
            }

            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                if (isDeleting)
                    return;

                String text = textField.getText();
                int caretPos = textField.getCaretPosition();
                String[] parts = text.split("/", -1);
                StringBuilder sb = new StringBuilder();

                if (parts.length > 0) {
                    String year = parts[0].replaceAll("\\D", "");
                    if (year.length() > 4) {
                        year = year.substring(0, 4);
                    }
                    sb.append(year);

                    if (year.length() == 4) {
                        sb.append("/");
                        if (parts.length > 1) {
                            String month = parts[1].replaceAll("\\D", "");
                            boolean typedSlashAfterMonth = (parts.length > 2);
                            if (month.length() == 1) {
                                char m = month.charAt(0);
                                if (typedSlashAfterMonth || (m >= '2' && m <= '9')) {
                                    month = "0" + month;
                                }
                            } else if (month.length() > 2) {
                                month = month.substring(0, 2);
                            }

                            sb.append(month);
                            if (month.length() == 2) {
                                sb.append("/");
                                if (parts.length > 2) {
                                    String day = parts[2].replaceAll("\\D", "");
                                    boolean typedSlashAfterDay = (parts.length > 3);
                                    if (day.length() == 1) {
                                        char d = day.charAt(0);
                                        if (typedSlashAfterDay || (d >= '4' && d <= '9')) {
                                            day = "0" + day;
                                        }
                                    } else if (day.length() > 2) {
                                        day = day.substring(0, 2);
                                    }
                                    sb.append(day);
                                }
                            }
                        }
                    }
                }

                String formatted = sb.toString();
                if (!formatted.equals(text)) {
                    textField.setText(formatted);
                    int newPos = Math.min(caretPos + (formatted.length() - text.length()), formatted.length());
                    if (newPos >= 0) {
                        textField.setCaretPosition(newPos);
                    }
                }
            }
        });

        textField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String text = textField.getText().trim();
                if (text.isEmpty())
                    return;
                String[] parts = text.split("/", -1);
                StringBuilder sb = new StringBuilder();
                if (parts.length > 0) {
                    String year = parts[0].replaceAll("\\D", "");
                    sb.append(year);
                    if (year.length() == 4) {
                        sb.append("/");
                        String month = parts.length > 1 ? parts[1].replaceAll("\\D", "") : "";
                        if (month.length() == 1) {
                            month = "0" + month;
                        }
                        sb.append(month);
                        if (!month.isEmpty()) {
                            sb.append("/");
                            String day = parts.length > 2 ? parts[2].replaceAll("\\D", "") : "";
                            if (day.length() == 1) {
                                day = "0" + day;
                            }
                            sb.append(day);
                        }
                    }
                }
                textField.setText(sb.toString());
            }
        });
    }
}

// ==========================================
// 2. LOGIN SCREEN FRAME
// ==========================================
class LoginScreen extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainCardPanel;

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

// ==========================================
// 3. MAIN FRAME WRAPPER CONTAINER
// ==========================================
class MainFrame extends JFrame {
    private User currentUser;
    private CardLayout cardLayout;
    private JPanel centerPanel;

    private MainMenuPanel mainMenuPanel;
    private HowToApplyPanel howToApplyPanel;
    private VisaApplicationWizard visaApplicationWizard;
    private ApplicantDashboardPanel applicantDashboardPanel;
    private AdminDashboardPanel adminDashboardPanel;

    private JButton navHomeBtn;
    private JButton navHowBtn;
    private JButton navApplyBtn;
    private JButton navDashBtn;
    private JButton navLogoutBtn;

    public MainFrame(User user) {
        this.currentUser = user;
        setTitle("Non-Immigrant VISA Application Portal - Logged in as: " + user.getEmail());
        setSize(900, 650);
        setMinimumSize(new Dimension(850, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());
        add(buildNavBar(), BorderLayout.NORTH);

        cardLayout = new CardLayout();
        centerPanel = new JPanel(cardLayout);
        centerPanel.setBackground(Theme.BACKGROUND);

        mainMenuPanel = new MainMenuPanel(this);
        howToApplyPanel = new HowToApplyPanel(this);
        visaApplicationWizard = new VisaApplicationWizard(this, currentUser.getId());

        centerPanel.add(mainMenuPanel, "HOME");
        centerPanel.add(howToApplyPanel, "HOW_TO_APPLY");
        centerPanel.add(visaApplicationWizard, "APPLY");

        if (currentUser.isAdmin()) {
            adminDashboardPanel = new AdminDashboardPanel(this);
            centerPanel.add(adminDashboardPanel, "DASHBOARD");
            navApplyBtn.setVisible(false);
        } else {
            applicantDashboardPanel = new ApplicantDashboardPanel(this, currentUser);
            centerPanel.add(applicantDashboardPanel, "DASHBOARD");
        }

        add(centerPanel, BorderLayout.CENTER);
        showPanel("HOME");
    }

    private JPanel buildNavBar() {
        JPanel navBar = new JPanel(new BorderLayout());
        navBar.setBackground(Theme.PRIMARY_BLUE);
        navBar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JLabel brandLabel = new JLabel("VISA PORTAL");
        brandLabel.setFont(Theme.TITLE_FONT);
        brandLabel.setForeground(Theme.WHITE);
        navBar.add(brandLabel, BorderLayout.WEST);

        JPanel menuContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        menuContainer.setOpaque(false);

        navHomeBtn = createNavButton("Home");
        navHowBtn = createNavButton("How to Apply");
        navApplyBtn = createNavButton("Apply for Visa");
        navDashBtn = createNavButton("Dashboard");
        navLogoutBtn = createNavButton("Logout");

        navLogoutBtn.setForeground(new Color(240, 100, 100));

        menuContainer.add(navHomeBtn);
        menuContainer.add(navHowBtn);
        menuContainer.add(navApplyBtn);
        menuContainer.add(navDashBtn);
        menuContainer.add(navLogoutBtn);

        navBar.add(menuContainer, BorderLayout.EAST);

        navHomeBtn.addActionListener(e -> showPanel("HOME"));
        navHowBtn.addActionListener(e -> showPanel("HOW_TO_APPLY"));
        navApplyBtn.addActionListener(e -> {
            visaApplicationWizard.resetForm();
            showPanel("APPLY");
        });
        navDashBtn.addActionListener(e -> {
            refreshDashboard();
            showPanel("DASHBOARD");
        });
        navLogoutBtn.addActionListener(e -> handleLogout());

        return navBar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(Theme.BOLD_FONT);
        btn.setForeground(Theme.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setForeground(Theme.BORDER_COLOR);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (text.equals("Logout")) {
                    btn.setForeground(new Color(240, 100, 100));
                } else {
                    btn.setForeground(Theme.WHITE);
                }
            }
        });
        return btn;
    }

    public void showPanel(String name) {
        cardLayout.show(centerPanel, name);
    }

    public void editApplication(VisaApplication app) {
        visaApplicationWizard.loadApplicationForEditing(app);
        showPanel("APPLY");
    }

    public void refreshDashboard() {
        if (currentUser.isAdmin()) {
            if (adminDashboardPanel != null) {
                adminDashboardPanel.refreshData();
            }
        } else {
            if (applicantDashboardPanel != null) {
                applicantDashboardPanel.refreshData();
            }
        }
    }

    private void handleLogout() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out?",
                "Logout Confirmation",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (choice == JOptionPane.YES_OPTION) {
            this.dispose();
            SwingUtilities.invokeLater(() -> {
                LoginScreen loginScreen = new LoginScreen();
                loginScreen.setVisible(true);
            });
        }
    }

    public User getCurrentUser() {
        return currentUser;
    }
}

// ==========================================
// 4. MAIN MENU PANEL (HOME LANDING SCREEN)
// ==========================================
class MainMenuPanel extends JPanel {
    private MainFrame mainFrame;

    public MainMenuPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.BACKGROUND);
        setLayout(new GridBagLayout());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.NONE;

        JPanel card = Theme.createCardPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        Theme.setComponentSizes(card, 550, 360);

        JPanel bar = new JPanel();
        bar.setBackground(Theme.ACCENT_BLUE);
        Theme.setComponentSizes(bar, 60, 5);
        bar.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel titleLabel = new JLabel("Non-Immigrant VISA Application Portal");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.PRIMARY_BLUE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String bodyText = "<html><body style='text-align: center; font-family: Segoe UI, sans-serif; font-size: 11px;'>"
                + "Welcome to the official Non-Immigrant Visa Application portal.<br><br>"
                + "Use this application to complete your visa forms, supply necessary family and "
                + "employment details, and upload travel documents.<br><br>"
                + "Track your application status (Pending, Approved, Denied) directly in your "
                + "personal Dashboard."
                + "</body></html>";
        JLabel bodyLabel = new JLabel(bodyText);
        bodyLabel.setFont(Theme.REGULAR_FONT);
        bodyLabel.setForeground(Theme.TEXT_DARK);
        bodyLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        bodyLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton actionBtn;
        if (mainFrame.getCurrentUser().isAdmin()) {
            actionBtn = Theme.createPrimaryButton("Go to Admin Dashboard");
            actionBtn.addActionListener(e -> {
                mainFrame.refreshDashboard();
                mainFrame.showPanel("DASHBOARD");
            });
        } else {
            actionBtn = Theme.createPrimaryButton("Apply for Visa");
            actionBtn.addActionListener(e -> {
                mainFrame.showPanel("APPLY");
            });
        }
        actionBtn.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(Box.createVerticalStrut(20));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(15));
        card.add(bar);
        card.add(Box.createVerticalStrut(30));
        card.add(bodyLabel);
        card.add(Box.createVerticalStrut(40));
        card.add(actionBtn);
        card.add(Box.createVerticalGlue());

        add(card, gbc);
    }
}

// ==========================================
// 5. HOW TO APPLY PANEL (GUIDE SCREEN)
// ==========================================
class HowToApplyPanel extends JPanel {
    private MainFrame mainFrame;

    public HowToApplyPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(Theme.PRIMARY_BLUE);
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("How to Apply for a Non-Immigrant Visa");
        titleLabel.setFont(Theme.TITLE_FONT);
        titleLabel.setForeground(Theme.WHITE);
        titlePanel.add(titleLabel, BorderLayout.WEST);

        JLabel infoLabel = new JLabel("Follow these simple steps to submit your application");
        infoLabel.setFont(Theme.REGULAR_FONT);
        infoLabel.setForeground(Theme.BORDER_COLOR);
        titlePanel.add(infoLabel, BorderLayout.SOUTH);

        add(titlePanel, BorderLayout.NORTH);

        JPanel contentScrollPanel = new JPanel(new GridBagLayout());
        contentScrollPanel.setBackground(Theme.BACKGROUND);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(15, 40, 15, 40);

        gbc.gridy = 0;
        contentScrollPanel.add(createStepCard("1", "Register/Login",
                "Create a personal applicant account using your email and password. Log in using your credentials to access your personal application space."),
                gbc);

        gbc.gridy = 1;
        contentScrollPanel.add(createStepCard("2", "Fill Personal Details",
                "Select 'Apply for Visa' from the top menu. Fill out page 1 of the application wizard containing all required personal information (Full Name, Address, Contact, Birth details)."),
                gbc);

        gbc.gridy = 2;
        contentScrollPanel.add(createStepCard("3", "Provide Additional Information (Optional)",
                "Fill out family profiles, spouse name, and add children's names and ages if applicable. Provide employment information including current occupation and employer office address."),
                gbc);

        gbc.gridy = 3;
        contentScrollPanel.add(createStepCard("4", "Attach Travel Documents (Required)",
                "Provide at least one travel document (e.g., Original Passport, Air Ticket, Bank Certificate, or Invitation Letter). Ensure passport details such as number and validity are entered correctly."),
                gbc);

        gbc.gridy = 4;
        contentScrollPanel.add(createStepCard("5", "Submit and Track Status",
                "Submit your application. Navigate to the 'Dashboard' submenu to review your personal details, edit/delete pending requests, or track whether the Administrator has Approved or Denied your file."),
                gbc);

        JScrollPane scrollPane = new JScrollPane(contentScrollPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);
        add(scrollPane, BorderLayout.CENTER);

        if (!mainFrame.getCurrentUser().isAdmin()) {
            JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            actionPanel.setBackground(Theme.WHITE);
            actionPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER_COLOR),
                    BorderFactory.createEmptyBorder(15, 0, 15, 0)));

            JButton startBtn = Theme.createPrimaryButton("Start Application Now");
            startBtn.addActionListener(e -> mainFrame.showPanel("APPLY"));
            actionPanel.add(startBtn);
            add(actionPanel, BorderLayout.SOUTH);
        }
    }

    private JPanel createStepCard(String number, String title, String description) {
        JPanel card = new JPanel(new BorderLayout(15, 0));
        card.setBackground(Theme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JPanel circlePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Theme.PRIMARY_BLUE);
                g2.fillOval(0, 0, 36, 36);
                g2.setColor(Theme.WHITE);
                g2.setFont(Theme.BOLD_FONT.deriveFont(16f));
                FontMetrics fm = g2.getFontMetrics();
                int x = (36 - fm.stringWidth(number)) / 2;
                int y = ((36 - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(number, x, y);
            }
        };
        circlePanel.setOpaque(false);
        Theme.setComponentSizes(circlePanel, 36, 36);
        card.add(circlePanel, BorderLayout.WEST);

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);

        JLabel stepTitle = new JLabel(title);
        stepTitle.setFont(Theme.SUBTITLE_FONT.deriveFont(16f));
        stepTitle.setForeground(Theme.PRIMARY_BLUE);
        textPanel.add(stepTitle);

        JLabel stepDesc = new JLabel("<html><body style='font-family: Segoe UI, sans-serif; font-size: 11px;'>"
                + description + "</body></html>");
        stepDesc.setFont(Theme.REGULAR_FONT);
        stepDesc.setForeground(Theme.TEXT_DARK);
        textPanel.add(stepDesc);

        card.add(textPanel, BorderLayout.CENTER);
        return card;
    }
}

// ==========================================
// 6. VISA APPLICATION FORM WIZARD
// ==========================================
class VisaApplicationWizard extends JPanel {
    private MainFrame mainFrame;
    private int currentUserId;
    private int editingAppId = -1;

    private CardLayout wizardCardLayout;
    private JPanel wizardCardPanel;
    private JLabel wizardTitleLabel;

    private JTextField fullNameField;
    private JComboBox<String> sexCombo;
    private JTextField citizenshipField;
    private JComboBox<String> civilStatusCombo;
    private JTextField birthDateField;
    private JTextField birthPlaceField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField addressField;

    private JTextField fatherField;
    private JTextField motherField;
    private JTextField spouseField;
    private JComboBox<String> withChildrenCombo;
    private JPanel childrenSection;
    private DefaultListModel<String> childrenListModel;
    private JList<String> childrenList;
    private List<Child> tempChildrenList;

    private JTextField occupationField;
    private JTextField employerField;

    private DefaultTableModel docTableModel;
    private JTable docTable;
    private List<Document> tempDocList;

    // Passport specific fields directly on Page 2
    private JTextField passportNumField;
    private JTextField passportAuthField;
    private JTextField passportIssuedField;
    private JTextField passportExpiryField;

    // Page 3: Application Details fields
    private JComboBox<String> entryTypeCombo;
    private JTextField lengthOfStayField;
    private JComboBox<String> portOfEntryCombo;
    private JTextField destinationAfterField;
    private JTextField dateOfAppField;
    private JComboBox<String> purposeTypeCombo;
    private JTextField sponsorNameField;
    private JTextField sponsorContactField;

    public VisaApplicationWizard(MainFrame mainFrame, int currentUserId) {
        this.mainFrame = mainFrame;
        this.currentUserId = currentUserId;
        this.tempChildrenList = new ArrayList<>();
        this.tempDocList = new ArrayList<>();

        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Theme.WHITE);
        headerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 30, 15, 30)));

        wizardTitleLabel = new JLabel("New Visa Application");
        wizardTitleLabel.setFont(Theme.TITLE_FONT);
        wizardTitleLabel.setForeground(Theme.PRIMARY_BLUE);
        headerPanel.add(wizardTitleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        wizardCardLayout = new CardLayout();
        wizardCardPanel = new JPanel(wizardCardLayout);
        wizardCardPanel.setBackground(Theme.BACKGROUND);

        wizardCardPanel.add(buildPage1Scroll(), "PAGE_1");
        wizardCardPanel.add(buildPage2Scroll(), "PAGE_2");
        wizardCardPanel.add(buildPage3Scroll(), "PAGE_3");

        add(wizardCardPanel, BorderLayout.CENTER);
    }

    private JScrollPane buildPage1Scroll() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridy = row++;
        JLabel sectionTitle = new JLabel("Personal Information (Required)");
        sectionTitle.setFont(Theme.SUBTITLE_FONT);
        sectionTitle.setForeground(Theme.PRIMARY_BLUE);
        panel.add(sectionTitle, gbc);

        gbc.gridy = row++;
        panel.add(new JSeparator(), gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("<html>Full Name (First Name, Middle Name, Last Name) <font color='red'>*</font></html>"),
                gbc);
        gbc.gridy = row++;
        fullNameField = Theme.createTextField(30);
        panel.add(fullNameField, gbc);

        JPanel colPanel1 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel1.setOpaque(false);

        JPanel pSex = new JPanel(new BorderLayout(0, 5));
        pSex.setOpaque(false);
        pSex.add(new JLabel("<html>Sex <font color='red'>*</font></html>"), BorderLayout.NORTH);
        sexCombo = new JComboBox<>(new String[] { "Male", "Female" });
        sexCombo.setFont(Theme.REGULAR_FONT);
        sexCombo.setBackground(Theme.WHITE);
        pSex.add(sexCombo, BorderLayout.CENTER);

        JPanel pStatus = new JPanel(new BorderLayout(0, 5));
        pStatus.setOpaque(false);
        pStatus.add(new JLabel("<html>Civil Status <font color='red'>*</font></html>"), BorderLayout.NORTH);
        civilStatusCombo = new JComboBox<>(new String[] { "Single", "Married", "Widowed", "Separated" });
        civilStatusCombo.setFont(Theme.REGULAR_FONT);
        civilStatusCombo.setBackground(Theme.WHITE);
        pStatus.add(civilStatusCombo, BorderLayout.CENTER);

        colPanel1.add(pSex);
        colPanel1.add(pStatus);

        gbc.gridy = row++;
        panel.add(colPanel1, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("<html>Citizenship <font color='red'>*</font></html>"), gbc);
        gbc.gridy = row++;
        citizenshipField = Theme.createTextField(30);
        panel.add(citizenshipField, gbc);

        JPanel colPanel2 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel2.setOpaque(false);

        JPanel pBirthDate = new JPanel(new BorderLayout(0, 5));
        pBirthDate.setOpaque(false);
        pBirthDate.add(new JLabel("<html>Birth Date (YYYY/MM/DD) <font color='red'>*</font></html>"),
                BorderLayout.NORTH);
        birthDateField = Theme.createTextField(15);
        Theme.setupAutomaticDateField(birthDateField);
        pBirthDate.add(birthDateField, BorderLayout.CENTER);

        JPanel pBirthPlace = new JPanel(new BorderLayout(0, 5));
        pBirthPlace.setOpaque(false);
        pBirthPlace.add(new JLabel("<html>Place of Birth <font color='red'>*</font></html>"), BorderLayout.NORTH);
        birthPlaceField = Theme.createTextField(15);
        pBirthPlace.add(birthPlaceField, BorderLayout.CENTER);

        colPanel2.add(pBirthDate);
        colPanel2.add(pBirthPlace);

        gbc.gridy = row++;
        panel.add(colPanel2, gbc);

        JPanel colPanel3 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel3.setOpaque(false);

        JPanel pEmail = new JPanel(new BorderLayout(0, 5));
        pEmail.setOpaque(false);
        pEmail.add(new JLabel("<html>Email Address <font color='red'>*</font></html>"), BorderLayout.NORTH);
        emailField = Theme.createTextField(15);
        pEmail.add(emailField, BorderLayout.CENTER);

        JPanel pContact = new JPanel(new BorderLayout(0, 5));
        pContact.setOpaque(false);
        pContact.add(new JLabel("<html>Contact Number <font color='red'>*</font></html>"), BorderLayout.NORTH);
        contactField = Theme.createTextField(15);
        pContact.add(contactField, BorderLayout.CENTER);

        colPanel3.add(pEmail);
        colPanel3.add(pContact);

        gbc.gridy = row++;
        panel.add(colPanel3, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("<html>Home Address <font color='red'>*</font></html>"), gbc);
        gbc.gridy = row++;
        addressField = Theme.createTextField(30);
        panel.add(addressField, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(30, 0, 10, 0);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);

        JButton nextBtn = Theme.createPrimaryButton("Next Page →");
        nextBtn.addActionListener(e -> handleNextPage());
        buttonPanel.add(nextBtn);
        panel.add(buttonPanel, gbc);

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    private JScrollPane buildPage2Scroll() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridy = row++;
        JLabel familyHeader = new JLabel("Family Information (Optional)");
        familyHeader.setFont(Theme.SUBTITLE_FONT);
        familyHeader.setForeground(Theme.PRIMARY_BLUE);
        panel.add(familyHeader, gbc);

        gbc.gridy = row++;
        panel.add(new JSeparator(), gbc);

        JPanel familyNamesPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        familyNamesPanel.setOpaque(false);

        JPanel pFather = new JPanel(new BorderLayout(0, 5));
        pFather.setOpaque(false);
        pFather.add(new JLabel("Father's Full Name"), BorderLayout.NORTH);
        fatherField = Theme.createTextField(10);
        pFather.add(fatherField, BorderLayout.CENTER);

        JPanel pMother = new JPanel(new BorderLayout(0, 5));
        pMother.setOpaque(false);
        pMother.add(new JLabel("Mother's Full Name"), BorderLayout.NORTH);
        motherField = Theme.createTextField(10);
        pMother.add(motherField, BorderLayout.CENTER);

        JPanel pSpouse = new JPanel(new BorderLayout(0, 5));
        pSpouse.setOpaque(false);
        pSpouse.add(new JLabel("Spouse's Full Name"), BorderLayout.NORTH);
        spouseField = Theme.createTextField(10);
        pSpouse.add(spouseField, BorderLayout.CENTER);

        familyNamesPanel.add(pFather);
        familyNamesPanel.add(pMother);
        familyNamesPanel.add(pSpouse);
        gbc.gridy = row++;
        panel.add(familyNamesPanel, gbc);

        JPanel withKidsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 5));
        withKidsPanel.setOpaque(false);
        withKidsPanel.add(new JLabel("With Children?  "));
        withChildrenCombo = new JComboBox<>(new String[] { "No", "Yes" });
        withChildrenCombo.setFont(Theme.REGULAR_FONT);
        withChildrenCombo.setBackground(Theme.WHITE);
        withKidsPanel.add(withChildrenCombo);
        gbc.gridy = row++;
        panel.add(withKidsPanel, gbc);

        childrenSection = new JPanel(new BorderLayout(10, 10));
        childrenSection.setBackground(Theme.BACKGROUND);
        childrenSection.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        childrenSection.setVisible(false);

        childrenListModel = new DefaultListModel<>();
        childrenList = new JList<>(childrenListModel);
        childrenList.setFont(Theme.REGULAR_FONT);
        JScrollPane kidsScroll = new JScrollPane(childrenList);
        kidsScroll.setPreferredSize(new Dimension(250, 80));

        JPanel kidsBtnPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        kidsBtnPanel.setOpaque(false);
        JButton addChildBtn = Theme.createPrimaryButton("Add Child");
        JButton removeChildBtn = Theme.createSecondaryButton("Remove Selected");
        kidsBtnPanel.add(addChildBtn);
        kidsBtnPanel.add(removeChildBtn);

        childrenSection.add(new JLabel("Children list:"), BorderLayout.NORTH);
        childrenSection.add(kidsScroll, BorderLayout.CENTER);
        childrenSection.add(kidsBtnPanel, BorderLayout.EAST);

        gbc.gridy = row++;
        panel.add(childrenSection, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(15, 0, 8, 0);
        JLabel empHeader = new JLabel("Employment Information (Optional)");
        empHeader.setFont(Theme.SUBTITLE_FONT);
        empHeader.setForeground(Theme.PRIMARY_BLUE);
        panel.add(empHeader, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(new JSeparator(), gbc);

        JPanel empPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        empPanel.setOpaque(false);

        JPanel pOcc = new JPanel(new BorderLayout(0, 5));
        pOcc.setOpaque(false);
        pOcc.add(new JLabel("Occupation"), BorderLayout.NORTH);
        occupationField = Theme.createTextField(15);
        pOcc.add(occupationField, BorderLayout.CENTER);

        JPanel pEmp = new JPanel(new BorderLayout(0, 5));
        pEmp.setOpaque(false);
        pEmp.add(new JLabel("Employer Office & Address"), BorderLayout.NORTH);
        employerField = Theme.createTextField(15);
        pEmp.add(employerField, BorderLayout.CENTER);

        empPanel.add(pOcc);
        empPanel.add(pEmp);
        gbc.gridy = row++;
        panel.add(empPanel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(15, 0, 8, 0);
        JLabel docHeader = new JLabel("Travel Documents (Passport Required)");
        docHeader.setFont(Theme.SUBTITLE_FONT);
        docHeader.setForeground(Theme.PRIMARY_BLUE);
        panel.add(docHeader, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(new JSeparator(), gbc);

        // Passport Details direct input
        JPanel passportPanel = new JPanel(new GridLayout(2, 2, 20, 10));
        passportPanel.setOpaque(false);

        JPanel pPassNum = new JPanel(new BorderLayout(0, 5));
        pPassNum.setOpaque(false);
        pPassNum.add(new JLabel("<html>Passport Number <font color='red'>*</font></html>"), BorderLayout.NORTH);
        passportNumField = Theme.createTextField(15);
        pPassNum.add(passportNumField, BorderLayout.CENTER);

        JPanel pPassAuth = new JPanel(new BorderLayout(0, 5));
        pPassAuth.setOpaque(false);
        pPassAuth.add(new JLabel("<html>Issuing Authority <font color='red'>*</font></html>"), BorderLayout.NORTH);
        passportAuthField = Theme.createTextField(15);
        pPassAuth.add(passportAuthField, BorderLayout.CENTER);

        JPanel pPassIssued = new JPanel(new BorderLayout(0, 5));
        pPassIssued.setOpaque(false);
        pPassIssued.add(new JLabel("<html>Date Issued (YYYY/MM/DD) <font color='red'>*</font></html>"),
                BorderLayout.NORTH);
        passportIssuedField = Theme.createTextField(15);
        Theme.setupAutomaticDateField(passportIssuedField);
        pPassIssued.add(passportIssuedField, BorderLayout.CENTER);

        JPanel pPassExpiry = new JPanel(new BorderLayout(0, 5));
        pPassExpiry.setOpaque(false);
        pPassExpiry.add(new JLabel("<html>Validity Date (YYYY/MM/DD) <font color='red'>*</font></html>"),
                BorderLayout.NORTH);
        passportExpiryField = Theme.createTextField(15);
        Theme.setupAutomaticDateField(passportExpiryField);
        pPassExpiry.add(passportExpiryField, BorderLayout.CENTER);

        passportPanel.add(pPassNum);
        passportPanel.add(pPassAuth);
        passportPanel.add(pPassIssued);
        passportPanel.add(pPassExpiry);

        gbc.gridy = row++;
        panel.add(passportPanel, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(15, 0, 8, 0);
        JLabel supportingDocHeader = new JLabel("Supporting Travel Documents (Optional)");
        supportingDocHeader.setFont(Theme.SUBTITLE_FONT);
        supportingDocHeader.setForeground(Theme.PRIMARY_BLUE);
        panel.add(supportingDocHeader, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(8, 0, 8, 0);
        panel.add(new JSeparator(), gbc);

        String[] docColumns = { "Document Type" };
        docTableModel = new DefaultTableModel(docColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        docTable = new JTable(docTableModel);
        docTable.setFont(Theme.REGULAR_FONT);
        docTable.setRowHeight(22);
        JScrollPane docScroll = new JScrollPane(docTable);
        docScroll.setPreferredSize(new Dimension(300, 100));

        JPanel docBtnContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        docBtnContainer.setOpaque(false);
        JButton addDocBtn = Theme.createPrimaryButton("+ Add Document");
        JButton removeDocBtn = Theme.createSecondaryButton("Remove Selected Document");
        docBtnContainer.add(addDocBtn);
        docBtnContainer.add(removeDocBtn);

        gbc.gridy = row++;
        panel.add(docScroll, gbc);

        gbc.gridy = row++;
        panel.add(docBtnContainer, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(30, 0, 10, 0);
        JPanel page2NavPanel = new JPanel(new BorderLayout());
        page2NavPanel.setOpaque(false);

        JButton backBtn = Theme.createSecondaryButton("← Back");
        JButton nextBtn = Theme.createPrimaryButton("Next Page →");
        page2NavPanel.add(backBtn, BorderLayout.WEST);
        page2NavPanel.add(nextBtn, BorderLayout.EAST);

        panel.add(page2NavPanel, gbc);

        withChildrenCombo.addActionListener(e -> {
            boolean hasKids = "Yes".equals(withChildrenCombo.getSelectedItem());
            childrenSection.setVisible(hasKids);
            panel.revalidate();
            panel.repaint();
        });

        addChildBtn.addActionListener(e -> handleAddChildDialog());
        removeChildBtn.addActionListener(e -> handleRemoveChild());
        addDocBtn.addActionListener(e -> handleAddDocDialog());
        removeDocBtn.addActionListener(e -> handleRemoveDoc());
        backBtn.addActionListener(e -> wizardCardLayout.show(wizardCardPanel, "PAGE_1"));
        nextBtn.addActionListener(e -> handlePage2Next());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    private JScrollPane buildPage3Scroll() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Theme.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        int row = 0;

        gbc.gridy = row++;
        JLabel sectionTitle = new JLabel("Application Details");
        sectionTitle.setFont(Theme.SUBTITLE_FONT);
        sectionTitle.setForeground(Theme.PRIMARY_BLUE);
        panel.add(sectionTitle, gbc);

        gbc.gridy = row++;
        panel.add(new JSeparator(), gbc);

        JPanel colPanel1 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel1.setOpaque(false);

        JPanel pEntryType = new JPanel(new BorderLayout(0, 5));
        pEntryType.setOpaque(false);
        pEntryType.add(new JLabel("<html>Requested Entry Type <font color='red'>*</font></html>"), BorderLayout.NORTH);
        entryTypeCombo = new JComboBox<>(new String[] { "Single", "Multiple" });
        entryTypeCombo.setFont(Theme.REGULAR_FONT);
        entryTypeCombo.setBackground(Theme.WHITE);
        pEntryType.add(entryTypeCombo, BorderLayout.CENTER);

        JPanel pLength = new JPanel(new BorderLayout(0, 5));
        pLength.setOpaque(false);
        pLength.add(new JLabel("<html>Length of Stay Days <font color='red'>*</font></html>"), BorderLayout.NORTH);
        lengthOfStayField = Theme.createTextField(15);
        pLength.add(lengthOfStayField, BorderLayout.CENTER);

        colPanel1.add(pEntryType);
        colPanel1.add(pLength);
        gbc.gridy = row++;
        panel.add(colPanel1, gbc);

        JPanel colPanel2 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel2.setOpaque(false);

        JPanel pPort = new JPanel(new BorderLayout(0, 5));
        pPort.setOpaque(false);
        pPort.add(new JLabel("<html>Port Of Entry <font color='red'>*</font></html>"), BorderLayout.NORTH);
        portOfEntryCombo = new JComboBox<>(new String[] { "NAIA 1", "NAIA 2", "NAIA 3", "NAIA 4" });
        portOfEntryCombo.setFont(Theme.REGULAR_FONT);
        portOfEntryCombo.setBackground(Theme.WHITE);
        pPort.add(portOfEntryCombo, BorderLayout.CENTER);

        JPanel pDest = new JPanel(new BorderLayout(0, 5));
        pDest.setOpaque(false);
        pDest.add(new JLabel("<html>Destination After Philippines <font color='red'>*</font></html>"),
                BorderLayout.NORTH);
        destinationAfterField = Theme.createTextField(15);
        pDest.add(destinationAfterField, BorderLayout.CENTER);

        colPanel2.add(pPort);
        colPanel2.add(pDest);
        gbc.gridy = row++;
        panel.add(colPanel2, gbc);

        JPanel colPanel3 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel3.setOpaque(false);

        JPanel pDateApp = new JPanel(new BorderLayout(0, 5));
        pDateApp.setOpaque(false);
        pDateApp.add(new JLabel("<html>Date of Application (YYYY/MM/DD) <font color='red'>*</font></html>"),
                BorderLayout.NORTH);
        dateOfAppField = Theme.createTextField(15);
        Theme.setupAutomaticDateField(dateOfAppField);
        pDateApp.add(dateOfAppField, BorderLayout.CENTER);

        colPanel3.add(pDateApp);
        colPanel3.add(new JPanel() {
            {
                setOpaque(false);
            }
        });
        gbc.gridy = row++;
        panel.add(colPanel3, gbc);

        gbc.gridy = row++;
        panel.add(new JLabel("<html>Purpose Type <font color='red'>*</font></html>"), gbc);
        gbc.gridy = row++;
        purposeTypeCombo = new JComboBox<>(new String[] { "Leisure", "Wellness", "Business", "Official Business" });
        purposeTypeCombo.setFont(Theme.REGULAR_FONT);
        purposeTypeCombo.setBackground(Theme.WHITE);
        panel.add(purposeTypeCombo, gbc);

        JPanel colPanel4 = new JPanel(new GridLayout(1, 2, 20, 0));
        colPanel4.setOpaque(false);

        JPanel pSponsor = new JPanel(new BorderLayout(0, 5));
        pSponsor.setOpaque(false);
        pSponsor.add(new JLabel("Sponsor Name (Optional)"), BorderLayout.NORTH);
        sponsorNameField = Theme.createTextField(15);
        pSponsor.add(sponsorNameField, BorderLayout.CENTER);

        JPanel pSponsorContact = new JPanel(new BorderLayout(0, 5));
        pSponsorContact.setOpaque(false);
        pSponsorContact.add(new JLabel("Sponsor Contact Number (Optional)"), BorderLayout.NORTH);
        sponsorContactField = Theme.createTextField(15);
        pSponsorContact.add(sponsorContactField, BorderLayout.CENTER);

        colPanel4.add(pSponsor);
        colPanel4.add(pSponsorContact);
        gbc.gridy = row++;
        panel.add(colPanel4, gbc);

        gbc.gridy = row++;
        gbc.insets = new Insets(30, 0, 10, 0);
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);

        JButton backBtn = Theme.createSecondaryButton("← Back");
        JButton submitBtn = Theme.createPrimaryButton("Submit Application");
        buttonPanel.add(backBtn, BorderLayout.WEST);
        buttonPanel.add(submitBtn, BorderLayout.EAST);

        panel.add(buttonPanel, gbc);

        backBtn.addActionListener(e -> wizardCardLayout.show(wizardCardPanel, "PAGE_2"));
        submitBtn.addActionListener(e -> handleSubmit());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        return scroll;
    }

    private void handlePage2Next() {
        String passportNum = passportNumField.getText().trim();
        String passportAuth = passportAuthField.getText().trim();
        String passportIssued = passportIssuedField.getText().trim();
        String passportExpiry = passportExpiryField.getText().trim();

        if (passportNum.isEmpty() || passportAuth.isEmpty() || passportIssued.isEmpty() || passportExpiry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required Passport fields on Page 2.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportIssued)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Issue Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportExpiry)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Validity Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (passportExpiry.compareTo(passportIssued) <= 0) {
            JOptionPane.showMessageDialog(this, "Passport Validity Date must be after Date Issued.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        wizardCardLayout.show(wizardCardPanel, "PAGE_3");
    }

    private void handleNextPage() {
        String name = fullNameField.getText().trim();
        String citizenship = citizenshipField.getText().trim();
        String bDate = birthDateField.getText().trim();
        String bPlace = birthPlaceField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || citizenship.isEmpty() || bDate.isEmpty() || bPlace.isEmpty() ||
                email.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields on Page 1.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(bDate)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Birth Date in YYYY/MM/DD format (valid month and days).", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        wizardCardLayout.show(wizardCardPanel, "PAGE_2");
    }

    private void handleAddChildDialog() {
        JTextField nameField = Theme.createTextField(15);
        JTextField ageField = Theme.createTextField(5);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("Child's Full Name:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("Child's Age:"));
        inputPanel.add(ageField);

        int result = JOptionPane.showConfirmDialog(this, inputPanel, "Add Child Profile", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String cName = nameField.getText().trim();
            String cAgeStr = ageField.getText().trim();

            if (cName.isEmpty() || cAgeStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Both Child Name and Age are required.", "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int cAge = Integer.parseInt(cAgeStr);
                if (cAge < 0 || cAge > 18) {
                    JOptionPane.showMessageDialog(this, "Child age must be between 0 and 18.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Child child = new Child(cName, cAge);
                tempChildrenList.add(child);
                childrenListModel.addElement(child.getName() + " (Age: " + child.getAge() + ")");
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for Age.", "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void handleRemoveChild() {
        int index = childrenList.getSelectedIndex();
        if (index >= 0) {
            tempChildrenList.remove(index);
            childrenListModel.remove(index);
        } else {
            JOptionPane.showMessageDialog(this, "Select a child from the list to remove.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void handleAddDocDialog() {
        JComboBox<String> typeCombo = new JComboBox<>(
                new String[] { "Air Ticket", "Invitation Letter", "Bank Certificate" });
        typeCombo.setFont(Theme.REGULAR_FONT);
        typeCombo.setBackground(Theme.WHITE);

        int result = JOptionPane.showConfirmDialog(this, typeCombo, "Select Supporting Document Type",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String selectedType = (String) typeCombo.getSelectedItem();
            Document doc = new Document(selectedType, "", "", "", "");
            tempDocList.add(doc);
            docTableModel.addRow(new Object[] {
                    doc.getDocumentType()
            });
        }
    }

    private void handleRemoveDoc() {
        int row = docTable.getSelectedRow();
        if (row >= 0) {
            tempDocList.remove(row);
            docTableModel.removeRow(row);
        } else {
            JOptionPane.showMessageDialog(this, "Select a document from the table to remove.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private int calculateAge(String birthDateStr, String appDateStr) {
        try {
            String[] bParts = birthDateStr.split("/");
            String[] aParts = appDateStr.split("/");

            int bYear = Integer.parseInt(bParts[0]);
            int bMonth = Integer.parseInt(bParts[1]);
            int bDay = Integer.parseInt(bParts[2]);

            int aYear = Integer.parseInt(aParts[0]);
            int aMonth = Integer.parseInt(aParts[1]);
            int aDay = Integer.parseInt(aParts[2]);

            int age = aYear - bYear;
            if (aMonth < bMonth || (aMonth == bMonth && aDay < bDay)) {
                age--;
            }
            return age;
        } catch (Exception e) {
            return -1;
        }
    }

    private void handleSubmit() {
        // Validation check for Page 1, 2, 3
        String name = fullNameField.getText().trim();
        String citizenship = citizenshipField.getText().trim();
        String bDate = birthDateField.getText().trim();
        String bPlace = birthPlaceField.getText().trim();
        String email = emailField.getText().trim();
        String contact = contactField.getText().trim();
        String address = addressField.getText().trim();

        if (name.isEmpty() || citizenship.isEmpty() || bDate.isEmpty() || bPlace.isEmpty() ||
                email.isEmpty() || contact.isEmpty() || address.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields on Page 1.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(bDate)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Birth Date in YYYY/MM/DD format (valid month and days).", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passportNum = passportNumField.getText().trim();
        String passportAuth = passportAuthField.getText().trim();
        String passportIssued = passportIssuedField.getText().trim();
        String passportExpiry = passportExpiryField.getText().trim();

        if (passportNum.isEmpty() || passportAuth.isEmpty() || passportIssued.isEmpty() || passportExpiry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required Passport fields on Page 2.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportIssued)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Issue Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportExpiry)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Validity Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (passportExpiry.compareTo(passportIssued) <= 0) {
            JOptionPane.showMessageDialog(this, "Passport Validity Date must be after Date Issued.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String lenStr = lengthOfStayField.getText().trim();
        String dest = destinationAfterField.getText().trim();
        String appDate = dateOfAppField.getText().trim();

        if (lenStr.isEmpty() || dest.isEmpty() || appDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields on Page 3.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int lengthOfStay = 0;
        try {
            lengthOfStay = Integer.parseInt(lenStr);
            if (lengthOfStay <= 0) {
                JOptionPane.showMessageDialog(this, "Length of Stay Days must be a positive number.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Length of Stay Days.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(appDate)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Date of Application in YYYY/MM/DD format (valid month and days).",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int ageUponApp = calculateAge(bDate, appDate);
        if (ageUponApp < 0) {
            JOptionPane.showMessageDialog(this, "Date of Application cannot be before Birth Date.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        VisaApplication app = new VisaApplication();
        app.setId(editingAppId);
        app.setUserId(currentUserId);

        app.setFullName(fullNameField.getText().trim());
        app.setSex((String) sexCombo.getSelectedItem());
        app.setCitizenship(citizenshipField.getText().trim());
        app.setCivilStatus((String) civilStatusCombo.getSelectedItem());
        app.setBirthDate(birthDateField.getText().trim());
        app.setPlaceOfBirth(birthPlaceField.getText().trim());
        app.setEmail(emailField.getText().trim());
        app.setContactNumber(contactField.getText().trim());
        app.setHomeAddress(addressField.getText().trim());

        app.setFatherName(fatherField.getText().trim());
        app.setMotherName(motherField.getText().trim());
        app.setSpouseName(spouseField.getText().trim());
        app.setWithChildren("Yes".equals(withChildrenCombo.getSelectedItem()));

        if (app.isWithChildren()) {
            app.setChildren(tempChildrenList);
        } else {
            app.setChildren(new ArrayList<>());
        }

        app.setOccupation(occupationField.getText().trim());
        app.setEmployerAddress(employerField.getText().trim());

        // Build document list: required Passport + other supporting documents
        List<Document> finalDocsList = new ArrayList<>();
        finalDocsList.add(new Document("Original Passport", passportNum, passportAuth, passportIssued, passportExpiry));
        finalDocsList.addAll(tempDocList);
        app.setDocuments(finalDocsList);

        // Set application details
        app.setEntryType((String) entryTypeCombo.getSelectedItem());
        app.setLengthOfStay(lengthOfStay);
        app.setPortOfEntry((String) portOfEntryCombo.getSelectedItem());
        app.setDestinationAfter(dest);
        app.setAgeUponApp(ageUponApp);
        app.setDateOfApp(appDate);
        app.setPurposeType((String) purposeTypeCombo.getSelectedItem());
        app.setSponsorName(sponsorNameField.getText().trim());
        app.setSponsorContact(sponsorContactField.getText().trim());

        app.setStatus("PENDING");

        boolean success;
        DatabaseManager db = DatabaseManager.getInstance();
        if (editingAppId == -1) {
            success = db.saveApplication(app);
            // OOP Demo: build Applicant model and pass through BackendBridge
            com.visa.app.model.Applicant oopApplicant = new com.visa.app.model.Applicant(
                    splitName(app.getFullName(), true),
                    splitName(app.getFullName(), false),
                    app.getBirthDate(), app.getEmail(),
                    app.getContactNumber(), app.getSex(),
                    app.getCitizenship(), app.getCivilStatus(),
                    app.getPlaceOfBirth(), app.getHomeAddress());
            System.out.println("[OOP] " + oopApplicant.getProfileSummary());
        } else {
            success = db.updateApplication(app);
        }

        if (success) {
            String message = editingAppId == -1 ? "Your Visa Application has been submitted successfully!"
                    : "Your Visa Application details have been updated successfully!";
            JOptionPane.showMessageDialog(this, message, "Submission Successful", JOptionPane.INFORMATION_MESSAGE);
            resetForm();
            mainFrame.refreshDashboard();
            mainFrame.showPanel("DASHBOARD");
        } else {
            JOptionPane.showMessageDialog(this, "Error saving application details to database.", "Submission Failed",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public void loadApplicationForEditing(VisaApplication app) {
        resetForm();
        editingAppId = app.getId();
        wizardTitleLabel.setText("Edit Visa Application (#" + editingAppId + ")");

        fullNameField.setText(app.getFullName());
        sexCombo.setSelectedItem(app.getSex());
        citizenshipField.setText(app.getCitizenship());
        civilStatusCombo.setSelectedItem(app.getCivilStatus());
        birthDateField.setText(app.getBirthDate());
        birthPlaceField.setText(app.getPlaceOfBirth());
        emailField.setText(app.getEmail());
        contactField.setText(app.getContactNumber());
        addressField.setText(app.getHomeAddress());

        fatherField.setText(app.getFatherName());
        motherField.setText(app.getMotherName());
        spouseField.setText(app.getSpouseName());
        withChildrenCombo.setSelectedItem(app.isWithChildren() ? "Yes" : "No");

        tempChildrenList.addAll(app.getChildren());
        for (Child child : app.getChildren()) {
            childrenListModel.addElement(child.getName() + " (Age: " + child.getAge() + ")");
        }

        occupationField.setText(app.getOccupation());
        employerField.setText(app.getEmployerAddress());

        passportNumField.setText("");
        passportAuthField.setText("");
        passportIssuedField.setText("");
        passportExpiryField.setText("");
        tempDocList.clear();
        docTableModel.setRowCount(0);

        for (Document doc : app.getDocuments()) {
            if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())) {
                passportNumField.setText(doc.getPassportNumber());
                passportAuthField.setText(doc.getIssuingAuthority());
                passportIssuedField.setText(doc.getDateIssued());
                passportExpiryField.setText(doc.getValidityDate());
            } else {
                tempDocList.add(doc);
                docTableModel.addRow(new Object[] { doc.getDocumentType() });
            }
        }

        // Set application details
        entryTypeCombo.setSelectedItem(app.getEntryType());
        lengthOfStayField.setText(String.valueOf(app.getLengthOfStay()));
        portOfEntryCombo.setSelectedItem(app.getPortOfEntry());
        destinationAfterField.setText(app.getDestinationAfter());
        dateOfAppField.setText(app.getDateOfApp());
        purposeTypeCombo.setSelectedItem(app.getPurposeType());
        sponsorNameField.setText(app.getSponsorName());
        sponsorContactField.setText(app.getSponsorContact());

        childrenSection.setVisible(app.isWithChildren());
        wizardCardLayout.show(wizardCardPanel, "PAGE_1");
    }

    public void resetForm() {
        editingAppId = -1;
        wizardTitleLabel.setText("New Visa Application");

        fullNameField.setText("");
        sexCombo.setSelectedIndex(0);
        citizenshipField.setText("");
        civilStatusCombo.setSelectedIndex(0);
        birthDateField.setText("");
        birthPlaceField.setText("");
        emailField.setText("");
        contactField.setText("");
        addressField.setText("");

        fatherField.setText("");
        motherField.setText("");
        spouseField.setText("");
        withChildrenCombo.setSelectedIndex(0);
        childrenListModel.clear();
        tempChildrenList.clear();
        childrenSection.setVisible(false);

        occupationField.setText("");
        employerField.setText("");

        passportNumField.setText("");
        passportAuthField.setText("");
        passportIssuedField.setText("");
        passportExpiryField.setText("");
        docTableModel.setRowCount(0);
        tempDocList.clear();

        // Reset application details
        entryTypeCombo.setSelectedIndex(0);
        lengthOfStayField.setText("");
        portOfEntryCombo.setSelectedIndex(0);
        destinationAfterField.setText("");
        dateOfAppField.setText("");
        purposeTypeCombo.setSelectedIndex(0);
        sponsorNameField.setText("");
        sponsorContactField.setText("");

        wizardCardLayout.show(wizardCardPanel, "PAGE_1");
    }

    private String splitName(String fullName, boolean firstPart) {
        if (fullName == null || !fullName.contains(" "))
            return fullName == null ? "" : fullName;
        return firstPart
                ? fullName.substring(0, fullName.indexOf(' '))
                : fullName.substring(fullName.indexOf(' ') + 1);
    }
}

// ==========================================
// 7. APPLICANT DASHBOARD PANEL
// ==========================================
class ApplicantDashboardPanel extends JPanel {
    private MainFrame mainFrame;
    private User currentUser;

    private JTable appTable;
    private DefaultTableModel tableModel;
    private List<VisaApplication> userAppsList;

    public ApplicantDashboardPanel(MainFrame mainFrame, User user) {
        this.mainFrame = mainFrame;
        this.currentUser = user;

        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel welcomeLabel = new JLabel("Welcome back, " + currentUser.getEmail());
        welcomeLabel.setFont(Theme.SUBTITLE_FONT);
        welcomeLabel.setForeground(Theme.PRIMARY_BLUE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        JLabel roleLabel = new JLabel("Applicant Dashboard");
        roleLabel.setFont(Theme.BOLD_FONT);
        roleLabel.setForeground(Theme.TEXT_MUTED);
        topPanel.add(roleLabel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));
        tableContainer.setOpaque(false);

        JLabel tableTitle = new JLabel("Your Visa Applications");
        tableTitle.setFont(Theme.HEADER_FONT);
        tableTitle.setForeground(Theme.TEXT_DARK);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = { "ID", "Full Name", "Citizenship", "Submission Date/Birth Date", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appTable = new JTable(tableModel);
        appTable.setFont(Theme.REGULAR_FONT);
        appTable.setRowHeight(26);
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        appTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                setFont(Theme.BOLD_FONT);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    if ("APPROVED".equalsIgnoreCase(status)) {
                        c.setForeground(Theme.STATUS_APPROVED);
                    } else if ("PENDING".equalsIgnoreCase(status)) {
                        c.setForeground(Theme.STATUS_PENDING);
                    } else {
                        c.setForeground(Theme.STATUS_DENIED);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(appTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 10));
        sidebar.setOpaque(false);
        Theme.setComponentSizes(sidebar, 200, 300);

        JButton viewBtn = Theme.createPrimaryButton("View Full Details");
        JButton editBtn = Theme.createSecondaryButton("Edit Application");
        JButton deleteBtn = Theme.createDangerButton("Cancel / Delete");
        JButton exportBtn = Theme.createSecondaryButton("Export to XML 💾");
        JButton refreshBtn = Theme.createSecondaryButton("Refresh List ↻");

        sidebar.add(viewBtn);
        sidebar.add(editBtn);
        sidebar.add(deleteBtn);
        sidebar.add(exportBtn);
        sidebar.add(refreshBtn);
        sidebar.add(new JLabel(""));

        add(sidebar, BorderLayout.EAST);

        viewBtn.addActionListener(e -> handleViewDetails());
        editBtn.addActionListener(e -> handleEditApp());
        deleteBtn.addActionListener(e -> handleDeleteApp());
        exportBtn.addActionListener(e -> handleExportXML());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        userAppsList = DatabaseManager.getInstance().getApplicationsByUserId(currentUser.getId());
        BackendBridge.getInstance().getApplicantsWithPassportDetails(); // Q9: 3-table JOIN
        for (VisaApplication app : userAppsList) {
            tableModel.addRow(new Object[] {
                    app.getId(),
                    app.getFullName(),
                    app.getCitizenship(),
                    app.getBirthDate(),
                    app.getStatus()
            });
        }
    }

    private VisaApplication getSelectedApplication() {
        int row = appTable.getSelectedRow();
        if (row >= 0) {
            int appId = (Integer) tableModel.getValueAt(row, 0);
            for (VisaApplication app : userAppsList) {
                if (app.getId() == appId) {
                    return app;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an application from the table.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
        return null;
    }

    private void handleViewDetails() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            Theme.showDetailsDialog(mainFrame, app);
        }
    }

    private void handleEditApp() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            if (!"PENDING".equalsIgnoreCase(app.getStatus())) {
                JOptionPane.showMessageDialog(this, "Only pending applications can be modified.", "Access Denied",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            mainFrame.editApplication(app);
        }
    }

    private void handleDeleteApp() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete this visa application? This action cannot be undone.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = DatabaseManager.getInstance().deleteApplication(app.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Application removed successfully.", "Deleted",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete from database.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleExportXML() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Export Visa Application XML");
            fileChooser.setSelectedFile(new File("visa_application_" + app.getId() + ".xml"));

            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                boolean success = XMLManager.exportApplicationToXML(app, fileToSave);
                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "Application successfully saved to:\n" + fileToSave.getAbsolutePath(),
                            "XML Export Complete", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Export operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

}

// ==========================================
// 8. ADMINISTRATOR DASHBOARD PANEL
// ==========================================
class AdminDashboardPanel extends JPanel {
    private MainFrame mainFrame;

    private JTable appTable;
    private DefaultTableModel tableModel;
    private List<VisaApplication> applicationsList;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Theme.WHITE);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)));

        JLabel welcomeLabel = new JLabel("Administrator Dashboard");
        welcomeLabel.setFont(Theme.SUBTITLE_FONT);
        welcomeLabel.setForeground(Theme.PRIMARY_BLUE);
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        add(topPanel, BorderLayout.NORTH);

        JPanel tableContainer = new JPanel(new BorderLayout(10, 10));
        tableContainer.setOpaque(false);

        JLabel tableTitle = new JLabel("All Visa Application Submissions");
        tableTitle.setFont(Theme.HEADER_FONT);
        tableTitle.setForeground(Theme.TEXT_DARK);
        tableContainer.add(tableTitle, BorderLayout.NORTH);

        String[] columns = { "ID", "Email", "Full Name", "Citizenship", "Date of Application", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        appTable = new JTable(tableModel);
        appTable.setFont(Theme.REGULAR_FONT);
        appTable.setRowHeight(26);
        appTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        appTable.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                setFont(Theme.BOLD_FONT);
                setHorizontalAlignment(SwingConstants.CENTER);

                if (!isSelected) {
                    if ("APPROVED".equalsIgnoreCase(status)) {
                        c.setForeground(Theme.STATUS_APPROVED);
                    } else if ("PENDING".equalsIgnoreCase(status)) {
                        c.setForeground(Theme.STATUS_PENDING);
                    } else {
                        c.setForeground(Theme.STATUS_DENIED);
                    }
                }
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(appTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        add(tableContainer, BorderLayout.CENTER);

        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 10));
        sidebar.setOpaque(false);
        Theme.setComponentSizes(sidebar, 200, 300);

        JButton viewBtn = Theme.createPrimaryButton("View Full Details");
        JButton approveBtn = Theme.createButton("Approve Visa", Theme.STATUS_APPROVED, Theme.WHITE);
        JButton denyBtn = Theme.createDangerButton("Deny Visa");
        JButton deleteBtn = Theme.createSecondaryButton("Delete Record");
        JButton exportBtn = Theme.createSecondaryButton("Export All to XML");
        JButton refreshBtn = Theme.createSecondaryButton("Refresh List");

        sidebar.add(viewBtn);
        sidebar.add(approveBtn);
        sidebar.add(denyBtn);
        sidebar.add(deleteBtn);
        sidebar.add(exportBtn);
        sidebar.add(refreshBtn);

        add(sidebar, BorderLayout.EAST);

        viewBtn.addActionListener(e -> handleViewDetails());
        approveBtn.addActionListener(e -> handleUpdateStatus("APPROVED"));
        denyBtn.addActionListener(e -> handleUpdateStatus("DENIED"));
        deleteBtn.addActionListener(e -> handleDeleteRecord());
        exportBtn.addActionListener(e -> handleExportAllXML());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        applicationsList = DatabaseManager.getInstance().getAllApplications();
        BackendBridge.getInstance().getApplicationsWithDocumentCount(); // Q6: JOIN + COUNT
        for (VisaApplication app : applicationsList) {
            tableModel.addRow(new Object[] {
                    app.getId(),
                    app.getEmail(),
                    app.getFullName(),
                    app.getCitizenship(),
                    app.getDateOfApp(),
                    app.getStatus()
            });
        }
    }

    private VisaApplication getSelectedApplication() {
        int row = appTable.getSelectedRow();
        if (row >= 0) {
            int appId = (Integer) tableModel.getValueAt(row, 0);
            for (VisaApplication app : applicationsList) {
                if (app.getId() == appId) {
                    return app;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a submission from the table.", "No Selection",
                    JOptionPane.WARNING_MESSAGE);
        }
        return null;
    }

    private void handleViewDetails() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            Theme.showDetailsDialog(mainFrame, app);
        }
    }

    private void handleUpdateStatus(String status) {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to mark Visa Application #" + app.getId() + " as " + status + "?",
                    "Confirm Status Update",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = BackendBridge.getInstance().approveApplication(app.getId());
                if ("DENIED".equalsIgnoreCase(status)) {
                    success = BackendBridge.getInstance().denyApplication(app.getId());
                }
                if (success) {
                    JOptionPane.showMessageDialog(this, "Application status successfully updated to " + status + ".",
                            "Status Updated", JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update status in the database.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleDeleteRecord() {
        VisaApplication app = getSelectedApplication();
        if (app != null) {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to permanently delete Visa Application #" + app.getId()
                            + "?\nThis deletes all related travel docs and children.",
                    "Confirm Record Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = DatabaseManager.getInstance().deleteApplication(app.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Record successfully deleted.", "Record Removed",
                            JOptionPane.INFORMATION_MESSAGE);
                    refreshData();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete record.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void handleExportAllXML() {
        if (applicationsList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No records to export.", "Empty Set", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Bulk Export All Applications to XML");
        fileChooser.setSelectedFile(new File("all_visa_applications_export.xml"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            boolean success = XMLManager.exportAllApplicationsToXML(applicationsList, fileToSave);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "Successfully exported all applications to:\n" + fileToSave.getAbsolutePath(),
                        "XML Bulk Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Export operation failed.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

}
