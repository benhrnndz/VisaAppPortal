package com.visa.app.ui.screen;

import com.visa.app.model.User;
import com.visa.app.model.VisaApplication;
import com.visa.app.ui.panel.AdminDashboardPanel;
import com.visa.app.ui.panel.ApplicantDashboardPanel;
import com.visa.app.ui.panel.HowToApplyPanel;
import com.visa.app.ui.panel.MainMenuPanel;
import com.visa.app.ui.panel.VisaApplicationWizard;
import com.visa.app.ui.theme.Theme;

import javax.swing.*;
import java.awt.*;

/**
 * ============================================================
 *  SCREEN: MainFrame (Primary Navigation & Screen Container)
 * ============================================================
 */
public class MainFrame extends JFrame {
    private final User currentUser;
    private final CardLayout cardLayout;
    private final JPanel centerPanel;

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
