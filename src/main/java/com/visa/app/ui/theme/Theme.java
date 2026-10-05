package com.visa.app.ui.theme;

import com.visa.app.model.Child;
import com.visa.app.model.Document;
import com.visa.app.model.VisaApplication;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 *  UI THEME: Theme Styling Tokens & Visual Helpers
 * ============================================================
 */
public class Theme {

    // Brand Colors
    public static final Color PRIMARY_BLUE = new Color(15, 76, 129); // Classic Deep Blue
    public static final Color ACCENT_BLUE  = new Color(41, 128, 185); // Vibrant Sky Blue
    public static final Color BACKGROUND   = new Color(245, 247, 250); // Soft Off-white
    public static final Color WHITE        = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(210, 220, 235);
    public static final Color TEXT_DARK    = new Color(45, 55, 72);
    public static final Color TEXT_LIGHT   = Color.WHITE;
    public static final Color TEXT_MUTED   = new Color(113, 128, 150);

    // Status Colors
    public static final Color STATUS_APPROVED = new Color(46, 204, 113); // Emerald Green
    public static final Color STATUS_PENDING  = new Color(241, 196, 15);  // Yellow
    public static final Color STATUS_DENIED   = new Color(231, 76, 60);   // Alizarin Red

    // Typography
    public static final Font TITLE_FONT    = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font SUBTITLE_FONT = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font HEADER_FONT   = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font REGULAR_FONT  = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font BOLD_FONT     = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font SMALL_FONT    = new Font("Segoe UI", Font.PLAIN, 11);

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

    public static void styleInputComponent(JComponent comp) {
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

        html.append("<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>1. Personal Information</h3>");
        html.append("<b>Sex:</b> ").append(app.getSex()).append("<br>");
        html.append("<b>Citizenship:</b> ").append(app.getCitizenship()).append("<br>");
        html.append("<b>Civil Status:</b> ").append(app.getCivilStatus()).append("<br>");
        html.append("<b>Birth Date:</b> ").append(app.getBirthDate()).append("<br>");
        html.append("<b>Place of Birth:</b> ").append(app.getPlaceOfBirth()).append("<br>");
        html.append("<b>Email:</b> ").append(app.getEmail()).append("<br>");
        html.append("<b>Contact:</b> ").append(app.getContactNumber()).append("<br>");
        html.append("<b>Home Address:</b> ").append(app.getHomeAddress()).append("<br>");

        html.append("<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>2. Application Details</h3>");
        html.append("<b>Requested Entry Type:</b> ").append(app.getEntryType()).append("<br>");
        html.append("<b>Length of Stay:</b> ").append(app.getLengthOfStay()).append(" days<br>");
        html.append("<b>Port of Entry:</b> ").append(app.getPortOfEntry()).append("<br>");
        html.append("<b>Destination After Philippines:</b> ").append(app.getDestinationAfter()).append("<br>");
        html.append("<b>Age Upon Application:</b> ").append(app.getAgeUponApp()).append("<br>");
        html.append("<b>Date of Application:</b> ").append(app.getDateOfApp()).append("<br>");
        html.append("<b>Purpose Type:</b> ").append(app.getPurposeType()).append("<br>");
        html.append("<b>Sponsor Name:</b> ").append(app.getSponsorName().isEmpty() ? "N/A" : app.getSponsorName()).append("<br>");
        html.append("<b>Sponsor Contact Number:</b> ").append(app.getSponsorContact().isEmpty() ? "N/A" : app.getSponsorContact()).append("<br>");

        html.append("<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>3. Family Information</h3>");
        html.append("<b>Father's Name:</b> ").append(app.getFatherName().isEmpty() ? "N/A" : app.getFatherName()).append("<br>");
        html.append("<b>Mother's Name:</b> ").append(app.getMotherName().isEmpty() ? "N/A" : app.getMotherName()).append("<br>");
        html.append("<b>Spouse's Name:</b> ").append(app.getSpouseName().isEmpty() ? "N/A" : app.getSpouseName()).append("<br>");
        html.append("<b>With Children:</b> ").append(app.isWithChildren() ? "Yes" : "No").append("<br>");

        if (app.isWithChildren() && app.getChildren() != null && !app.getChildren().isEmpty()) {
            html.append("<b>Children Profiles:</b><ul style='margin-top: 3px;'>");
            for (Child c : app.getChildren()) {
                html.append("<li>").append(c.getName()).append(" (Age: ").append(c.getAge()).append(")</li>");
            }
            html.append("</ul>");
        }

        html.append("<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>4. Employment Information</h3>");
        html.append("<b>Occupation:</b> ").append(app.getOccupation().isEmpty() ? "N/A" : app.getOccupation()).append("<br>");
        html.append("<b>Employer/Address:</b> ").append(app.getEmployerAddress().isEmpty() ? "N/A" : app.getEmployerAddress()).append("<br>");

        html.append("<h3 style='color:#0f4c81; border-bottom: 1px solid #d2e0eb; padding-bottom: 3px;'>5. Attached Travel Documents</h3>");
        if (app.getDocuments() != null && !app.getDocuments().isEmpty()) {
            html.append("<table border='1' style='border-collapse: collapse; font-size: 10px; width: 100%; text-align: left;'>");
            html.append("<tr style='background-color: #f5f7fa;'><th>Type</th><th>Number</th><th>Authority</th><th>Dates</th></tr>");
            for (Document d : app.getDocuments()) {
                html.append("<tr>");
                html.append("<td>").append(d.getDocumentType()).append("</td>");
                html.append("<td>").append(d.getPassportNumber().isEmpty() ? "N/A" : d.getPassportNumber()).append("</td>");
                html.append("<td>").append(d.getIssuingAuthority().isEmpty() ? "N/A" : d.getIssuingAuthority()).append("</td>");
                if ("Original Passport".equalsIgnoreCase(d.getDocumentType())) {
                    html.append("<td>").append(d.getDateIssued()).append(" - ").append(d.getValidityDate()).append("</td>");
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

            if (month < 1 || month > 12) return false;

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

    public static boolean isFutureDate(String dateStr) {
        if (!isValidDateString(dateStr)) return false;
        String[] parts = dateStr.split("/");
        java.time.LocalDate inputDate = java.time.LocalDate.of(
                Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
        return inputDate.isAfter(java.time.LocalDate.now());
    }

    public static boolean isValidContact(String contact) {
        if (contact == null || contact.isBlank()) return false;
        String stripped = contact.replaceAll("[\\s\\-]", "");
        return stripped.matches("\\+?\\d{7,15}");
    }

    public static boolean isValidName(String name) {
        if (name == null || name.trim().length() < 2) return false;
        return name.trim().matches("[A-Za-zÀ-ÖØ-öø-ÿ][A-Za-zÀ-ÖØ-öø-ÿ .'\\-]{1,}");
    }

    public static boolean isValidPassportNo(String passportNo) {
        if (passportNo == null || passportNo.isBlank()) return false;
        return passportNo.trim().matches("[A-Za-z0-9]{6,20}");
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
                if (isDeleting) return;

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
                if (text.isEmpty()) return;
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
