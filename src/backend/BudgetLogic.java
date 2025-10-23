package backend;

import database.TransactionDAO;
import models.Transaction;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class BudgetLogic {
    private TransactionDAO transactionDAO;
    
    public BudgetLogic() {
        this.transactionDAO = new TransactionDAO();
    }
    
    public double calculateBudgetUsagePercentage() {
        double[] budgetInfo = transactionDAO.getBudgetInfo();
        double limitAmount = budgetInfo[1];
        double monthlyIncome = budgetInfo[0];
        double totalExpenses = transactionDAO.getTotalExpenses();
        
        // If no limit is set, return 0
        if (limitAmount == 0 && monthlyIncome == 0) {
            return 0.0;
        }
        
        // Use the limit if set, otherwise use income as the reference
        double baseAmount = limitAmount > 0 ? limitAmount : monthlyIncome;
        
        if (baseAmount == 0) {
            return 0.0;
        }
        
        return (totalExpenses / baseAmount) * 100;
    }
    
    public double getRemainingBudget() {
        double[] budgetInfo = transactionDAO.getBudgetInfo();
        double limitAmount = budgetInfo[1];
        double monthlyIncome = budgetInfo[0];
        double totalExpenses = transactionDAO.getTotalExpenses();
        
        // Use limit if set, otherwise use income
        double baseAmount = limitAmount > 0 ? limitAmount : monthlyIncome;
        
        return Math.max(0, baseAmount - totalExpenses);
    }
    
    public Map<String, Double> getExpensesByCategory() {
        List<Transaction> transactions = transactionDAO.getAllTransactions();
        Map<String, Double> categoryExpenses = new HashMap<>();
        
        for (Transaction transaction : transactions) {
            if ("Expense".equals(transaction.getType())) {
                categoryExpenses.merge(transaction.getCategory(), 
                                     transaction.getAmount(), 
                                     Double::sum);
            }
        }
        
        return categoryExpenses;
    }
    
    public String getBudgetStatus() {
        double percentage = calculateBudgetUsagePercentage();
        
        if (percentage <= 50) {
            return "Good - You're within budget";
        } else if (percentage <= 80) {
            return "Warning - Approaching budget limit";
        } else if (percentage <= 100) {
            return "Caution - Near budget limit";
        } else {
            return "Alert - Over budget!";
        }
    }
    
    public String[] generateAIRecommendations() {
        Map<String, Double> expenses = getExpensesByCategory();
        double budgetPercentage = calculateBudgetUsagePercentage();
        
        String[] recommendations = new String[3];
        
        // Find highest expense category
        String highestCategory = "";
        double highestAmount = 0;
        if (!expenses.isEmpty()) {
            for (Map.Entry<String, Double> entry : expenses.entrySet()) {
                if (entry.getValue() > highestAmount) {
                    highestAmount = entry.getValue();
                    highestCategory = entry.getKey();
                }
            }
        } else {
            highestCategory = "General";
            highestAmount = 0;
        }
        
        // Generate recommendations based on spending patterns
        if (budgetPercentage > 80) {
            recommendations[0] = "🚨 You're at " + String.format("%.1f", budgetPercentage) + 
                               "% of your budget. Consider reducing expenses.";
            if (!highestCategory.isEmpty() && highestAmount > 0) {
                recommendations[1] = "💡 Your highest spending category is " + highestCategory + 
                                   " ($" + String.format("%.2f", highestAmount) + 
                                   "). Try reducing this by 15%.";
            } else {
                recommendations[1] = "💡 Start tracking your expenses by category to optimize spending.";
            }
            recommendations[2] = "📊 Set up automatic savings to avoid overspending next month.";
        } else if (budgetPercentage > 50) {
            recommendations[0] = "👍 You're doing well at " + String.format("%.1f", budgetPercentage) + 
                               "% of your budget.";
            recommendations[1] = "💰 Consider saving the remaining $" + 
                               String.format("%.2f", getRemainingBudget()) + " for emergencies.";
            if (!highestCategory.isEmpty() && highestAmount > 0) {
                recommendations[2] = "📈 Track your " + highestCategory + " expenses more closely.";
            } else {
                recommendations[2] = "📈 Keep maintaining your current spending habits.";
            }
        } else {
            recommendations[0] = "🌟 Excellent! You're only at " + String.format("%.1f", budgetPercentage) + 
                               "% of your budget.";
            recommendations[1] = "💎 Consider increasing your savings or investment contributions.";
            recommendations[2] = "🎯 You have room for a small treat or planned purchase.";
        }
        
        return recommendations;
    }
    
    public double calculateSavingsRate() {
        double totalIncome = transactionDAO.getTotalIncome();
        double totalExpenses = transactionDAO.getTotalExpenses();
        
        if (totalIncome == 0) {
            return 0.0;
        }
        
        double savings = totalIncome - totalExpenses;
        return (savings / totalIncome) * 100;
    }
    
    public String getFinancialHealth() {
        double savingsRate = calculateSavingsRate();
        
        if (savingsRate >= 20) {
            return "Excellent";
        } else if (savingsRate >= 10) {
            return "Good";
        } else if (savingsRate >= 0) {
            return "Fair";
        } else {
            return "Poor";
        }
    }
}