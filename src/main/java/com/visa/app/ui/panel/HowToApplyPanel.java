package com.visa.app.ui.panel;

import com.visa.app.ui.screen.MainFrame;
import com.visa.app.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 *  PANEL: HowToApplyPanel (Instructions Screen)
 * ============================================================
 */
public class HowToApplyPanel extends JPanel {
    private final MainFrame mainFrame;

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
