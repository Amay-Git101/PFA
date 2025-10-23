package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import database.TransactionDAO;
import database.CategoryDAO;
import events.TransactionListener;
import events.TransactionEventManager;
import models.Transaction;
import java.util.List;

public class ExpensePanel extends JPanel implements TransactionListener {
    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;
    private DefaultTableModel tableModel;
    private JTable transactionTable;
    private JComboBox<String> typeComboBox;
    private JComboBox<String> categoryComboBox;
    private JTextField amountField;
    private JTextField dateField;
    private JTextArea notesArea;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public ExpensePanel() {
        transactionDAO = new TransactionDAO();
        categoryDAO = new CategoryDAO();
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadTransactions();
        
        // Subscribe to transaction events
        TransactionEventManager.getInstance().subscribe(this);
    }
    
    private void initComponents() {
        // Header
        JLabel headerLabel = new JLabel("💰 Expense Tracker");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        add(headerLabel, BorderLayout.NORTH);
        
        // Create main panel with form and table
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Transaction form
        JPanel formPanel = createTransactionForm();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 0, 20, 0);
        mainPanel.add(formPanel, gbc);
        
        // Transaction table
        JPanel tablePanel = createTransactionTable();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(tablePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createTransactionForm() {
        JPanel formPanel = new JPanel(new BorderLayout(10, 10));
        formPanel.setBackground(PANEL_COLOR);
        formPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel formTitle = new JLabel("Add New Transaction");
        formTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        formTitle.setForeground(TEXT_COLOR);
        formPanel.add(formTitle, BorderLayout.NORTH);
        
        // Main form panel with GridBagLayout
        JPanel mainForm = new JPanel(new GridBagLayout());
        mainForm.setBackground(PANEL_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Type Row
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 1;
        mainForm.add(createLabel("Type:"), gbc);
        
        typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        styleComboBox(typeComboBox);
        typeComboBox.addActionListener(e -> updateCategoryComboBox());
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.weightx = 0.25;
        mainForm.add(typeComboBox, gbc);
        
        gbc.gridx = 2; gbc.gridy = 0;
        gbc.weightx = 0;
        mainForm.add(createLabel("Category:"), gbc);
        
        categoryComboBox = new JComboBox<>();
        styleComboBox(categoryComboBox);
        updateCategoryComboBox();
        gbc.gridx = 3; gbc.gridy = 0;
        gbc.weightx = 0.25;
        mainForm.add(categoryComboBox, gbc);
        
        gbc.gridx = 4; gbc.gridy = 0;
        gbc.weightx = 0;
        mainForm.add(createLabel("Amount ($):"), gbc);
        
        amountField = createTextField();
        gbc.gridx = 5; gbc.gridy = 0;
        gbc.weightx = 0.2;
        mainForm.add(amountField, gbc);
        
        // Date Row
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.weightx = 0;
        mainForm.add(createLabel("Date:"), gbc);
        
        dateField = createTextField();
        dateField.setText(LocalDate.now().toString());
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.weightx = 0.25;
        mainForm.add(dateField, gbc);
        
        gbc.gridx = 2; gbc.gridy = 1;
        gbc.weightx = 0;
        mainForm.add(createLabel("Notes:"), gbc);
        
        notesArea = new JTextArea(2, 0);
        notesArea.setBackground(PANEL_COLOR.brighter());
        notesArea.setForeground(TEXT_COLOR);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        notesArea.setLineWrap(true);
        notesArea.setWrapStyleWord(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.gridx = 3; gbc.gridy = 1;
        gbc.gridwidth = 3;
        gbc.weightx = 0.5;
        mainForm.add(notesScroll, gbc);
        
        // Buttons Row
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 6;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(15, 8, 8, 8);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        
        JButton addButton = createStyledButton("➕ Add Transaction");
        addButton.addActionListener(this::addTransaction);
        buttonPanel.add(addButton);
        
        JButton clearButton = createStyledButton("🗑️ Clear Form");
        clearButton.setBackground(new Color(108, 117, 125));
        clearButton.addActionListener(e -> clearForm());
        buttonPanel.add(clearButton);
        
        mainForm.add(buttonPanel, gbc);
        
        formPanel.add(mainForm, BorderLayout.CENTER);
        
        return formPanel;
    }
    
    private JPanel createTransactionTable() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBackground(BACKGROUND_COLOR);
        tablePanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Table title and delete button
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(BACKGROUND_COLOR);
        
        JLabel tableTitle = new JLabel("📋 All Transactions");
        tableTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        tableTitle.setForeground(TEXT_COLOR);
        titlePanel.add(tableTitle, BorderLayout.WEST);
        
        JButton deleteButton = createStyledButton("🗑️ Delete Selected");
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.addActionListener(this::deleteTransaction);
        titlePanel.add(deleteButton, BorderLayout.EAST);
        
        tablePanel.add(titlePanel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"ID", "Date", "Type", "Category", "Amount", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 0) return Integer.class;
                return String.class;
            }
        };
        
        transactionTable = new JTable(tableModel);
        transactionTable.setBackground(PANEL_COLOR);
        transactionTable.setForeground(TEXT_COLOR);
        transactionTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        transactionTable.setRowHeight(25);
        transactionTable.setShowGrid(true);
        transactionTable.setGridColor(BORDER_COLOR);
        transactionTable.setSelectionBackground(ACCENT_COLOR.darker());
        transactionTable.setSelectionForeground(TEXT_COLOR);
        transactionTable.getTableHeader().setBackground(BACKGROUND_COLOR);
        transactionTable.getTableHeader().setForeground(TEXT_COLOR);
        transactionTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Hide ID column
        transactionTable.getColumnModel().getColumn(0).setMinWidth(0);
        transactionTable.getColumnModel().getColumn(0).setMaxWidth(0);
        transactionTable.getColumnModel().getColumn(0).setWidth(0);
        
        // Set column widths
        transactionTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(2).setPreferredWidth(70);
        transactionTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        transactionTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        transactionTable.getColumnModel().getColumn(5).setPreferredWidth(250);
        
        JScrollPane scrollPane = new JScrollPane(transactionTable);
        scrollPane.setBackground(PANEL_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setBackground(PANEL_COLOR.brighter());
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        return field;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_COLOR.brighter());
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }
    
