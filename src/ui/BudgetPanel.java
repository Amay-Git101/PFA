package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import database.TransactionDAO;
import database.CategoryDAO;
import database.CategoryBudgetDAO;
import models.BudgetCategory;
import models.Transaction;
import backend.BudgetLogic;
import java.util.List;
import java.util.Map;

public class BudgetPanel extends JPanel implements Refreshable {
    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;
    private CategoryBudgetDAO categoryBudgetDAO;
    private BudgetLogic budgetLogic;
    private Main mainFrame;
    private JTextField incomeField;
    private JTextField limitField;
    private JProgressBar budgetProgressBar;
    private JLabel statusLabel;
    private JLabel remainingLabel;
    private DefaultTableModel categoryBudgetTableModel;
    private JTable categoryBudgetTable;
    private JComboBox<String> monthComboBox;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public BudgetPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        transactionDAO = new TransactionDAO();
        categoryDAO = new CategoryDAO();
        categoryBudgetDAO = new CategoryBudgetDAO();
        budgetLogic = new BudgetLogic();
        
        // CRITICAL: Set layout FIRST before any component operations
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadBudgetData();
    }
    
    private void initComponents() {
        // Header
        JLabel headerLabel = new JLabel("📊 Budget Planner");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        add(headerLabel, BorderLayout.NORTH);
        
        // Create tabbed interface
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(PANEL_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Overall Budget Tab
        JPanel overallPanel = new JPanel(new GridBagLayout());
        overallPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Budget settings panel
        JPanel settingsPanel = createBudgetSettingsPanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 20, 20, 20);
        overallPanel.add(settingsPanel, gbc);
        
        // Progress panel
        JPanel progressPanel = createProgressPanel();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 20, 20, 20);
        overallPanel.add(progressPanel, gbc);
        
        tabbedPane.addTab("Overall Budget", overallPanel);
        
        // Category Budget Tab
        JPanel categoryBudgetPanel = createCategoryBudgetPanel();
        tabbedPane.addTab("Category Budgets", categoryBudgetPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createBudgetSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Title
        JLabel titleLabel = new JLabel("Budget Configuration");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 4;
        gbc.insets = new Insets(0, 0, 20, 0);
        panel.add(titleLabel, gbc);
        
        // Monthly Income
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; gbc.gridy = 1;
        panel.add(createLabel("Monthly Income:"), gbc);
        
        incomeField = createTextField();
        incomeField.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 1; gbc.gridy = 1;
        panel.add(incomeField, gbc);
        
        // Budget Limit
        gbc.gridx = 2; gbc.gridy = 1;
        panel.add(createLabel("Budget Limit:"), gbc);
        
        limitField = createTextField();
        limitField.setPreferredSize(new Dimension(150, 30));
        gbc.gridx = 3; gbc.gridy = 1;
        panel.add(limitField, gbc);
        
        // Save button
        JButton saveButton = createStyledButton("💾 Save Budget");
        saveButton.addActionListener(e -> saveBudget());
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 10, 10, 10);
        panel.add(saveButton, gbc);
        
        // Refresh button
        JButton refreshButton = createStyledButton("🔄 Refresh Data");
        refreshButton.addActionListener(e -> loadBudgetData());
        gbc.gridx = 2; gbc.gridy = 2;
        gbc.gridwidth = 2;
        panel.add(refreshButton, gbc);
        
        return panel;
    }
    
    private JPanel createProgressPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        
        // Title
        JLabel titleLabel = new JLabel("Budget Usage");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        panel.add(titleLabel, gbc);
        
        // Progress bar
        budgetProgressBar = new JProgressBar(0, 100);
        budgetProgressBar.setStringPainted(true);
        budgetProgressBar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        budgetProgressBar.setForeground(ACCENT_COLOR);
        budgetProgressBar.setBackground(BACKGROUND_COLOR);
        budgetProgressBar.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        budgetProgressBar.setPreferredSize(new Dimension(400, 40));
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(budgetProgressBar, gbc);
        
        // Status label
        statusLabel = new JLabel("Status: Loading...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        statusLabel.setForeground(ACCENT_COLOR);
        gbc.gridx = 0; gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 15, 10, 15);
        panel.add(statusLabel, gbc);
        
        // Remaining budget
        remainingLabel = new JLabel("Remaining: $0.00");
        remainingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        remainingLabel.setForeground(TEXT_COLOR);
        gbc.gridx = 0; gbc.gridy = 3;
        gbc.insets = new Insets(10, 15, 15, 15);
        panel.add(remainingLabel, gbc);
        
        return panel;
    }
    
    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        return label;
    }
    
    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setBackground(PANEL_COLOR.brighter());
        field.setForeground(TEXT_COLOR);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        return field;
    }
    
    private void styleComboBox(JComboBox<String> comboBox) {
        comboBox.setBackground(PANEL_COLOR.brighter());
        comboBox.setForeground(TEXT_COLOR);
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        comboBox.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    }
    
    private JPanel createCategoryBudgetPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel titleLabel = new JLabel("Per-Category Budget Tracking");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(TEXT_COLOR);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        // Month selector
        JPanel monthPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        monthPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel monthLabel = new JLabel("Month:");
        monthLabel.setForeground(TEXT_COLOR);
        monthPanel.add(monthLabel);
        
        monthComboBox = new JComboBox<>();
        monthComboBox.addItem(CategoryBudgetDAO.getPreviousMonth());
        monthComboBox.addItem(CategoryBudgetDAO.getCurrentMonth());
        monthComboBox.addItem(CategoryBudgetDAO.getNextMonth());
        monthComboBox.setSelectedItem(CategoryBudgetDAO.getCurrentMonth());
        monthComboBox.addActionListener(e -> loadCategoryBudgets());
        styleComboBox(monthComboBox);
        monthPanel.add(monthComboBox);
        
        headerPanel.add(monthPanel, BorderLayout.EAST);
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Category", "Budget Limit", "Spent", "Remaining", "Usage %"};
        categoryBudgetTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only budget limit column is editable
            }
        };
        
        categoryBudgetTable = new JTable(categoryBudgetTableModel);
        categoryBudgetTable.setBackground(PANEL_COLOR);
        categoryBudgetTable.setForeground(TEXT_COLOR);
        categoryBudgetTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        categoryBudgetTable.setRowHeight(25);
        categoryBudgetTable.setShowGrid(true);
        categoryBudgetTable.setGridColor(BORDER_COLOR);
        categoryBudgetTable.getTableHeader().setBackground(BACKGROUND_COLOR);
        categoryBudgetTable.getTableHeader().setForeground(TEXT_COLOR);
        categoryBudgetTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        // Set column widths
        categoryBudgetTable.getColumnModel().getColumn(0).setPreferredWidth(150);
        categoryBudgetTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        categoryBudgetTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        categoryBudgetTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        categoryBudgetTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        
        JScrollPane scrollPane = new JScrollPane(categoryBudgetTable);
        scrollPane.setBackground(PANEL_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonsPanel.setBackground(BACKGROUND_COLOR);
        
        JButton saveButton = createStyledButton("💾 Save Category Budgets");
        saveButton.addActionListener(e -> saveCategoryBudgets());
        buttonsPanel.add(saveButton);
        
        JButton refreshButton = createStyledButton("🔄 Refresh");
        refreshButton.addActionListener(e -> loadCategoryBudgets());
        buttonsPanel.add(refreshButton);
        
        panel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        return button;
    }
    
    private void saveBudget() {
        try {
            double income = Double.parseDouble(incomeField.getText());
            double limit = Double.parseDouble(limitField.getText());
            
            // Validate income
            if (income < 0) {
                JOptionPane.showMessageDialog(this, "Monthly income cannot be negative.", 
                    "Invalid Income", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Validate budget limit
            if (limit < 0) {
                JOptionPane.showMessageDialog(this, "Budget limit cannot be negative.", 
                    "Invalid Budget Limit", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Warn if both income and limit are zero
            if (income == 0 && limit == 0) {
                JOptionPane.showMessageDialog(this, "Please set either Monthly Income or Budget Limit (or both) to track your budget.", 
                    "Budget Configuration", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            if (transactionDAO.updateBudget(income, limit)) {
                JOptionPane.showMessageDialog(this, "Budget saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
                loadBudgetData();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save budget.", 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", 
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void loadBudgetData() {
        double[] budgetInfo = transactionDAO.getBudgetInfo();
        double monthlyIncome = budgetInfo[0];
        double budgetLimit = budgetInfo[1];
        
        incomeField.setText(String.format("%.2f", monthlyIncome));
        limitField.setText(String.format("%.2f", budgetLimit));
        
        // Get expense data
        double totalExpenses = transactionDAO.getTotalExpenses();
        
        // Calculate percentage
        double percentage = budgetLogic.calculateBudgetUsagePercentage();
        
        // Cap progress bar at 100 for display (but allow values > 100%)
        int displayPercentage = Math.min(100, (int) percentage);
        budgetProgressBar.setValue(displayPercentage);
        budgetProgressBar.setString(String.format("%.1f%% Used", percentage));
        
        // Set progress bar color based on usage
        if (percentage > 100) {
            budgetProgressBar.setForeground(new Color(220, 53, 69)); // Red - Over budget
        } else if (percentage > 80) {
            budgetProgressBar.setForeground(new Color(255, 193, 7)); // Yellow - Warning
        } else if (percentage > 50) {
            budgetProgressBar.setForeground(new Color(255, 152, 0)); // Orange - Moderate
        } else {
            budgetProgressBar.setForeground(ACCENT_COLOR); // Green - Good
        }
        
        // Update status label with more details
        String status = budgetLogic.getBudgetStatus();
        double remaining = budgetLogic.getRemainingBudget();
        String detailedStatus = String.format("%s | Spent: $%.2f | Limit: $%.2f", 
            status, totalExpenses, (budgetLimit > 0 ? budgetLimit : monthlyIncome));
        statusLabel.setText(detailedStatus);
        
        // Update remaining budget label
        remainingLabel.setText(String.format("Remaining: $%.2f", remaining));
        
        // Set status color based on budget percentage
        if (percentage > 100) {
            statusLabel.setForeground(new Color(220, 53, 69)); // Red
            remainingLabel.setForeground(new Color(220, 53, 69));
        } else if (percentage > 80) {
            statusLabel.setForeground(new Color(255, 193, 7)); // Yellow
            remainingLabel.setForeground(new Color(255, 193, 7));
        } else {
            statusLabel.setForeground(ACCENT_COLOR); // Green
            remainingLabel.setForeground(ACCENT_COLOR);
        }
    }
    
    private void loadCategoryBudgets() {
        String selectedMonth = (String) monthComboBox.getSelectedItem();
        if (selectedMonth == null) selectedMonth = CategoryBudgetDAO.getCurrentMonth();
        
        // Make final for lambda expression
        final String finalSelectedMonth = selectedMonth;
        
        // Get all categories
        List<String> expenseCategories = categoryDAO.getCategoriesByType("Expense");
        
        // Clear the table
        categoryBudgetTableModel.setRowCount(0);
        
        // Get budget map for the month
        Map<Integer, Double> budgetMap = categoryBudgetDAO.getBudgetMapByMonth(selectedMonth);
        
        // Get all transactions and filter by month and type
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();
        
        // Populate table with each category
        for (String categoryName : expenseCategories) {
            int categoryId = categoryDAO.getCategoryId(categoryName);
            double budgetLimit = budgetMap.getOrDefault(categoryId, 0.0);
            
            // Calculate spent for this category in the selected month
            double spent = allTransactions.stream()
                .filter(t -> "Expense".equals(t.getType()))
                .filter(t -> categoryName.equals(t.getCategory()))
                .filter(t -> isInSelectedMonth(t.getDate(), finalSelectedMonth))
                .mapToDouble(Transaction::getAmount)
                .sum();
            
            double remaining = budgetLimit - spent;
            double usagePercent = (budgetLimit > 0) ? (spent / budgetLimit) * 100 : 0;
            Object[] row = {
                categoryName,
                String.format("$%.2f", budgetLimit),
                String.format("$%.2f", spent),
                String.format("$%.2f", remaining),
                String.format("%.1f%%", usagePercent)
            };
            categoryBudgetTableModel.addRow(row);
        }
    }
    
    private boolean isInSelectedMonth(String transactionDate, String selectedMonth) {
        // transactionDate format: "YYYY-MM-DD"
        // selectedMonth format: "YYYY-MM" (e.g., "2025-01")
        if (transactionDate == null || selectedMonth == null) return false;
        return transactionDate.startsWith(selectedMonth);
    }
    
    private void saveCategoryBudgets() {
        try {
            int rowCount = categoryBudgetTable.getRowCount();
            for (int row = 0; row < rowCount; row++) {
                String categoryName = (String) categoryBudgetTableModel.getValueAt(row, 0);
                String budgetLimitStr = ((String) categoryBudgetTableModel.getValueAt(row, 1)).replaceAll("[^\\d.]", "");
                
                if (budgetLimitStr.isEmpty()) budgetLimitStr = "0";
                double budgetLimit = Double.parseDouble(budgetLimitStr);
                
                if (budgetLimit < 0) {
                    JOptionPane.showMessageDialog(this, "Budget limits cannot be negative.", 
                        "Invalid Budget", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                int categoryId = categoryDAO.getCategoryId(categoryName);
                String selectedMonth = (String) monthComboBox.getSelectedItem();
                if (selectedMonth == null) selectedMonth = CategoryBudgetDAO.getCurrentMonth();
                
                categoryBudgetDAO.setBudget(categoryId, selectedMonth, budgetLimit);
            }
            
            JOptionPane.showMessageDialog(this, "Category budgets saved successfully!", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            loadCategoryBudgets();
            
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter valid budget amounts.", 
                "Invalid Input", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    @Override
    public void refreshData() {
        loadBudgetData();
        loadCategoryBudgets();
    }
}
