package com.visa.app.ui.panel;

import com.visa.app.DatabaseManager;
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
 *  PANEL: AdminDashboardPanel (Administrator View)
 * ============================================================
 */
public class AdminDashboardPanel extends JPanel {
    private final MainFrame mainFrame;

    private JTable appTable;
    private DefaultTableModel tableModel;
    private List<VisaApplication> applicationsList;

    // Search tab fields
    private JTextField searchField;
    private DefaultTableModel searchTableModel;

    public AdminDashboardPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setBackground(Theme.BACKGROUND);
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // Header
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

        // Tabbed center panel
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(Theme.BOLD_FONT);
        tabbedPane.addTab("All Applications", buildApplicationsTab());
        tabbedPane.addTab("Search Records",   buildSearchTab());
        tabbedPane.addTab("Reports",          buildReportTab());
        add(tabbedPane, BorderLayout.CENTER);

        // Sidebar actions
        JPanel sidebar = new JPanel(new GridLayout(6, 1, 0, 10));
        sidebar.setOpaque(false);
        Theme.setComponentSizes(sidebar, 200, 300);

        JButton viewBtn    = Theme.createPrimaryButton("View Full Details");
        JButton approveBtn = Theme.createButton("Approve Visa", Theme.STATUS_APPROVED, Theme.WHITE);
        JButton denyBtn    = Theme.createDangerButton("Deny Visa");
        JButton deleteBtn  = Theme.createSecondaryButton("Delete Record");
        JButton refreshBtn = Theme.createSecondaryButton("Refresh List");

        sidebar.add(viewBtn);
        sidebar.add(approveBtn);
        sidebar.add(denyBtn);
        sidebar.add(deleteBtn);
        sidebar.add(refreshBtn);
        add(sidebar, BorderLayout.EAST);

        viewBtn.addActionListener(e    -> handleViewDetails());
        approveBtn.addActionListener(e -> handleUpdateStatus("APPROVED"));
        denyBtn.addActionListener(e    -> handleUpdateStatus("DENIED"));
        deleteBtn.addActionListener(e  -> handleDeleteRecord());
        refreshBtn.addActionListener(e -> refreshData());