    private void updateCategoryComboBox() {
        String selectedType = (String) typeComboBox.getSelectedItem();
        if (selectedType == null) return;
        
        // Get categories from database based on selected type
        List<String> categories = categoryDAO.getCategoriesByType(selectedType);
        
        // Update the combo box
        categoryComboBox.removeAllItems();
        for (String category : categories) {
            categoryComboBox.addItem(category);
        }
        
        // If no categories exist, show a default
        if (categories.isEmpty()) {
            categoryComboBox.addItem("No categories available");
        }
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    private void addTransaction(ActionEvent e) {
        try {
            // Validate amount
            double amount = Double.parseDouble(amountField.getText());
            if (amount <= 0) {
                JOptionPane.showMessageDialog(this, "Amount must be greater than $0.00", 
                    "Invalid Amount", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validate date format
            String date = dateField.getText().trim();
            if (date.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a date (YYYY-MM-DD format)", 
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                java.time.LocalDate.parse(date);
            } catch (java.time.format.DateTimeParseException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD (e.g., 2024-10-22)", 
                    "Invalid Date", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String type = (String) typeComboBox.getSelectedItem();
            String category = (String) categoryComboBox.getSelectedItem();
            String notes = notesArea.getText();
            
            Transaction transaction = new Transaction(type, category, amount, date, notes);
            
            if (transactionDAO.addTransaction(transaction)) {
                JOptionPane.showMessageDialog(this, "Transaction added successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                clearForm();
                loadTransactions();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add transaction.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid amount.", 
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteTransaction(ActionEvent e) {
        int selectedRow = transactionTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a transaction to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int id = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to delete this transaction?", 
            "Confirm Delete", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (transactionDAO.deleteTransaction(id)) {
                JOptionPane.showMessageDialog(this, "Transaction deleted successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadTransactions();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete transaction.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void clearForm() {
        typeComboBox.setSelectedIndex(0);
        categoryComboBox.setSelectedIndex(0);
        amountField.setText("");
        dateField.setText(LocalDate.now().toString());
        notesArea.setText("");
    }
    
    private void loadTransactions() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        tableModel.setRowCount(0);
        
        for (Transaction transaction : transactions) {
            Object[] row = {
                transaction.getId(),
                transaction.getDate(),
                transaction.getType(),
                transaction.getCategory(),
                String.format("$%.2f", transaction.getAmount()),
                transaction.getNotes()
            };
            tableModel.addRow(row);
        }
    }
    
    // Implementation of TransactionListener interface
    @Override
    public void onTransactionAdded(Transaction transaction) {
        // Refresh the table to show the new transaction
        SwingUtilities.invokeLater(this::loadTransactions);
    }
    
    @Override
    public void onTransactionDeleted(int transactionId) {
        // Refresh the table to reflect deletion
        SwingUtilities.invokeLater(this::loadTransactions);
    }
    
    @Override
    public void onTransactionUpdated(Transaction transaction) {
        // Refresh the table to reflect update
        SwingUtilities.invokeLater(this::loadTransactions);
    }
    
    @Override
    public void onTransactionsRefreshed() {
        // Refresh the table
        SwingUtilities.invokeLater(this::loadTransactions);
    }
}
