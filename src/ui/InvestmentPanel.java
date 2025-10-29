package ui;

import database.InvestmentDAO;
import database.TransactionDAO;
import models.Investment;
import models.Transaction;
import events.TransactionEventManager;
import events.TransactionListener;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Date;
import java.util.Calendar;

public class InvestmentPanel extends JPanel implements TransactionListener, Refreshable {
    private InvestmentDAO investmentDAO;
    private TransactionDAO transactionDAO;
    private JTable investmentTable;
    private DefaultTableModel tableModel;
    private JLabel frequencyLabel;
    private JComboBox<String> frequencyCombo;
    private JLabel paymentDayLabel;
    private JSpinner paymentDaySpinner;
    private JLabel maturityDateLabel;
    private JTextField maturityDateField;
    private JLabel interestRateLabel;
    private JTextField interestRateField;
    private JLabel actionLabel;
    private JComboBox<String> actionCombo;
    private Main mainFrame;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    
    public InvestmentPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        investmentDAO = new InvestmentDAO();
        transactionDAO = new TransactionDAO();
        setLayout(new BorderLayout());
        setBackground(BACKGROUND_COLOR);
        
        // Header
        JLabel headerLabel = new JLabel("📈 Investment Management");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(ACCENT_COLOR);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        add(headerLabel, BorderLayout.NORTH);
        
        // Main content panel with GridBagLayout
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Form Panel (Top part)
        JPanel formPanel = createFormPanel();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        centerPanel.add(formPanel, gbc);
        
        // Table Panel (Bottom part)
        JPanel tablePanel = createTablePanel();
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        centerPanel.add(tablePanel, gbc);
        
        add(new JScrollPane(centerPanel), BorderLayout.CENTER);
        
        // Subscribe to transaction events
        TransactionEventManager.getInstance().subscribe(this);
        