        refreshData();
    }

    // ── TAB 1: All Applications ───────────────────────────────────────────────
    private JPanel buildApplicationsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel tableTitle = new JLabel("All Visa Application Submissions");
        tableTitle.setFont(Theme.HEADER_FONT);
        tableTitle.setForeground(Theme.TEXT_DARK);
        panel.add(tableTitle, BorderLayout.NORTH);

        String[] columns = { "ID", "Email", "Full Name", "Citizenship", "Date of Application", "Status" };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
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
                String status = value == null ? "" : (String) value;
                setFont(Theme.BOLD_FONT);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    if ("APPROVED".equalsIgnoreCase(status))     c.setForeground(Theme.STATUS_APPROVED);
                    else if ("PENDING".equalsIgnoreCase(status)) c.setForeground(Theme.STATUS_PENDING);
                    else                                         c.setForeground(Theme.STATUS_DENIED);
                }
                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(appTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // ── TAB 2: Search Records ─────────────────────────────────────────────────
    private JPanel buildSearchTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        // Search bar row
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        searchBar.setOpaque(false);
        JLabel searchLbl = new JLabel("Search by name, citizenship, or status:");
        searchLbl.setFont(Theme.BOLD_FONT);
        searchLbl.setForeground(Theme.TEXT_DARK);
        searchField = Theme.createTextField(30);
        searchField.setPreferredSize(new Dimension(280, 34));
        JButton searchBtn  = Theme.createPrimaryButton("Search");
        JButton clearBtn   = Theme.createSecondaryButton("Clear / Show All");

        searchBar.add(searchLbl);
        searchBar.add(searchField);
        searchBar.add(searchBtn);
        searchBar.add(clearBtn);
        panel.add(searchBar, BorderLayout.NORTH);

        // Results table
        String[] cols = { "App ID", "Full Name", "Citizenship", "Status", "Docs Submitted" };
        searchTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable searchTable = new JTable(searchTableModel);
        searchTable.setFont(Theme.REGULAR_FONT);
        searchTable.setRowHeight(26);
        searchTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        searchTable.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = value == null ? "" : (String) value;
                setFont(Theme.BOLD_FONT);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    if ("APPROVED".equalsIgnoreCase(status))     c.setForeground(Theme.STATUS_APPROVED);
                    else if ("PENDING".equalsIgnoreCase(status)) c.setForeground(Theme.STATUS_PENDING);
                    else                                         c.setForeground(Theme.STATUS_DENIED);
                }
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(searchTable);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);

        JLabel noResultsLabel = new JLabel("No records found matching your search.", SwingConstants.CENTER);
        noResultsLabel.setFont(Theme.BOLD_FONT);
        noResultsLabel.setForeground(Theme.TEXT_MUTED);
        noResultsLabel.setVisible(false);
        panel.add(noResultsLabel, BorderLayout.SOUTH);

        // Wire search button — Q5: WHERE + LIKE
        searchBtn.addActionListener(e -> {
            String keyword = searchField.getText().trim();
            if (keyword.isEmpty()) {
                JOptionPane.showMessageDialog(panel,
                        "Please enter a keyword to search.", "Empty Search",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            List<String[]> results = BackendBridge.getInstance().searchApplicationsAsRows(keyword); // Q5
            searchTableModel.setRowCount(0);
            if (results.isEmpty()) {
                noResultsLabel.setText("No records found for: \"" + keyword + "\"");
                noResultsLabel.setVisible(true);
            } else {
                noResultsLabel.setVisible(false);
                for (String[] row : results) {
                    searchTableModel.addRow(row);
                }
            }
        });

        searchField.addActionListener(e -> searchBtn.doClick());

        clearBtn.addActionListener(e -> {
            searchField.setText("");
            noResultsLabel.setVisible(false);
            List<String[]> all = BackendBridge.getInstance().getApplicationsWithDocumentCount(); // Q6
            searchTableModel.setRowCount(0);
            for (String[] row : all) {
                searchTableModel.addRow(row);
            }
        });

        List<String[]> all = BackendBridge.getInstance().getApplicationsWithDocumentCount();
        for (String[] row : all) {
            searchTableModel.addRow(row);
        }

        return panel;
    }

    // ── TAB 3: Report Generation ──────────────────────────────────────────────
    private JPanel buildReportTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JLabel title = new JLabel("System Reports & Summaries");
        title.setFont(Theme.HEADER_FONT);
        title.setForeground(Theme.TEXT_DARK);
        panel.add(title, BorderLayout.NORTH);

        // Summary stats cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 12, 0));
        statsRow.setOpaque(false);
        statsRow.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        List<VisaApplication> all = DatabaseManager.getInstance().getAllApplications();
        long total    = all.size();
        long pending  = all.stream().filter(a -> "PENDING".equalsIgnoreCase(a.getStatus())).count();
        long approved = all.stream().filter(a -> "APPROVED".equalsIgnoreCase(a.getStatus())).count();
        long denied   = all.stream().filter(a -> "DENIED".equalsIgnoreCase(a.getStatus())).count();

        statsRow.add(buildStatCard("Total Applications", String.valueOf(total),   Theme.PRIMARY_BLUE));
        statsRow.add(buildStatCard("Pending",            String.valueOf(pending),  Theme.STATUS_PENDING));
        statsRow.add(buildStatCard("Approved",           String.valueOf(approved), Theme.STATUS_APPROVED));
        statsRow.add(buildStatCard("Denied",             String.valueOf(denied),   Theme.STATUS_DENIED));

        // Report tables panel (Q9 and Q10/Q11)
        JPanel tablesPanel = new JPanel();
        tablesPanel.setLayout(new BoxLayout(tablesPanel, BoxLayout.Y_AXIS));
        tablesPanel.setOpaque(false);

        // Q9: 3-table JOIN — passport details
        List<String[]> passportRows = BackendBridge.getInstance().getApplicantsWithPassportDetails();
        String[] ppCols = { "Email", "Full Name", "Citizenship", "Status", "Passport No.", "Issued By", "Valid Until" };
        JPanel q9Panel = buildReportTable(
            "Q9 Report — Applicants with Passport Details (3-Table JOIN)",
            ppCols, passportRows);
        tablesPanel.add(q9Panel);
        tablesPanel.add(Box.createVerticalStrut(16));

        // Q10: Fully-documented applications
        List<String[]> completeRows = BackendBridge.getInstance().getCompleteApplications();
        String[] compCols = { "App ID", "Full Name", "Citizenship", "Status" };
        JPanel q10Panel = buildReportTable(
            "Q10 Report — Fully-Documented Applications (Subquery + HAVING)",
            compCols, completeRows);
        tablesPanel.add(q10Panel);
        tablesPanel.add(Box.createVerticalStrut(16));

        // Q11: Expiring passports
        List<String[]> expiringRows = BackendBridge.getInstance().getApplicationsWithExpiringPassports();
        String[] expCols = { "Full Name", "Citizenship", "Passport No.", "App Date", "Valid Until", "Days Left", "Urgency" };
        JPanel q11Panel = buildReportTable(
            "Q11 Report — Passports Expiring Within 180 Days (Correlated Subquery)",
            expCols, expiringRows);
        tablesPanel.add(q11Panel);

        // Combine stats + tables into a scrollable pane
        JPanel combined = new JPanel(new BorderLayout(0, 12));
        combined.setOpaque(false);
        combined.add(statsRow,   BorderLayout.NORTH);
        combined.add(tablesPanel, BorderLayout.CENTER);

        JScrollPane scroll = new JScrollPane(combined);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        panel.add(scroll, BorderLayout.CENTER);

        JButton refreshBtn = Theme.createSecondaryButton("Refresh Report Data");
        refreshBtn.addActionListener(e -> {
            JTabbedPane tp = (JTabbedPane) panel.getParent();
            if (tp != null) {
                int idx = tp.indexOfComponent(panel);
                tp.setComponentAt(idx, buildReportTab());
                tp.setSelectedIndex(idx);
            }
        });
        panel.add(refreshBtn, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel buildStatCard(String label, String value, Color color) {
        JPanel card = new JPanel(new BorderLayout(4, 4));
        card.setBackground(Theme.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER_COLOR),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)));
        JLabel valLabel = new JLabel(value, SwingConstants.CENTER);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        valLabel.setForeground(color);
        JLabel lbl = new JLabel(label, SwingConstants.CENTER);
        lbl.setFont(Theme.REGULAR_FONT);
        lbl.setForeground(Theme.TEXT_MUTED);
        card.add(valLabel, BorderLayout.CENTER);
        card.add(lbl,      BorderLayout.SOUTH);
        return card;
    }

    private JPanel buildReportTable(String titleText, String[] columns, List<String[]> rows) {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setOpaque(false);

        JLabel lbl = new JLabel(titleText);
        lbl.setFont(Theme.BOLD_FONT);
        lbl.setForeground(Theme.PRIMARY_BLUE);
        panel.add(lbl, BorderLayout.NORTH);

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        for (String[] row : rows) {
            model.addRow(row);
        }
        if (rows.isEmpty()) {
            model.addRow(new String[columns.length]);
            JLabel empty = new JLabel("  No data available for this report.", SwingConstants.LEFT);
            empty.setFont(Theme.SMALL_FONT);
            empty.setForeground(Theme.TEXT_MUTED);
            panel.add(empty, BorderLayout.SOUTH);
        }

        JTable table = new JTable(model);
        table.setFont(Theme.REGULAR_FONT);
        table.setRowHeight(24);
        table.setEnabled(false);
        table.getTableHeader().setFont(Theme.BOLD_FONT);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(600, rows.isEmpty() ? 60 : Math.min(rows.size() * 26 + 30, 180)));
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER_COLOR));
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
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
                boolean success;
                if ("APPROVED".equalsIgnoreCase(status)) {
                    success = BackendBridge.getInstance().approveApplication(app.getId());
                } else if ("DENIED".equalsIgnoreCase(status)) {
                    success = BackendBridge.getInstance().denyApplication(app.getId());
                } else {
                    success = DatabaseManager.getInstance().updateApplicationStatus(app.getId(), status);
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
}
