package service;

import database.TransactionDAO;
import database.InvestmentDAO;
import database.CategoryDAO;
import models.Transaction;
import models.Investment;
import backend.BudgetLogic;
import backend.SettingsManager;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Summarizes user financial data into compact JSON for AI processing.
 * WHY: Keeps AI context under token limits while providing relevant data
 */
public class SummarizerService {
    private final TransactionDAO transactionDAO;
    private final InvestmentDAO investmentDAO;
    private final CategoryDAO categoryDAO;
    private final BudgetLogic budgetLogic;
    
    public SummarizerService() {
        this.transactionDAO = new TransactionDAO();
        this.investmentDAO = new InvestmentDAO();
        this.categoryDAO = new CategoryDAO();
        this.budgetLogic = new BudgetLogic();
    }
    
    /**
     * Generate complete financial summary as JSON
     * WHY: Provides AI with context about user's financial state
     */
    public JSONObject summarizeUserData() {
        JSONObject summary = new JSONObject();
        
        // Basic user info
        summary.put("currency", SettingsManager.getCurrencyCode());
        summary.put("currencySymbol", SettingsManager.getCurrencySymbol());
        summary.put("asOf", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        
        // Budget information
        JSONObject budgetInfo = summarizeBudget();
        summary.put("budget", budgetInfo);
        
        // Recent transactions (last 10)
        JSONArray recentTransactions = summarizeRecentTransactions(10);
        summary.put("recentTransactions", recentTransactions);
        
        // Monthly totals
        JSONObject monthlyTotals = summarizeMonthlyTotals();
        summary.put("monthlyTotals", monthlyTotals);
        
        // Top expense categories
        JSONArray topCategories = summarizeTopCategories(5);
        summary.put("topExpenseCategories", topCategories);
        
        // Investment summary (if any)
        JSONArray investments = summarizeInvestments();
        if (investments.length() > 0) {
            summary.put("activeInvestments", investments);
        }
        
        return summary;
    }
    
    /**
     * Summarize budget status
     * WHY: Critical for AI to assess financial health
     */
    private JSONObject summarizeBudget() {
        JSONObject budget = new JSONObject();
        
        double[] budgetInfo = transactionDAO.getBudgetInfo();
        double monthlyIncome = budgetInfo[0];
        double budgetLimit = budgetInfo[1];
        double totalExpenses = transactionDAO.getTotalExpenses();
        
        double usagePercent = budgetLogic.calculateBudgetUsagePercentage();
        double remaining = budgetLogic.getRemainingBudget();
        String status = budgetLogic.getBudgetStatus();
        
        budget.put("monthlyIncome", monthlyIncome);
        budget.put("budgetLimit", budgetLimit);
        budget.put("totalExpenses", totalExpenses);
        budget.put("usagePercent", Math.round(usagePercent * 10) / 10.0);
        budget.put("remaining", remaining);
        budget.put("status", status);
        
        return budget;
    }
    
    /**
     * Get recent transactions for context
     * WHY: Shows recent spending patterns
     */
    private JSONArray summarizeRecentTransactions(int limit) {
        JSONArray transactions = new JSONArray();
        List<Transaction> allTransactions = transactionDAO.getAllTransactions();
        
        // Take most recent 'limit' transactions
        int count = Math.min(limit, allTransactions.size());
        for (int i = 0; i < count; i++) {
            Transaction t = allTransactions.get(i);
            JSONObject txn = new JSONObject();
            txn.put("date", t.getDate());
            txn.put("type", t.getType());
            txn.put("category", t.getCategory());
            txn.put("amount", Math.round(t.getAmount() * 100) / 100.0);
            // Note: deliberately NOT including full notes for privacy
            transactions.put(txn);
        }
        
        return transactions;
    }
    
    /**
     * Calculate monthly income/expense totals
     * WHY: Provides month-over-month context
     */
    private JSONObject summarizeMonthlyTotals() {
        JSONObject totals = new JSONObject();
        
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        double monthlyIncome = 0;
        double monthlyExpenses = 0;
        
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(currentMonth)) {
                if ("Income".equals(t.getType())) {
                    monthlyIncome += t.getAmount();
                } else if ("Expense".equals(t.getType())) {
                    monthlyExpenses += t.getAmount();
                }
            }
        }
        
        totals.put("currentMonth", currentMonth);
        totals.put("income", Math.round(monthlyIncome * 100) / 100.0);
        totals.put("expenses", Math.round(monthlyExpenses * 100) / 100.0);
        totals.put("netSavings", Math.round((monthlyIncome - monthlyExpenses) * 100) / 100.0);
        
        return totals;
    }
    
    /**
     * Get top spending categories
     * WHY: Identifies where money is going
     */
    private JSONArray summarizeTopCategories(int limit) {
        JSONArray categories = new JSONArray();
        
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        
        // Calculate totals by category
        Map<String, Double> categoryTotals = new HashMap<>();
        double totalExpenses = 0;
        
        for (Transaction t : transactions) {
            if (t.getDate().startsWith(currentMonth) && "Expense".equals(t.getType())) {
                String category = t.getCategory();
                categoryTotals.put(category, 
                    categoryTotals.getOrDefault(category, 0.0) + t.getAmount());
                totalExpenses += t.getAmount();
            }
        }
        
        // Sort and take top N
        final double finalTotalExpenses = totalExpenses;
        categoryTotals.entrySet().stream()
            .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
            .limit(limit)
            .forEach(entry -> {
                JSONObject cat = new JSONObject();
                cat.put("category", entry.getKey());
                cat.put("amount", Math.round(entry.getValue() * 100) / 100.0);
                cat.put("percent", finalTotalExpenses > 0 ? 
                    Math.round((entry.getValue() / finalTotalExpenses) * 1000) / 10.0 : 0);
                categories.put(cat);
            });
        
        return categories;
    }
    
    /**
     * Summarize active investments
     * WHY: Complete financial picture includes assets
     */
    private JSONArray summarizeInvestments() {
        JSONArray investments = new JSONArray();
        
        List<Investment> allInvestments = investmentDAO.getAllInvestments();
        for (Investment inv : allInvestments) {
            JSONObject investment = new JSONObject();
            investment.put("name", inv.getName());
            investment.put("category", inv.getCategory());
            investment.put("amount", Math.round(inv.getAmount() * 100) / 100.0);
            investment.put("startDate", inv.getStartDate());
            // Note: Omitting detailed notes for privacy/token efficiency
            investments.put(investment);
        }
        
        return investments;
    }
    
    /**
     * Get top expense category name for quick reference
     */
    public String getTopExpenseCategory() {
        JSONArray topCategories = summarizeTopCategories(1);
        if (topCategories.length() > 0) {
            return topCategories.getJSONObject(0).getString("category");
        }
        return "Unknown";
    }
    
    /**
     * Generate privacy-safe summary (minimal PII)
     * WHY: When AI data upload is restricted by user preference
     */
    public JSONObject summarizeUserDataMinimal() {
        JSONObject summary = new JSONObject();
        
        summary.put("currency", SettingsManager.getCurrencyCode());
        summary.put("dataPrivacyMode", true);
        
        // Only aggregate statistics, no individual transactions
        JSONObject budgetInfo = summarizeBudget();
        summary.put("budget", budgetInfo);
        
        JSONObject monthlyTotals = summarizeMonthlyTotals();
        summary.put("monthlyTotals", monthlyTotals);
        
        // Category totals only (no dates or specific amounts)
        summary.put("note", "Detailed transaction data hidden per user privacy settings");
        
        return summary;
    }
}
