package database;

import models.Investment;
import models.Transaction;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvestmentDAO {
    
    /**
     * Adds a new investment to the investments table.
     * Note: SIPs are not given automatic transactions - use processRecurringSIPs() to generate them.
     */
    public boolean addInvestment(Investment investment) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        
        try {
            String sql = "INSERT INTO investments (name, category, amount, start_date, frequency, day_of_month, maturity_date, interest_rate, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            
            if (investment.getMaturityDate() != null && !investment.getMaturityDate().isEmpty()) {
                pstmt.setString(7, investment.getMaturityDate());
            } else {
                pstmt.setNull(7, Types.VARCHAR);
            }
            
            if (investment.getInterestRate() != null) {
                pstmt.setDouble(8, investment.getInterestRate());
            } else {
                pstmt.setNull(8, Types.REAL);
            }
            
            pstmt.setString(9, "Active");
            
            pstmt.executeUpdate();
            pstmt.close();
            
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
            String sql = "SELECT id, name, category, amount, start_date, frequency, day_of_month, maturity_date, interest_rate, status FROM investments ORDER BY start_date DESC";
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
                String maturityDate = rs.getString("maturity_date");
                Double interestRate = rs.getObject("interest_rate") != null ? rs.getDouble("interest_rate") : null;
                String status = rs.getString("status");
                if (status == null || status.isEmpty()) {
                    status = "Active";
                }
                
                Investment investment = new Investment(id, name, category, amount, startDate, frequency, dayOfMonth, maturityDate, interestRate, status);
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
    
    /**
     * Updates investment status (e.g., "Active", "Matured").
     */
    public boolean updateInvestmentStatus(int investmentId, String status) {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed");
            return false;
        }
        
        try {
            String sql = "UPDATE investments SET status = ? WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, status);
            pstmt.setInt(2, investmentId);
            pstmt.executeUpdate();
            pstmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating investment status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
