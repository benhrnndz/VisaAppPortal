package com.visa.app.ui.panel;

import com.visa.app.DatabaseManager;
import com.visa.app.model.Child;
import com.visa.app.model.Document;
import com.visa.app.model.VisaApplication;
import com.visa.app.ui.screen.MainFrame;
import com.visa.app.ui.theme.Theme;
import com.visa.app.util.NameUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 *  PANEL: VisaApplicationWizard (3-Page Application Form)
 * ============================================================
 */
public class VisaApplicationWizard extends JPanel {
    private final MainFrame mainFrame;
    private final int currentUserId;
    private int editingAppId = -1;

    private CardLayout wizardCardLayout;
    private JPanel wizardCardPanel;
    private JLabel wizardTitleLabel;

    // Page 1: Personal Details
    private JTextField fullNameField;
    private JComboBox<String> sexCombo;
    private JTextField citizenshipField;
    private JComboBox<String> civilStatusCombo;
    private JTextField birthDateField;
    private JTextField birthPlaceField;
    private JTextField emailField;
    private JTextField contactField;
    private JTextField addressField;

    // Page 2: Family & Employment Details
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
        panel.add(new JLabel("<html>Full Name (First Name, Middle Name, Last Name) <font color='red'>*</font></html>"), gbc);
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
        pBirthDate.add(new JLabel("<html>Birth Date (YYYY/MM/DD) <font color='red'>*</font></html>"), BorderLayout.NORTH);
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
        pPassIssued.add(new JLabel("<html>Date Issued (YYYY/MM/DD) <font color='red'>*</font></html>"), BorderLayout.NORTH);
        passportIssuedField = Theme.createTextField(15);
        Theme.setupAutomaticDateField(passportIssuedField);
        pPassIssued.add(passportIssuedField, BorderLayout.CENTER);

        JPanel pPassExpiry = new JPanel(new BorderLayout(0, 5));
        pPassExpiry.setOpaque(false);
        pPassExpiry.add(new JLabel("<html>Validity Date (YYYY/MM/DD) <font color='red'>*</font></html>"), BorderLayout.NORTH);
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
        pDest.add(new JLabel("<html>Destination After Philippines <font color='red'>*</font></html>"), BorderLayout.NORTH);
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
        pDateApp.add(new JLabel("Date of Application (auto-filled)"), BorderLayout.NORTH);
        dateOfAppField = Theme.createTextField(15);
        dateOfAppField.setEditable(false);
        dateOfAppField.setFocusable(false);
        dateOfAppField.setBackground(Theme.BACKGROUND);
        pDateApp.add(dateOfAppField, BorderLayout.CENTER);

        colPanel3.add(pDateApp);
        colPanel3.add(new JPanel() {{ setOpaque(false); }});
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
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidPassportNo(passportNum)) {
            JOptionPane.showMessageDialog(this,
                    "Passport number must be 6\u201320 alphanumeric characters (no spaces).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportIssued)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Issue Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportExpiry)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Validity Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
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

        if (Theme.isFutureDate(bDate)) {
            JOptionPane.showMessageDialog(this,
                    "Birth Date cannot be in the future.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidContact(contact)) {
            JOptionPane.showMessageDialog(this,
                    "Contact number must be 7\u201315 digits (e.g. +63 917 123 4567 or 09171234567).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidName(name)) {
            JOptionPane.showMessageDialog(this,
                    "Full name must contain only letters, spaces, hyphens, or apostrophes (min. 2 characters).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
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
            docTableModel.addRow(new Object[] { doc.getDocumentType() });
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

        if (!Theme.isValidContact(contact)) {
            JOptionPane.showMessageDialog(this,
                    "Contact number must be 7\u201315 digits (e.g. +63 917 123 4567 or 09171234567).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidName(name)) {
            JOptionPane.showMessageDialog(this,
                    "Full name must contain only letters, spaces, hyphens, or apostrophes (min. 2 characters).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String passportNum = passportNumField.getText().trim();
        String passportAuth = passportAuthField.getText().trim();
        String passportIssued = passportIssuedField.getText().trim();
        String passportExpiry = passportExpiryField.getText().trim();

        if (passportNum.isEmpty() || passportAuth.isEmpty() || passportIssued.isEmpty() || passportExpiry.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required Passport fields on Page 2.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidPassportNo(passportNum)) {
            JOptionPane.showMessageDialog(this,
                    "Passport number must be 6\u201320 alphanumeric characters (no spaces).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportIssued)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Issue Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!Theme.isValidDateString(passportExpiry)) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid Passport Validity Date in YYYY/MM/DD format (valid month and days).",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
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
        if (appDate.isEmpty()) {
            appDate = java.time.LocalDate.now().toString().replace("-", "/");
            dateOfAppField.setText(appDate);
        }

        if (lenStr.isEmpty() || dest.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields on Page 3.", "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int lengthOfStay = 0;
        try {
            lengthOfStay = Integer.parseInt(lenStr);
            if (lengthOfStay <= 0) {
                JOptionPane.showMessageDialog(this, "Length of Stay Days must be a positive number.",
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for Length of Stay Days.",
                    "Validation Error", JOptionPane.ERROR_MESSAGE);
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
            // OOP Demo: runtime profile summary
            com.visa.app.model.Applicant oopApplicant = new com.visa.app.model.Applicant(
                    NameUtils.splitFirst(app.getFullName()),
                    NameUtils.splitLast(app.getFullName()),
                    app.getBirthDate(), app.getPlaceOfBirth(),
                    app.getSex(), app.getCitizenship(),
                    app.getContactNumber(), app.getHomeAddress(),
                    app.getCivilStatus());
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

        entryTypeCombo.setSelectedIndex(0);
        lengthOfStayField.setText("");
        portOfEntryCombo.setSelectedIndex(0);
        destinationAfterField.setText("");
        dateOfAppField.setText(java.time.LocalDate.now().toString().replace("-", "/"));
        purposeTypeCombo.setSelectedIndex(0);
        sponsorNameField.setText("");
        sponsorContactField.setText("");

        wizardCardLayout.show(wizardCardPanel, "PAGE_1");
    }
}
