package com.visa.app.ui;

import com.visa.app.utils.BackendBridge;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

/**
 * ============================================================
 *  UI WIRING: AdminActionHandler
 * ============================================================
 *
 * Provides static helper methods that the admin panel's buttons
 * call directly.  Each method follows the same 4-step pattern:
 *   1. Read from UI  2. Build model  3. Call DAO  4. Refresh table
 *
 * HOW TO ATTACH TO ADMIN PANEL BUTTONS:
 * ─────────────────────────────────────────────────────────────
 *   // Approve button
 *   btnApprove.addActionListener(e ->
 *       AdminActionHandler.approveSelected(table, tableModel));
 *
 *   // Deny button
 *   btnDeny.addActionListener(e ->
 *       AdminActionHandler.denySelected(table, tableModel));
 *
 *   // Search field + button
 *   btnSearch.addActionListener(e ->
 *       AdminActionHandler.searchAndRefresh(txtSearch.getText(), tableModel));
 *
 *   // Load all on startup
 *   AdminActionHandler.loadAllIntoTable(tableModel);
 * ─────────────────────────────────────────────────────────────
 */
public class AdminActionHandler {

    // ── Approve the selected table row ────────────────────────────────────────
    public static void approveSelected(JTable table, DefaultTableModel tableModel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select an application first.");
            return;
        }

        // Column 0 of the table model holds the Application ID
        int appId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

        BackendBridge backend = BackendBridge.getInstance();
        boolean ok = backend.approveApplication(appId);   // Q4 UPDATE

        if (ok) {
            tableModel.setValueAt("APPROVED", selectedRow, 3);  // update status column
            JOptionPane.showMessageDialog(null,
                "Application #" + appId + " has been APPROVED.",
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Deny the selected table row ───────────────────────────────────────────
    public static void denySelected(JTable table, DefaultTableModel tableModel) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(null, "Please select an application first.");
            return;
        }

        int appId = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());

        BackendBridge backend = BackendBridge.getInstance();
        boolean ok = backend.denyApplication(appId);     // Q4 UPDATE

        if (ok) {
            tableModel.setValueAt("DENIED", selectedRow, 3);
            JOptionPane.showMessageDialog(null,
                "Application #" + appId + " has been DENIED.",
                "Status Updated", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Update failed.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Search and refresh the table (Q5 LIKE query) ──────────────────────────
    public static void searchAndRefresh(String keyword, DefaultTableModel tableModel) {
        BackendBridge backend = BackendBridge.getInstance();
        List<String[]> results = keyword.isBlank()
            ? backend.getApplicationsWithDocumentCount()   // Q6: show all
            : backend.searchApplicationsAsRows(keyword);   // Q5: filtered
        populateTable(tableModel, results);
    }

    // ── Load all applications into table on panel open ────────────────────────
    public static void loadAllIntoTable(DefaultTableModel tableModel) {
        BackendBridge backend = BackendBridge.getInstance();
        List<String[]> all = backend.getApplicationsWithDocumentCount();  // Q6
        populateTable(tableModel, all);
    }

    // ── Helper: fills table rows from a list of String[] rows ────────────────
    // Columns: [0]=application_id, [1]=name, [2]=citizenship, [3]=status, [4]=doc_count
    private static void populateTable(DefaultTableModel model, List<String[]> rows) {
        model.setRowCount(0);
        for (String[] row : rows) {
            model.addRow(new Object[]{
                row[0],  // application_id
                row[1],  // name
                row[2],  // citizenship
                row[3],  // status
                row[4]   // doc_count
            });
        }
        System.out.println("[UI] Table refreshed with " + rows.size() + " applications.");
    }
}