        // Load data
        reloadTableData();
    }
    
    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Investment Name
        JLabel nameLabel = createLabel("Investment Name:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);
        
        JTextField nameField = new JTextField(15);
        styleTextField(nameField);
        gbc.gridx = 1;
        formPanel.add(nameField, gbc);
        
        // Amount
        JLabel amountLabel = createLabel("Amount (₹):");
        gbc.gridx = 2;
        gbc.gridy = 0;
        formPanel.add(amountLabel, gbc);
        
        JTextField amountField = new JTextField(15);
        styleTextField(amountField);
        gbc.gridx = 3;
        formPanel.add(amountField, gbc);
        
        // Category
        JLabel categoryLabel = createLabel("Category:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(categoryLabel, gbc);
        
        String[] categories = {"SIP", "Stocks", "Fixed Deposit (FD)", "Real Estate", "Gold", "Other"};
        JComboBox<String> categoryCombo = new JComboBox<>(categories);
        styleComboBox(categoryCombo);
        gbc.gridx = 1;
        formPanel.add(categoryCombo, gbc);
        
        // Add listener for dynamic field visibility
        categoryCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selected = (String) categoryCombo.getSelectedItem();
                boolean isSIP = "SIP".equals(selected);
                boolean isFD = "Fixed Deposit (FD)".equals(selected);
                boolean isTradeableAsset = "Stocks".equals(selected) || "Gold".equals(selected) || "Real Estate".equals(selected);
                
                frequencyLabel.setVisible(isSIP);
                frequencyCombo.setVisible(isSIP);
                paymentDayLabel.setVisible(isSIP);
                paymentDaySpinner.setVisible(isSIP);
                
                maturityDateLabel.setVisible(isFD);
                maturityDateField.setVisible(isFD);
                interestRateLabel.setVisible(isFD);
                interestRateField.setVisible(isFD);
                
                actionLabel.setVisible(isTradeableAsset);
                actionCombo.setVisible(isTradeableAsset);
                
                formPanel.revalidate();
                formPanel.repaint();
            }
        });
        
        // Start Date
        JLabel startDateLabel = createLabel("Start Date (YYYY-MM-DD):");
        gbc.gridx = 2;
        gbc.gridy = 1;
        formPanel.add(startDateLabel, gbc);
        
        JTextField startDateField = new JTextField(15);
        styleTextField(startDateField);
        gbc.gridx = 3;
        formPanel.add(startDateField, gbc);
        
        // Frequency
        frequencyLabel = createLabel("Frequency:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(frequencyLabel, gbc);
        
        String[] frequencies = {"One-Time", "Monthly", "Quarterly", "Yearly"};
        frequencyCombo = new JComboBox<>(frequencies);
        styleComboBox(frequencyCombo);
        frequencyCombo.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedFrequency = (String) frequencyCombo.getSelectedItem();
                boolean isMonthly = "Monthly".equals(selectedFrequency);
                paymentDayLabel.setEnabled(isMonthly);
                paymentDaySpinner.setEnabled(isMonthly);
            }
        });
        gbc.gridx = 1;
        formPanel.add(frequencyCombo, gbc);
        
        // Payment Day (visible only for Monthly)
        paymentDayLabel = createLabel("Payment Day (1-28):");
        paymentDayLabel.setEnabled(false);
        gbc.gridx = 2;
        gbc.gridy = 2;
        formPanel.add(paymentDayLabel, gbc);
        
        SpinnerModel paymentDayModel = new SpinnerNumberModel(5, 1, 28, 1);
        paymentDaySpinner = new JSpinner(paymentDayModel);
        paymentDaySpinner.setEnabled(false);
        styleSpinner(paymentDaySpinner);
        gbc.gridx = 3;
        formPanel.add(paymentDaySpinner, gbc);
        
        // Maturity Date (for FD)
        maturityDateLabel = createLabel("Maturity Date (YYYY-MM-DD):");
        maturityDateLabel.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(maturityDateLabel, gbc);
        
        maturityDateField = new JTextField(15);
        styleTextField(maturityDateField);
        maturityDateField.setVisible(false);
        gbc.gridx = 1;
        formPanel.add(maturityDateField, gbc);
        
        // Interest Rate (for FD)
        interestRateLabel = createLabel("Interest Rate (%):");
        interestRateLabel.setVisible(false);
        gbc.gridx = 2;
        formPanel.add(interestRateLabel, gbc);
        
        interestRateField = new JTextField(15);
        styleTextField(interestRateField);
        interestRateField.setVisible(false);
        gbc.gridx = 3;
        formPanel.add(interestRateField, gbc);
        
        // Action (Buy/Sell) for Stocks, Gold, Real Estate
        actionLabel = createLabel("Action:");
        actionLabel.setVisible(false);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(actionLabel, gbc);
        
        String[] actions = {"Buy", "Sell"};
        actionCombo = new JComboBox<>(actions);
        styleComboBox(actionCombo);
        actionCombo.setVisible(false);
        gbc.gridx = 1;
        formPanel.add(actionCombo, gbc);
        
        // Add Investment Button
        JButton addButton = new JButton("💾 Add Investment");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        addButton.setForeground(TEXT_COLOR);
        addButton.setBackground(ACCENT_COLOR);
        addButton.setFocusPainted(false);
        addButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                String category = (String) categoryCombo.getSelectedItem();
                String amountStr = amountField.getText().trim();
                String startDate = startDateField.getText().trim();
                String frequency = (String) frequencyCombo.getSelectedItem();
                Integer paymentDay = frequency.equals("Monthly") ? (Integer) paymentDaySpinner.getValue() : null;
                String action = (String) actionCombo.getSelectedItem();
                
                // Validation
                if (name.isEmpty() || amountStr.isEmpty() || startDate.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                double amount;
                try {
                    amount = Double.parseDouble(amountStr);
                    if (amount <= 0) {
                        JOptionPane.showMessageDialog(this, "Amount must be greater than 0.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException nfe) {
                    JOptionPane.showMessageDialog(this, "Invalid amount format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Validate date format
                if (!startDate.matches("\\d{4}-\\d{2}-\\d{2}")) {
                    JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                // Validate optional fields based on category
                String maturityDate = null;
                Double interestRate = null;
                
                if ("Fixed Deposit (FD)".equals(category)) {
                    String maturityDateStr = maturityDateField.getText().trim();
                    String interestRateStr = interestRateField.getText().trim();
                    
                    if (!maturityDateStr.isEmpty()) {
                        if (!maturityDateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
                            JOptionPane.showMessageDialog(this, "Maturity date must be in YYYY-MM-DD format.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                        maturityDate = maturityDateStr;
                    }
                    
                    if (!interestRateStr.isEmpty()) {
                        try {
                            interestRate = Double.parseDouble(interestRateStr);
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(this, "Interest rate must be a valid number.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                            return;
                        }
                    }
                }
                
                // For Stocks, Gold, Real Estate - handle as transactions only
                boolean isTradeableAsset = "Stocks".equals(category) || "Gold".equals(category) || "Real Estate".equals(category);
                
                if (isTradeableAsset) {
                    // Create transaction only (not in investments table)
                    String transactionType = "Buy".equals(action) ? "Expense" : "Income";
                    String transactionCategory = category + "-" + action;
                    Transaction tradeTransaction = new Transaction(
                        transactionType,
                        transactionCategory,
                        amount,
                        startDate,
                        name,
                        "investment-" + action.toLowerCase()
                    );
                    
                    if (transactionDAO.addTransaction(tradeTransaction)) {
                        JOptionPane.showMessageDialog(this, action + " transaction recorded successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        nameField.setText("");
                        amountField.setText("");
                        startDateField.setText("");
                        actionCombo.setSelectedIndex(0);
                        reloadTableData();
                        mainFrame.refreshAllPanels();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to record transaction.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Regular investment (SIP, FD, etc.)
                    Investment investment = new Investment(name, category, amount, startDate, frequency, paymentDay, maturityDate, interestRate);
                    if (investmentDAO.addInvestment(investment)) {
                        // For SIPs, don't create a transaction here - processRecurringSIPs will handle it
                        // For other categories, create appropriate transaction
                        if (!"SIP".equalsIgnoreCase(category)) {
                            String transactionCategory = "Investment-" + category;
                            Transaction investmentTransaction = new Transaction(
                                "Expense",
                                transactionCategory,
                                amount,
                                startDate,
                                name,
                                "investment"
                            );
                            transactionDAO.addTransaction(investmentTransaction);
                        }
                        
                        JOptionPane.showMessageDialog(this, "Investment added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        nameField.setText("");
                        amountField.setText("");
                        startDateField.setText("");
                        maturityDateField.setText("");
                        interestRateField.setText("");
                        categoryCombo.setSelectedIndex(0);
                        frequencyCombo.setSelectedIndex(0);
                        paymentDaySpinner.setValue(5);
                        reloadTableData();
                        // Refresh all panels
                        mainFrame.refreshAllPanels();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add investment.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        });
        
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 4;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(addButton, gbc);
        
        return formPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(PANEL_COLOR);
        tablePanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Table Model
        String[] columnNames = {"Name", "Category", "Amount (₹)", "Start Date", "Frequency", "Payment Day"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        investmentTable = new JTable(tableModel);
        investmentTable.setBackground(PANEL_COLOR);
        investmentTable.setForeground(TEXT_COLOR);
        investmentTable.setSelectionBackground(ACCENT_COLOR);
        investmentTable.setSelectionForeground(BACKGROUND_COLOR);
        investmentTable.setGridColor(BORDER_COLOR);
        investmentTable.setRowHeight(25);
        investmentTable.getTableHeader().setBackground(BACKGROUND_COLOR);
        investmentTable.getTableHeader().setForeground(TEXT_COLOR);
        investmentTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JScrollPane scrollPane = new JScrollPane(investmentTable);
        scrollPane.setBackground(PANEL_COLOR);
        scrollPane.setForeground(TEXT_COLOR);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add Process SIPs and Delete buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        buttonPanel.setBackground(PANEL_COLOR);
        
        JButton processSIPsButton = new JButton("Process Recurring SIPs");
        processSIPsButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        processSIPsButton.setForeground(TEXT_COLOR);
        processSIPsButton.setBackground(ACCENT_COLOR);
        processSIPsButton.setFocusPainted(false);
        processSIPsButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        processSIPsButton.addActionListener(e -> processRecurringSIPs());
        buttonPanel.add(processSIPsButton);
        
        JButton deleteButton = new JButton("🗑️ Delete Selected");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        deleteButton.setForeground(TEXT_COLOR);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setFocusPainted(false);
        deleteButton.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        deleteButton.addActionListener(e -> deleteSelectedInvestment());
        buttonPanel.add(deleteButton);
        
        tablePanel.add(buttonPanel, BorderLayout.SOUTH);
        
        return tablePanel;
    }
    
    private void reloadTableData() {
        tableModel.setRowCount(0);
        List<Investment> investments = investmentDAO.getAllInvestments();
        
        for (Investment inv : investments) {
            Object[] row = {
                inv.getName(),
                inv.getCategory(),
                String.format("₹%.2f", inv.getAmount()),
                inv.getStartDate(),
                inv.getFrequency(),
                inv.getDayOfMonth() != null ? inv.getDayOfMonth() : "-"
            };
            tableModel.addRow(row);
        }
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
    
    private void styleTextField(JTextField textField) {
        textField.setBackground(BACKGROUND_COLOR);
        textField.setForeground(TEXT_COLOR);
        textField.setCaretColor(TEXT_COLOR);
        textField.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }
    
    private void styleComboBox(JComboBox<?> comboBox) {
        comboBox.setBackground(BACKGROUND_COLOR);
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
    }
    
    private void styleSpinner(JSpinner spinner) {
        spinner.setBackground(BACKGROUND_COLOR);
        spinner.setForeground(TEXT_COLOR);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor) editor).getTextField().setBackground(BACKGROUND_COLOR);
            ((JSpinner.DefaultEditor) editor).getTextField().setForeground(TEXT_COLOR);
        }
    }
    
    private void deleteSelectedInvestment() {
        int selectedRow = investmentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an investment to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Get ID and name from table model
        int investmentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String investmentName = (String) tableModel.getValueAt(selectedRow, 1);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete this investment?\nThis will also delete all associated transactions.",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (investmentDAO.deleteInvestment(investmentId)) {
                transactionDAO.deleteTransactionsByName(investmentName);
                JOptionPane.showMessageDialog(this, "Investment and associated transactions deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                reloadTableData();
                mainFrame.refreshAllPanels();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete investment.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void processRecurringSIPs() {
        try {
            Calendar today = Calendar.getInstance();
            int currentDay = today.get(Calendar.DAY_OF_MONTH);
            int currentMonth = today.get(Calendar.MONTH) + 1; // 1-12
            int currentYear = today.get(Calendar.YEAR);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
            String currentDate = sdf.format(today.getTime());
            String currentYearMonth = monthFormat.format(today.getTime());
            
            List<Investment> allInvestments = investmentDAO.getAllInvestments();
            int processedCount = 0;
            
            for (Investment sip : allInvestments) {
                // Check if it's a SIP
                if (!"SIP".equalsIgnoreCase(sip.getCategory())) {
                    continue;
                }
                
                String frequency = sip.getFrequency();
                if (frequency == null) {
                    continue;
                }
                
                // Check: Is current date after SIP's start date?
                if (currentDate.compareTo(sip.getStartDate()) < 0) {
                    continue;
                }
                
                boolean shouldProcess = false;
                
                switch (frequency.toLowerCase()) {
                    case "one-time":
                        // Process only if today is on or after start date
                        shouldProcess = !transactionDAO.hasTransactionForMonth(sip.getName(), currentYearMonth);
                        break;
                        
                    case "monthly":
                        // Check if current day >= SIP's payment day
                        Integer monthlyPaymentDay = sip.getDayOfMonth();
                        if (monthlyPaymentDay != null && currentDay >= monthlyPaymentDay) {
                            shouldProcess = !transactionDAO.hasTransactionForMonth(sip.getName(), currentYearMonth);
                        }
                        break;
                        
                    case "quarterly":
                        // Process in months 1, 4, 7, 10 (Jan, Apr, Jul, Oct)
                        if ((currentMonth == 1 || currentMonth == 4 || currentMonth == 7 || currentMonth == 10)) {
                            Integer quarterlyPaymentDay = sip.getDayOfMonth();
                            if (quarterlyPaymentDay == null || currentDay >= quarterlyPaymentDay) {
                                shouldProcess = !transactionDAO.hasTransactionForMonth(sip.getName(), currentYearMonth);
                            }
                        }
                        break;
                        
                    case "yearly":
                        // Parse start date to get month and day
                        try {
                            String[] parts = sip.getStartDate().split("-");
                            int startMonth = Integer.parseInt(parts[1]);
                            int startDay = Integer.parseInt(parts[2]);
                            
                            // Process if current month and day match start date's month and day
                            if (currentMonth == startMonth && currentDay >= startDay) {
                                shouldProcess = !transactionDAO.hasTransactionForMonth(sip.getName(), currentYearMonth);
                            }
                        } catch (Exception ex) {
                            System.err.println("Error parsing start date for yearly SIP: " + ex.getMessage());
                        }
                        break;
                }
                
                if (shouldProcess) {
                    // Create recurring payment
                    Transaction recurringPayment = new Transaction(
                        "Expense",
                        "Investment-SIP",
                        sip.getAmount(),
                        currentDate,
                        sip.getName(),
                        "investment-recurring"
                    );
                    
                    if (transactionDAO.addTransaction(recurringPayment)) {
                        processedCount++;
                    }
                }
            }
            
            if (processedCount > 0) {
                mainFrame.refreshAllPanels();
                JOptionPane.showMessageDialog(this, "Recurring SIPs processed.\nTotal payments: " + processedCount, "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "No SIPs eligible for processing today.", "Info", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error processing SIPs: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
    
    @Override
    public void onTransactionAdded(models.Transaction transaction) {
        reloadTableData();
    }
    
    @Override
    public void onTransactionDeleted(int transactionId) {
        reloadTableData();
    }
    
    @Override
    public void onTransactionUpdated(models.Transaction transaction) {
        reloadTableData();
    }
    
    @Override
    public void onTransactionsRefreshed() {
        reloadTableData();
    }
    
    @Override
    public void refreshData() {
        reloadTableData();
    }
}
