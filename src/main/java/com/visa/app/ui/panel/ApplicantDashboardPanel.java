package com.visa.app.ui.panel;

import com.visa.app.DatabaseManager;
import com.visa.app.model.Document;
import com.visa.app.model.User;
import com.visa.app.model.VisaApplication;
import com.visa.app.ui.screen.MainFrame;
import com.visa.app.ui.theme.Theme;
import com.visa.app.utils.BackendBridge;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * ============================================================
 *  PANEL: ApplicantDashboardPanel (Applicant View)
 * ============================================================
 */
public class ApplicantDashboardPanel extends JPanel {
    private final MainFrame mainFrame;
    private final User currentUser;

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

        // Search bar
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        searchBar.setOpaque(false);
        JLabel searchLbl = new JLabel("Search:");
        searchLbl.setFont(Theme.BOLD_FONT);
        JTextField searchBox = Theme.createTextField(22);
        searchBox.setPreferredSize(new Dimension(220, 32));
        JButton searchBtn = Theme.createPrimaryButton("Search");
        JButton clearBtn  = Theme.createSecondaryButton("Clear");
        searchBar.add(searchLbl);
        searchBar.add(searchBox);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);

        JLabel tableTitle = new JLabel("Your Visa Applications");
        tableTitle.setFont(Theme.HEADER_FONT);
        tableTitle.setForeground(Theme.TEXT_DARK);

        JPanel northWrap = new JPanel(new BorderLayout(0, 4));
        northWrap.setOpaque(false);
        northWrap.add(tableTitle, BorderLayout.NORTH);
        northWrap.add(searchBar,  BorderLayout.SOUTH);
        tableContainer.add(northWrap, BorderLayout.NORTH);

        String[] columns = { "Application ID", "Passport Used", "Date of Application", "Status" };
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

        appTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
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
        JButton refreshBtn = Theme.createSecondaryButton("Refresh List ↻");

        sidebar.add(viewBtn);
        sidebar.add(editBtn);
        sidebar.add(deleteBtn);
        sidebar.add(refreshBtn);
        sidebar.add(new JLabel(""));

        add(sidebar, BorderLayout.EAST);

        viewBtn.addActionListener(e -> handleViewDetails());
        editBtn.addActionListener(e -> handleEditApp());
        deleteBtn.addActionListener(e -> handleDeleteApp());
        refreshBtn.addActionListener(e -> refreshData());
        searchBtn.addActionListener(e -> handleSearch(searchBox.getText().trim(), tableModel));
        searchBox.addActionListener(e -> searchBtn.doClick());
        clearBtn.addActionListener(e -> { searchBox.setText(""); refreshData(); });

        refreshData();
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        userAppsList = DatabaseManager.getInstance().getApplicationsByUserId(currentUser.getId());
        BackendBridge.getInstance().getApplicantsWithPassportDetails(); // Q9: 3-table JOIN
        for (VisaApplication app : userAppsList) {
            String passportNo = "N/A";
            if (app.getDocuments() != null) {
                for (Document doc : app.getDocuments()) {
                    if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())
                            && !doc.getPassportNumber().isBlank()) {
                        passportNo = doc.getPassportNumber();
                        break;
                    }
                }
            }
            tableModel.addRow(new Object[] {
                    app.getId(),
                    passportNo,
                    app.getDateOfApp(),
                    app.getStatus()
            });
        }
    }

    public void handleSearch(String keyword, DefaultTableModel targetModel) {
        targetModel.setRowCount(0);
        if (keyword == null || keyword.isBlank()) {
            for (VisaApplication app : userAppsList) {
                String pNo = "N/A";
                if (app.getDocuments() != null) {
                    for (Document doc : app.getDocuments()) {
                        if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())
                                && !doc.getPassportNumber().isBlank()) {
                            pNo = doc.getPassportNumber(); break;
                        }
                    }
                }
                targetModel.addRow(new Object[] {
                        app.getId(), pNo, app.getDateOfApp(), app.getStatus()
                });
            }
            return;
        }
        String kw = keyword.toLowerCase();
        boolean found = false;
        for (VisaApplication app : userAppsList) {
            String pNo = "N/A";
            if (app.getDocuments() != null) {
                for (Document doc : app.getDocuments()) {
                    if ("Original Passport".equalsIgnoreCase(doc.getDocumentType())
                            && !doc.getPassportNumber().isBlank()) {
                        pNo = doc.getPassportNumber(); break;
                    }
                }
            }
            if (String.valueOf(app.getId()).contains(kw)
                    || pNo.toLowerCase().contains(kw)
                    || app.getDateOfApp().toLowerCase().contains(kw)
                    || app.getStatus().toLowerCase().contains(kw)) {
                targetModel.addRow(new Object[] {
                        app.getId(), pNo, app.getDateOfApp(), app.getStatus()
                });
                found = true;
            }
        }
        if (!found) {
            JOptionPane.showMessageDialog(this,
                    "No applications found matching: \"" + keyword + "\"",
                    "No Results", JOptionPane.INFORMATION_MESSAGE);
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
}
