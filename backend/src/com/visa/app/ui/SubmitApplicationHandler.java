package com.visa.app.ui;

import com.visa.app.model.Applicant;
import com.visa.app.utils.BackendBridge;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * ============================================================
 *  UI WIRING: SubmitApplicationHandler
 * ============================================================
 *
 * This ActionListener is attached to the Submit button on the
 * visa application form.  It demonstrates the exact 4-step flow:
 *
 *   1.  Extract text from UI fields  (JTextField.getText())
 *   2.  Instantiate an OOP Model     (new Applicant(...))
 *   3.  Pass model to the DAO        (BackendBridge.submitApplication())
 *   4.  Update the UI                (JOptionPane.showMessageDialog())
 *
 * HOW TO USE IN YOUR SWING PANEL:
 * ─────────────────────────────────────────────────────────────
 *   // Inside your panel's constructor or initComponents():
 *   JButton btnSubmit = new JButton("Submit");
 *   btnSubmit.addActionListener(
 *       new SubmitApplicationHandler(
 *           txtFirstName, txtLastName, txtDOB, txtEmail,
 *           txtContact, comboSex, comboCivilStatus,
 *           txtCitizenship, txtPOB, txtAddress, txtPassword
 *       )
 *   );
 * ─────────────────────────────────────────────────────────────
 */
public class SubmitApplicationHandler implements ActionListener {

    // ── References to the Swing form fields ───────────────────────────────────
    private final JTextField txtFirstName;
    private final JTextField txtLastName;
    private final JTextField txtDOB;
    private final JTextField txtEmail;
    private final JTextField txtContact;
    private final JComboBox<String> comboSex;
    private final JComboBox<String> comboCivilStatus;
    private final JTextField txtCitizenship;
    private final JTextField txtPlaceOfBirth;
    private final JTextField txtAddress;
    private final JPasswordField txtPassword;

    // ── Constructor: receives field references from the parent panel ───────────
    public SubmitApplicationHandler(
            JTextField txtFirstName,   JTextField txtLastName,
            JTextField txtDOB,         JTextField txtEmail,
            JTextField txtContact,     JComboBox<String> comboSex,
            JComboBox<String> comboCivilStatus,
            JTextField txtCitizenship, JTextField txtPlaceOfBirth,
            JTextField txtAddress,     JPasswordField txtPassword) {

        this.txtFirstName      = txtFirstName;
        this.txtLastName       = txtLastName;
        this.txtDOB            = txtDOB;
        this.txtEmail          = txtEmail;
        this.txtContact        = txtContact;
        this.comboSex          = comboSex;
        this.comboCivilStatus  = comboCivilStatus;
        this.txtCitizenship    = txtCitizenship;
        this.txtPlaceOfBirth   = txtPlaceOfBirth;
        this.txtAddress        = txtAddress;
        this.txtPassword       = txtPassword;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // ── STEP 1: Extract text from UI fields ───────────────────────────────
        String firstName   = txtFirstName.getText().trim();
        String lastName    = txtLastName.getText().trim();
        String dob         = txtDOB.getText().trim();
        String email       = txtEmail.getText().trim();
        String contact     = txtContact.getText().trim();
        String sex         = (String) comboSex.getSelectedItem();
        String civilStatus = (String) comboCivilStatus.getSelectedItem();
        String citizenship = txtCitizenship.getText().trim();
        String pob         = txtPlaceOfBirth.getText().trim();
        String address     = txtAddress.getText().trim();
        String password    = new String(txtPassword.getPassword()).trim();

        // ── Basic validation ──────────────────────────────────────────────────
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(null,
                "Please fill in all required fields.",
                "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ── STEP 2: Instantiate the OOP Model ─────────────────────────────────
        Applicant newApplicant = new Applicant(
            firstName, lastName, dob,
            email, contact,
            sex, citizenship, civilStatus,
            pob, address
        );

        // Print the polymorphic summary to console (demonstrates getProfileSummary())
        System.out.println("Submitting: " + newApplicant.getProfileSummary());

        // ── STEP 3: Pass model to DAO via BackendBridge ───────────────────────
        BackendBridge backend = BackendBridge.getInstance();
        boolean saved = backend.submitApplication(newApplicant, password);

        // ── STEP 4: Update the UI based on result ─────────────────────────────
        if (saved) {
            JOptionPane.showMessageDialog(null,
                "Visa application for " + newApplicant.getFullName()
                    + " saved successfully!\nApplication ID: " + newApplicant.getApplicationId(),
                "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                "Failed to save application.\nEmail may already be registered.",
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
