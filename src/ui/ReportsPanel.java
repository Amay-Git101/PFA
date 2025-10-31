package ui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.YearMonth;
import java.util.*;
import database.TransactionDAO;
import database.CategoryDAO;
import models.Transaction;

public class ReportsPanel extends JPanel {
    private TransactionDAO transactionDAO;
    private CategoryDAO categoryDAO;
    private JTabbedPane tabbedPane;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public ReportsPanel() {
        transactionDAO = new TransactionDAO();
        categoryDAO = new CategoryDAO();
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // Header panel with title and refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel headerLabel = new JLabel("📈 Financial Reports");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Refresh button
        JButton refreshButton = createStyledButton("🔄 Refresh Reports");
        refreshButton.addActionListener(e -> refreshAllReports());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Create tabbed pane for different reports
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(PANEL_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Expense Breakdown (Pie Chart)
        JPanel expenseBreakdownPanel = createExpenseBreakdownReport();
        tabbedPane.addTab("Expense Breakdown", expenseBreakdownPanel);
        
        // Income vs Expense (Bar Chart)
        JPanel incomeVsExpensePanel = createIncomeVsExpenseReport();
        tabbedPane.addTab("Income vs Expense", incomeVsExpensePanel);
        
        // Category Summary
        JPanel categorySummaryPanel = createCategorySummaryReport();
        tabbedPane.addTab("Category Summary", categorySummaryPanel);
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    private JPanel createExpenseBreakdownReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Get expenses by category
        Map<String, Double> expensesByCategory = getExpensesByCategory();
        
        if (expensesByCategory.isEmpty()) {
            JLabel noDataLabel = new JLabel("No expense data available");
            noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noDataLabel.setForeground(TEXT_COLOR);
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(noDataLabel, BorderLayout.CENTER);
        } else {
            PieChartPanel pieChart = new PieChartPanel("Expense Breakdown by Category", expensesByCategory);
            panel.add(pieChart, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private JPanel createIncomeVsExpenseReport() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        Map<String, Double> data = new LinkedHashMap<>();
        double totalIncome = transactionDAO.getTotalIncome();
        double totalExpense = transactionDAO.getTotalExpenses();
        
        data.put("Income", totalIncome);
        data.put("Expense", totalExpense);
        
        BarChartPanel barChart = new BarChartPanel("Income vs Expense Comparison", data, "Amount ($)");
        panel.add(barChart, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createCategorySummaryReport() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Summary statistics
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setBackground(BACKGROUND_COLOR);
        
        double totalIncome = transactionDAO.getTotalIncome();
        double totalExpense = transactionDAO.getTotalExpenses();
        double balance = totalIncome - totalExpense;
        
        statsPanel.add(createStatCard("Total Income", String.format("$%.2f", totalIncome), new Color(46, 160, 67)));
        statsPanel.add(createStatCard("Total Expenses", String.format("$%.2f", totalExpense), new Color(220, 53, 69)));
        statsPanel.add(createStatCard("Balance", String.format("$%.2f", balance), 
            balance >= 0 ? ACCENT_COLOR : new Color(220, 53, 69)));
        
        panel.add(statsPanel, BorderLayout.NORTH);
        
        // Category breakdown bar chart
        Map<String, Double> expensesByCategory = getExpensesByCategory();
        if (!expensesByCategory.isEmpty()) {
            BarChartPanel barChart = new BarChartPanel("Top Expenses by Category", expensesByCategory, "Amount ($)");
            panel.add(barChart, BorderLayout.CENTER);
        } else {
            JLabel noDataLabel = new JLabel("No expense data available");
            noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noDataLabel.setForeground(TEXT_COLOR);
            noDataLabel.setHorizontalAlignment(SwingConstants.CENTER);
            panel.add(noDataLabel, BorderLayout.CENTER);
        }
        
        return panel;
    }
    
    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(PANEL_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(TEXT_COLOR.brighter());
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(accentColor);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(valueLabel);
        
        return card;
    }
    
    private Map<String, Double> getExpensesByCategory() {
        Map<String, Double> expensesByCategory = new java.util.HashMap<>();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();
        
        for (Transaction t : allTransactions) {
            if ("Expense".equals(t.getType())) {
                expensesByCategory.put(
                    t.getCategory(),
                    expensesByCategory.getOrDefault(t.getCategory(), 0.0) + t.getAmount()
                );
            }
        }
        
        // Sort by expense amount (highest first) and return as LinkedHashMap to maintain order
        Map<String, Double> sortedExpenses = expensesByCategory.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .collect(java.util.stream.Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));
        
        return sortedExpenses;
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
    
    private void refreshAllReports() {
        // Remove all tabs
        tabbedPane.removeAll();
        
        // Recreate all report panels with fresh data
        JPanel expenseBreakdownPanel = createExpenseBreakdownReport();
        tabbedPane.addTab("Expense Breakdown", expenseBreakdownPanel);
        
        JPanel incomeVsExpensePanel = createIncomeVsExpenseReport();
        tabbedPane.addTab("Income vs Expense", incomeVsExpensePanel);
        
        JPanel categorySummaryPanel = createCategorySummaryReport();
        tabbedPane.addTab("Category Summary", categorySummaryPanel);
        
        // Refresh the UI
        tabbedPane.revalidate();
        tabbedPane.repaint();
    }
}
