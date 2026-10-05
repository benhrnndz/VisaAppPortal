package com.visa.app.ui.panel;

import com.visa.app.ui.screen.MainFrame;
import com.visa.app.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 *  PANEL: MainMenuPanel (Home Landing Screen)
 * ============================================================
 */
public class MainMenuPanel extends JPanel {
    private final MainFrame mainFrame;

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
