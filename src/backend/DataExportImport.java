package backend;

import database.TransactionDAO;
import models.Transaction;
import java.io.*;
import java.util.List;

public class DataExportImport {
    private TransactionDAO transactionDAO;
    
    public DataExportImport() {
        this.transactionDAO = new TransactionDAO();
    }
    
    /**
     * Export all transactions to a CSV file
     */
    public boolean exportToCSV(String filePath) {
        try {
            List<Transaction> transactions = transactionDAO.getAllTransactions();
            
            FileWriter csvWriter = new FileWriter(filePath);
            
            // Write header
            csvWriter.append("ID,Type,Category,Amount,Date,Notes\n");
            
            // Write data
            for (Transaction t : transactions) {
                csvWriter.append(String.valueOf(t.getId())).append(",");
                csvWriter.append("\"").append(t.getType()).append("\",");
                csvWriter.append("\"").append(t.getCategory()).append("\",");
                csvWriter.append(String.valueOf(t.getAmount())).append(",");
                csvWriter.append("\"").append(t.getDate()).append("\",");
                csvWriter.append("\"").append(escapeCSV(t.getNotes())).append("\"\n");
            }
            
            csvWriter.flush();
            csvWriter.close();
            
            return true;
        } catch (IOException e) {
            System.err.println("Error exporting to CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Import transactions from a CSV file
     */
    public boolean importFromCSV(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            int lineNumber = 0;
            
            // Skip header
            reader.readLine();
            lineNumber++;
            
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                
                try {
                    String[] values = parseCSVLine(line);
                    
                    if (values.length >= 5) {
                        String type = values[1];
                        String category = values[2];
                        double amount = Double.parseDouble(values[3]);
                        String date = values[4];
                        String notes = values.length > 5 ? values[5] : "";
                        
                        Transaction transaction = new Transaction(type, category, amount, date, notes);
                        
                        if (!transactionDAO.addTransaction(transaction)) {
                            System.err.println("Failed to import transaction at line " + lineNumber);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error parsing line " + lineNumber + ": " + e.getMessage());
                }
            }
            
            reader.close();
            return true;
        } catch (IOException e) {
            System.err.println("Error importing from CSV: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Parse a CSV line handling quoted values
     */
    private String[] parseCSVLine(String line) {
        java.util.List<String> values = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        values.add(current.toString());
        return values.toArray(new String[0]);
    }
    
    /**
     * Escape CSV special characters
     */
    private String escapeCSV(String value) {
        if (value == null) return "";
        return value.replaceAll("\"", "\"\"");
    }
    
    /**
     * Create a backup file with timestamp
     */
    public String createBackupFilename() {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new java.util.Date());
        return "pfa_backup_" + timestamp + ".csv";
    }
}
