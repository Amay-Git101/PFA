package models;

public class BudgetCategory {
    private int id;
    private int categoryId;
    private String categoryName;
    private String month;  // Format: YYYY-MM
    private double limitAmount;
    
    // Constructor with all fields
    public BudgetCategory(int id, int categoryId, String categoryName, String month, double limitAmount) {
        this.id = id;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.month = month;
        this.limitAmount = limitAmount;
    }
    
    // Constructor without id (for new budget entries)
    public BudgetCategory(int categoryId, String categoryName, String month, double limitAmount) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.month = month;
        this.limitAmount = limitAmount;
    }
    
    // Getters
    public int getId() {
        return id;
    }
    
    public int getCategoryId() {
        return categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public String getMonth() {
        return month;
    }
    
    public double getLimitAmount() {
        return limitAmount;
    }
    
    // Setters
    public void setId(int id) {
        this.id = id;
    }
    
    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public void setMonth(String month) {
        this.month = month;
    }
    
    public void setLimitAmount(double limitAmount) {
        this.limitAmount = limitAmount;
    }
    
    @Override
    public String toString() {
        return String.format("BudgetCategory{category='%s', month='%s', limit=%.2f}", 
                             categoryName, month, limitAmount);
    }
}
