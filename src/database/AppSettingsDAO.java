package database;

import java.sql.*;

public class AppSettingsDAO {
    
    /**
     * Get a setting value by key
     */
    public String getSetting(String key, String defaultValue) {
        String sql = "SELECT value FROM app_settings WHERE key = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return defaultValue;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getString("value");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving setting: " + e.getMessage());
        }
        
        return defaultValue;
    }
    
    /**
     * Set a setting value
     */
    public boolean setSetting(String key, String value) {
        // First check if setting exists
        String checkSql = "SELECT id FROM app_settings WHERE key = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, key);
                ResultSet rs = checkStmt.executeQuery();
                
                if (rs.next()) {
                    // Update existing setting
                    return updateSetting(key, value);
                } else {
                    // Insert new setting
                    return insertSetting(key, value);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error setting property: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Insert a new setting
     */
    private boolean insertSetting(String key, String value) {
        String sql = "INSERT INTO app_settings (key, value) VALUES (?, ?)";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, key);
                pstmt.setString(2, value);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error inserting setting: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Update an existing setting
     */
    private boolean updateSetting(String key, String value) {
        String sql = "UPDATE app_settings SET value = ? WHERE key = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, value);
                pstmt.setString(2, key);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error updating setting: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Delete all user data (transactions only, keep settings and categories)
     */
    public boolean deleteAllTransactions() {
        String sql = "DELETE FROM transactions";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (Statement stmt = conn.createStatement()) {
                stmt.execute(sql);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting transactions: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Reset all application data
     */
    public boolean resetAllData() {
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            try (Statement stmt = conn.createStatement()) {
                // Delete all category budgets first (due to foreign key constraint)
                stmt.execute("DELETE FROM category_budgets");
                // Delete all transactions
                stmt.execute("DELETE FROM transactions");
                // Reset budget to defaults
                stmt.execute("DELETE FROM budget");
                stmt.execute("INSERT INTO budget (monthly_income, limit_amount) VALUES (0.0, 0.0)");
                
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error resetting data: " + e.getMessage());
            return false;
        }
    }
}
