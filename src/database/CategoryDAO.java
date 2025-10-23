package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
    
    public List<String> getCategoriesByType(String type) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE type = ? ORDER BY name";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return categories;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, type);
                ResultSet rs = pstmt.executeQuery();
                
                while (rs.next()) {
                    categories.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving categories: " + e.getMessage());
        }
        
        return categories;
    }
    
    public List<String> getAllCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT DISTINCT name FROM categories ORDER BY name";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return categories;
            }
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(sql);
                
                while (rs.next()) {
                    categories.add(rs.getString("name"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving all categories: " + e.getMessage());
        }
        
        return categories;
    }
    
    public int getCategoryId(String categoryName) {
        String sql = "SELECT id FROM categories WHERE name = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return -1;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getInt("id");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting category ID: " + e.getMessage());
        }
        
        return -1;
    }
    
    public String getCategoryType(String categoryName) {
        String sql = "SELECT type FROM categories WHERE name = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return null;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryName);
                ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    return rs.getString("type");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting category type: " + e.getMessage());
        }
        
        return null;
    }
    
    public boolean addCategory(String name, String type, String color) {
        String sql = "INSERT INTO categories (name, type, color) VALUES (?, ?, ?)";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, type);
                pstmt.setString(3, color != null ? color : "#00C897");
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error adding category: " + e.getMessage());
            return false;
        }
    }
    
    public boolean deleteCategory(String categoryName) {
        String sql = "DELETE FROM categories WHERE name = ?";
        
        try {
            Connection conn = DBConnection.getConnection();
            if (conn == null) {
                System.err.println("Database connection failed");
                return false;
            }
            
            // Get the category ID first
            int categoryId = getCategoryId(categoryName);
            if (categoryId == -1) {
                System.err.println("Category not found: " + categoryName);
                return false;
            }
            
            // Delete associated budgets first (cascade)
            CategoryBudgetDAO budgetDAO = new CategoryBudgetDAO();
            budgetDAO.deleteBudgetsByCategory(categoryId);
            
            // Now delete the category
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, categoryName);
                
                int result = pstmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }
}