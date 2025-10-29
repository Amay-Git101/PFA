package models;

public class Transaction {
    private int id;
    private String type;
    private String category;
    private double amount;
    private String date;
    private String notes;
    private String source;
    
    // Constructor with all fields
    public Transaction(int id, String type, String category, double amount, String date, String notes, String source) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.source = source;
    }
    
    // Constructor without id (for new transactions)
    public Transaction(String type, String category, double amount, String date, String notes, String source) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.notes = notes;
        this.source = source;
    }
    
    // Getters
    public int getId() { return id; }
    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getDate() { return date; }
    public String getNotes() { return notes; }
    public String getSource() { return source; }
    
    // Setters
    public void setId(int id) { this.id = id; }
    public void setType(String type) { this.type = type; }
    public void setCategory(String category) { this.category = category; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setDate(String date) { this.date = date; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setSource(String source) { this.source = source; }
    
    @Override
    public String toString() {
        return String.format("%s: %s - $%.2f (%s)", type, category, amount, date);
    }
}