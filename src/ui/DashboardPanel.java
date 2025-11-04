package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import database.TransactionDAO;
import events.TransactionListener;
import events.TransactionEventManager;
import models.Transaction;
import backend.BudgetLogic;
import java.util.List;

public class DashboardPanel extends JPanel implements TransactionListener, Refreshable {
    private TransactionDAO transactionDAO;
    private BudgetLogic budgetLogic;
    private JLabel balanceLabel;
    private JLabel incomeLabel;
    private JLabel expenseLabel;
    private JLabel healthLabel;
    private DefaultTableModel tableModel;
    private JComboBox<String> durationFilterCombo;
    private JLabel transactionsTitleLabel;
    private Main mainFrame;
    
    // Theme colors from Main
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public DashboardPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        transactionDAO = new TransactionDAO();
        budgetLogic = new BudgetLogic();
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        loadData();
        
        // Subscribe to transaction events
        TransactionEventManager.getInstance().subscribe(this);
    }
    
    private void initComponents() {
        // Header
        JLabel headerLabel = new JLabel("📊 Dashboard");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        headerLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        add(headerLabel, BorderLayout.NORTH);
        
        // Create main content panel
        JPanel contentPanel = new JPanel(new GridBagLayout());
        contentPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Summary cards panel
        JPanel summaryPanel = createSummaryPanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(0, 0, 20, 0);
        contentPanel.add(summaryPanel, gbc);
        
        // Recent transactions panel
        JPanel recentPanel = createRecentTransactionsPanel();
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 0);
        contentPanel.add(recentPanel, gbc);
        
        add(contentPanel, BorderLayout.CENTER);
        
        // Bottom panel with duration filter and refresh button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        bottomPanel.setBackground(BACKGROUND_COLOR);
        
        // Duration filter
        JLabel filterLabel = new JLabel("Duration:");
        filterLabel.setForeground(TEXT_COLOR);
        bottomPanel.add(filterLabel);
        
        String[] durations = {"This Month", "Last 3 Months", "This Year", "All Time"};
        durationFilterCombo = new JComboBox<>(durations);
        durationFilterCombo.setBackground(PANEL_COLOR);
        durationFilterCombo.setForeground(TEXT_COLOR);
        durationFilterCombo.addActionListener(e -> {
            String selectedDuration = (String) durationFilterCombo.getSelectedItem();
            transactionsTitleLabel.setText("📋 Transactions (" + selectedDuration + ")");
            loadTransactionsTable();
        });
        bottomPanel.add(durationFilterCombo);
        
        // Refresh button
        JButton refreshButton = createStyledButton("🔄 Refresh");
        refreshButton.addActionListener(e -> loadData());
        bottomPanel.add(refreshButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 20, 0));
        summaryPanel.setBackground(BACKGROUND_COLOR);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Balance card
        JPanel balanceCard = createSummaryCard("💰 Current Balance", "$0.00", ACCENT_COLOR);
        balanceLabel = findValueLabel(balanceCard);
        summaryPanel.add(balanceCard);
        
        // Income card
        JPanel incomeCard = createSummaryCard("📈 Total Income", "$0.00", new Color(46, 160, 67));
        incomeLabel = findValueLabel(incomeCard);
        summaryPanel.add(incomeCard);
        
        // Expense card
        JPanel expenseCard = createSummaryCard("📉 Total Expenses", "$0.00", new Color(220, 53, 69));
        expenseLabel = findValueLabel(expenseCard);
        summaryPanel.add(expenseCard);
        
        // Financial Health card
        JPanel healthCard = createSummaryCard("🏥 Financial Health", "Good", new Color(255, 193, 7));
        healthLabel = findValueLabel(healthCard);
        summaryPanel.add(healthCard);
        
        return summaryPanel;
    }
    
    private JPanel createSummaryCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_COLOR.brighter());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        valueLabel.setName("valueLabel"); // Name it for easy identification
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        
        return card;
    }
    
    private JLabel findValueLabel(JPanel card) {
        for (Component comp : card.getComponents()) {
            if (comp instanceof JLabel && "valueLabel".equals(comp.getName())) {
                return (JLabel) comp;
            }
        }
        return null;
    }
    
    private JPanel createRecentTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        transactionsTitleLabel = new JLabel("📋 Transactions (This Month)");
        transactionsTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        transactionsTitleLabel.setForeground(TEXT_COLOR);
        transactionsTitleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        panel.add(transactionsTitleLabel, BorderLayout.NORTH);
        
        // Create table
        String[] columnNames = {"Date", "Type", "Category", "Amount", "Notes"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable table = new JTable(tableModel);
        table.setBackground(PANEL_COLOR);
        table.setForeground(TEXT_COLOR);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setRowHeight(28);
        table.setShowGrid(true);
        table.setGridColor(BORDER_COLOR);
        table.getTableHeader().setBackground(BACKGROUND_COLOR);
        table.getTableHeader().setForeground(TEXT_COLOR);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        
        // Add hover cursor
        table.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        
        // WHY: Apply row striping for better readability
        table.setDefaultRenderer(Object.class, new StripedRowRenderer());
        
        // Set column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(80);
        table.getColumnModel().getColumn(1).setPreferredWidth(60);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(3).setPreferredWidth(80);
        table.getColumnModel().getColumn(4).setPreferredWidth(200);
        
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(PANEL_COLOR);
        scrollPane.getViewport().setBackground(PANEL_COLOR);
        scrollPane.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        
        // Add hover effect
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
    
    private void loadData() {
        // Update summary cards
        double balance = transactionDAO.getBalance();
        double income = transactionDAO.getTotalIncome();
        double expenses = transactionDAO.getTotalExpenses();
        String health = budgetLogic.getFinancialHealth();
        
        balanceLabel.setText(String.format("$%.2f", balance));
        incomeLabel.setText(String.format("$%.2f", income));
        expenseLabel.setText(String.format("$%.2f", expenses));
        healthLabel.setText(health);
        
        // Update balance color based on value
        if (balance >= 0) {
            balanceLabel.setForeground(ACCENT_COLOR);
        } else {
            balanceLabel.setForeground(new Color(220, 53, 69));
        }
        
        // Load transactions table
        loadTransactionsTable();
    }
    
    private void loadTransactionsTable() {
        String selectedDuration = (String) durationFilterCombo.getSelectedItem();
        List<Transaction> transactions = transactionDAO.getTransactionsByDuration(selectedDuration);
        tableModel.setRowCount(0);
        
        for (Transaction transaction : transactions) {
            Object[] row = {
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
        // Refresh dashboard when a transaction is added
        SwingUtilities.invokeLater(this::loadData);
    }
    
    @Override
    public void onTransactionDeleted(int transactionId) {
        // Refresh dashboard when a transaction is deleted
        SwingUtilities.invokeLater(this::loadData);
    }
    
    @Override
    public void onTransactionUpdated(Transaction transaction) {
        // Refresh dashboard when a transaction is updated
        SwingUtilities.invokeLater(this::loadData);
    }
    
    @Override
    public void onTransactionsRefreshed() {
        // Refresh dashboard
        SwingUtilities.invokeLater(this::loadData);
    }
    
    @Override
    public void refreshData() {
        loadData();
    }
    
    /**
     * Custom renderer for row striping
     * WHY: Alternating row colors improve table readability
     */
    private class StripedRowRenderer extends javax.swing.table.DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(PANEL_COLOR);
                } else {
                    c.setBackground(PANEL_COLOR.brighter());
                }
            }
            
            // Right-align amount column (column 3)
            if (column == 3) {
                ((javax.swing.JLabel) c).setHorizontalAlignment(javax.swing.JLabel.RIGHT);
            } else {
                ((javax.swing.JLabel) c).setHorizontalAlignment(javax.swing.JLabel.LEFT);
            }
            
            return c;
        }
    }
}
