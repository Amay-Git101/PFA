package database;

import models.Investment;
import models.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDAO {
    
    /**
     * Adds a new investment to the investments table.
     * If the investment is a SIP with Monthly frequency, it also creates an Expense transaction.
     */
    public boolean addInvestment(Investment investment) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        
        try {
            String sql = "INSERT INTO investments (name, category, amount, start_date, frequency, day_of_month) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, investment.getName());
            pstmt.setString(2, investment.getCategory());
            pstmt.setDouble(3, investment.getAmount());
            pstmt.setString(4, investment.getStartDate());
            pstmt.setString(5, investment.getFrequency());
            
            if (investment.getDayOfMonth() != null) {
                pstmt.setInt(6, investment.getDayOfMonth());
            } else {
                pstmt.setNull(6, Types.INTEGER);
            }
            
            pstmt.executeUpdate();
            pstmt.close();
            
            // If it's a SIP investment with Monthly frequency, add the first payment as an expense
            if ("SIP".equalsIgnoreCase(investment.getCategory()) && "Monthly".equalsIgnoreCase(investment.getFrequency())) {
                TransactionDAO transactionDAO = new TransactionDAO();
                Transaction sipExpense = new Transaction(
                    "Expense",
                    "Investment-SIP",
                    investment.getAmount(),
                    investment.getStartDate(),
                    investment.getName()
                );
                transactionDAO.addTransaction(sipExpense);
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding investment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Retrieves all investments ordered by start_date in descending order.
     */
    public List<Investment> getAllInvestments() {
        List<Investment> investments = new ArrayList<>();
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return investments;
        }
        
        try {
            String sql = "SELECT id, name, category, amount, start_date, frequency, day_of_month FROM investments ORDER BY start_date DESC";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String startDate = rs.getString("start_date");
                String frequency = rs.getString("frequency");
                Integer dayOfMonth = rs.getObject("day_of_month") != null ? rs.getInt("day_of_month") : null;
                
                Investment investment = new Investment(id, name, category, amount, startDate, frequency, dayOfMonth);
                investments.add(investment);
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error retrieving investments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return investments;
    }
    
    /**
     * Deletes an investment by id.
     */
    public boolean deleteInvestment(int investmentId) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        
        try {
            String sql = "DELETE FROM investments WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, investmentId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Error deleting investment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
