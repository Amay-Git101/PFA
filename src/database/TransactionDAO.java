package database;

import models.Transaction;
import events.TransactionEventManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {
    
    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed: getConnection returned null");
                return transactions;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
            
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("notes"),
                        rs.getString("source")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions: " + e.getMessage());
        }
        
        return transactions;
    }
    
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions ORDER BY date DESC LIMIT ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return transactions;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
                pstmt.setInt(1, limit);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("notes"),
                        rs.getString("source")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving recent transactions: " + e.getMessage());
        }
        
        return transactions;
    }
    
    public boolean addTransaction(Transaction transaction) {
        String sql = "INSERT INTO transactions (type, category, amount, date, notes, source) VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transaction.getType());
            pstmt.setString(2, transaction.getCategory());
            pstmt.setDouble(3, transaction.getAmount());
            pstmt.setString(4, transaction.getDate());
            pstmt.setString(5, transaction.getNotes());
            pstmt.setString(6, transaction.getSource());
            
            int result = pstmt.executeUpdate();
            if (result > 0) {
                // Fire transaction added event
                TransactionEventManager.getInstance().notifyTransactionAdded(transaction);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error adding transaction: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, id);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                // Fire transaction deleted event
                TransactionEventManager.getInstance().notifyTransactionDeleted(id);
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("Error deleting transaction: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteTransactionsByName(String name) {
        String sql = "DELETE FROM transactions WHERE notes = ? AND source LIKE 'investment%'";
        
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            int result = pstmt.executeUpdate();
            if (result > 0) {
                // Fire event for each deleted transaction (simplified)
                TransactionEventManager.getInstance().notifyTransactionsRefreshed();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting transactions by name: " + e.getMessage());
            return false;
        }
    }
    
    public double getTotalIncome() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'Income'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double value = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : value;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total income: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    public double getTotalExpenses() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE type = 'Expense'";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                double value = rs.getDouble(1);
                return rs.wasNull() ? 0.0 : value;
            }
        } catch (SQLException e) {
            System.err.println("Error calculating total expenses: " + e.getMessage());
        }
        
        return 0.0;
    }
    
    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }
    
    public List<Transaction> getAllManualTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE source = 'manual' ORDER BY date DESC";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return transactions;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
            
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("notes"),
                        rs.getString("source")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving manual transactions: " + e.getMessage());
        }
        
        return transactions;
    }
    
    public List<Transaction> getTransactionsForCurrentMonth() {
        return getTransactionsByDuration("This Month");
    }
    
    public boolean hasTransactionForMonth(String note, String yearMonth) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE notes = ? AND strftime('%Y-%m', date) = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, note);
                pstmt.setString(2, yearMonth);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking for transaction: " + e.getMessage());
        }
        return false;
    }
    
    public List<Transaction> getTransactionsByDuration(String duration) {
        List<Transaction> transactions = new ArrayList<>();
        String sql;
        
        switch(duration) {
            case "This Month":
                sql = "SELECT * FROM transactions WHERE strftime('%Y-%m', date) = strftime('%Y-%m', 'now') ORDER BY date DESC";
                break;
            case "Last 3 Months":
                sql = "SELECT * FROM transactions WHERE date BETWEEN strftime('%Y-%m-%d', 'now', '-3 months') AND strftime('%Y-%m-%d', 'now') ORDER BY date DESC";
                break;
            case "This Year":
                sql = "SELECT * FROM transactions WHERE strftime('%Y', date) = strftime('%Y', 'now') ORDER BY date DESC";
                break;
            case "All Time":
                sql = "SELECT * FROM transactions ORDER BY date DESC";
                break;
            default:
                sql = "SELECT * FROM transactions WHERE strftime('%Y-%m', date) = strftime('%Y-%m', 'now') ORDER BY date DESC";
        }
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return transactions;
            }
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
            
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        rs.getString("type"),
                        rs.getString("category"),
                        rs.getDouble("amount"),
                        rs.getString("date"),
                        rs.getString("notes"),
                        rs.getString("source")
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving transactions by duration (" + duration + "): " + e.getMessage());
        }
        
        return transactions;
    }
    
    public double[] getBudgetInfo() {
        String sql = "SELECT monthly_income, limit_amount FROM budget ORDER BY id DESC LIMIT 1";
        
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return new double[]{rs.getDouble("monthly_income"), rs.getDouble("limit_amount")};
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving budget info: " + e.getMessage());
        }
        
        return new double[]{0.0, 0.0};
    }
    
    public boolean updateBudget(double monthlyIncome, double limitAmount) {
        String sql = "INSERT OR REPLACE INTO budget (id, monthly_income, limit_amount) VALUES (1, ?, ?)";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, monthlyIncome);
            pstmt.setDouble(2, limitAmount);
            
            int result = pstmt.executeUpdate();
            return result > 0;
            
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            return false;
        }
    }
}