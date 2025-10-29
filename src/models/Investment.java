package models;

public class Investment {
    private int id;
    private String name;
    private String category;
    private double amount;
    private String startDate;
    private String frequency;
    private Integer dayOfMonth;
    
    // Full constructor
    public Investment(int id, String name, String category, double amount, String startDate, String frequency, Integer dayOfMonth) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
    }
    
    // Constructor without id (for new investments)
    public Investment(String name, String category, double amount, String startDate, String frequency, Integer dayOfMonth) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.startDate = startDate;
        this.frequency = frequency;
        this.dayOfMonth = dayOfMonth;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public String getStartDate() {
        return startDate;
    }
    
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    
    public String getFrequency() {
        return frequency;
    }
    
    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }
    
    public Integer getDayOfMonth() {
        return dayOfMonth;
    }
    
    public void setDayOfMonth(Integer dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
}
