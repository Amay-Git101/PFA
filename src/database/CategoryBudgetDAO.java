package database;

import models.BudgetCategory;
import java.sql.*;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CategoryBudgetDAO {
    
    /**
     * Get budget for a specific category in a specific month
     */
    public BudgetCategory getBudgetByCategory(int categoryId, String month) {
        String sql = "SELECT cb.id, cb.category_id, c.name, cb.month, cb.limit_amount " +
                     "FROM category_budgets cb " +
                     "JOIN categories c ON cb.category_id = c.id " +
                     "WHERE cb.category_id = ? AND cb.month = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return null;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                pstmt.setString(2, month);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return new BudgetCategory(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("month"),
                        rs.getDouble("limit_amount")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving category budget: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Get all budgets for a specific month
     */
    public List<BudgetCategory> getBudgetsByMonth(String month) {
        List<BudgetCategory> budgets = new ArrayList<>();
        String sql = "SELECT cb.id, cb.category_id, c.name, cb.month, cb.limit_amount " +
                     "FROM category_budgets cb " +
                     "JOIN categories c ON cb.category_id = c.id " +
                     "WHERE cb.month = ? " +
                     "ORDER BY c.name";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return budgets;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, month);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    budgets.add(new BudgetCategory(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("month"),
                        rs.getDouble("limit_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving budgets by month: " + e.getMessage());
        }
        
        return budgets;
    }
    
    /**
     * Get all budgets for a specific category across all months
     */
    public List<BudgetCategory> getBudgetsByCategory(int categoryId) {
        List<BudgetCategory> budgets = new ArrayList<>();
        String sql = "SELECT cb.id, cb.category_id, c.name, cb.month, cb.limit_amount " +
                     "FROM category_budgets cb " +
                     "JOIN categories c ON cb.category_id = c.id " +
                     "WHERE cb.category_id = ? " +
                     "ORDER BY cb.month DESC";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return budgets;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    budgets.add(new BudgetCategory(
                        rs.getInt("id"),
                        rs.getInt("category_id"),
                        rs.getString("name"),
                        rs.getString("month"),
                        rs.getDouble("limit_amount")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving category budgets: " + e.getMessage());
        }
        
        return budgets;
    }
    
    /**
     * Get map of category budgets for a month (category_id -> limit_amount)
     */
    public Map<Integer, Double> getBudgetMapByMonth(String month) {
        Map<Integer, Double> budgetMap = new HashMap<>();
        String sql = "SELECT category_id, limit_amount FROM category_budgets WHERE month = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return budgetMap;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, month);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    budgetMap.put(rs.getInt("category_id"), rs.getDouble("limit_amount"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving budget map: " + e.getMessage());
        }
        
        return budgetMap;
    }
    
    /**
     * Add or update a budget for a category in a specific month
     */
    public boolean setBudget(int categoryId, String month, double limitAmount) {
        // First check if budget exists
        String checkSql = "SELECT id FROM category_budgets WHERE category_id = ? AND month = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, categoryId);
                checkStmt.setString(2, month);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Budget exists, update it
                    return updateBudget(categoryId, month, limitAmount);
                } else {
                    // Budget doesn't exist, insert new
                    return insertBudget(categoryId, month, limitAmount);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error setting budget: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Insert a new budget
     */
    private boolean insertBudget(int categoryId, String month, double limitAmount) {
        String sql = "INSERT INTO category_budgets (category_id, month, limit_amount) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                pstmt.setString(2, month);
                pstmt.setDouble(3, limitAmount);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting budget: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update an existing budget
     */
    private boolean updateBudget(int categoryId, String month, double limitAmount) {
        String sql = "UPDATE category_budgets SET limit_amount = ? WHERE category_id = ? AND month = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setDouble(1, limitAmount);
                pstmt.setInt(2, categoryId);
                pstmt.setString(3, month);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating budget: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete a specific budget
     */
    public boolean deleteBudget(int categoryId, String month) {
        String sql = "DELETE FROM category_budgets WHERE category_id = ? AND month = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                pstmt.setString(2, month);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting budget: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete all budgets for a category (when category is deleted)
     */
    public boolean deleteBudgetsByCategory(int categoryId) {
        String sql = "DELETE FROM category_budgets WHERE category_id = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, categoryId);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting category budgets: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get current month in YYYY-MM format
     */
    public static String getCurrentMonth() {
        return YearMonth.now().toString();
    }
    
    /**
     * Get previous month in YYYY-MM format
     */
    public static String getPreviousMonth() {
        return YearMonth.now().minusMonths(1).toString();
    }
    
    /**
     * Get next month in YYYY-MM format
     */
    public static String getNextMonth() {
        return YearMonth.now().plusMonths(1).toString();
    }
}
