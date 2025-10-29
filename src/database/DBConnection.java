package database;

import java.sql.*;

public class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:finance.db";
    private static Connection connection = null;
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                try {
                    Class.forName("org.sqlite.JDBC");
                } catch (ClassNotFoundException e) {
                    System.err.println("SQLite JDBC driver not found: " + e.getMessage());
                    System.err.println("Make sure sqlite-jdbc JAR is in the classpath");
                    return null;
                }
                connection = DriverManager.getConnection(DB_URL);
                if (connection != null) {
                    createTables();
                }
            }
            return connection;
        } catch (SQLException e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    private static void createTables() {
        try (Statement stmt = connection.createStatement()) {
            // Create transactions table
            String createTransactionsTable = """
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    type TEXT NOT NULL,
                    category TEXT NOT NULL,
                    amount REAL NOT NULL,
                    date TEXT NOT NULL,
                    notes TEXT
                )
                """;
            stmt.execute(createTransactionsTable);
            
            // Create budget table
            String createBudgetTable = """
                CREATE TABLE IF NOT EXISTS budget (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    monthly_income REAL NOT NULL,
                    limit_amount REAL NOT NULL
                )
                """;
            stmt.execute(createBudgetTable);
            
            // Create categories table
            String createCategoriesTable = """
                CREATE TABLE IF NOT EXISTS categories (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT UNIQUE NOT NULL,
                    type TEXT NOT NULL,
                    color TEXT DEFAULT '#00C897'
                )
                """;
            stmt.execute(createCategoriesTable);
            
            // Create category budgets table
            String createCategoryBudgetsTable = """
                CREATE TABLE IF NOT EXISTS category_budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    category_id INTEGER NOT NULL,
                    month TEXT NOT NULL,
                    limit_amount REAL NOT NULL,
                    FOREIGN KEY(category_id) REFERENCES categories(id) ON DELETE CASCADE,
                    UNIQUE(category_id, month)
                )
                """;
            stmt.execute(createCategoryBudgetsTable);
            
            // Create investments table
            String createInvestmentsTable = """
                CREATE TABLE IF NOT EXISTS investments (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    amount REAL NOT NULL,
                    start_date TEXT NOT NULL,
                    frequency TEXT NOT NULL,
                    day_of_month INTEGER
                )
                """;
            stmt.execute(createInvestmentsTable);
            
            // Create settings table
            String createSettingsTable = """
                CREATE TABLE IF NOT EXISTS app_settings (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    key TEXT UNIQUE NOT NULL,
                    value TEXT NOT NULL
                )
                """;
            stmt.execute(createSettingsTable);
            
            // Insert sample data if tables are empty
            insertSampleData();
            
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
    
    private static void insertSampleData() {
        try (Statement stmt = connection.createStatement()) {
            // Check if transactions table is empty
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transactions");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert sample transactions
                String[] sampleTransactions = {
                    "INSERT INTO transactions (type, category, amount, date, notes) VALUES ('Expense', 'Food', 45.50, '2024-10-15', 'Grocery shopping')",
                    "INSERT INTO transactions (type, category, amount, date, notes) VALUES ('Income', 'Salary', 3000.00, '2024-10-01', 'Monthly salary')",
                    "INSERT INTO transactions (type, category, amount, date, notes) VALUES ('Expense', 'Entertainment', 25.00, '2024-10-18', 'Movie tickets')",
                    "INSERT INTO transactions (type, category, amount, date, notes) VALUES ('Expense', 'Transport', 15.75, '2024-10-20', 'Bus fare')",
                    "INSERT INTO transactions (type, category, amount, date, notes) VALUES ('Income', 'Freelance', 500.00, '2024-10-12', 'Web design project')"
                };
                
                for (String sql : sampleTransactions) {
                    stmt.execute(sql);
                }
            }
            
            // Check if budget table is empty
            rs = stmt.executeQuery("SELECT COUNT(*) FROM budget");
            if (rs.next() && rs.getInt(1) == 0) {
                // Insert default budget
                stmt.execute("INSERT INTO budget (monthly_income, limit_amount) VALUES (3500.00, 2000.00)");
            }
            
            // Initialize default categories
            rs = stmt.executeQuery("SELECT COUNT(*) FROM categories");
            if (rs.next() && rs.getInt(1) == 0) {
                String[] expenseCategories = {"Food", "Transport", "Entertainment", "Shopping", "Bills", "Healthcare", "Education", "Utilities", "Other"};
                String[] incomeCategories = {"Salary", "Freelance", "Investment", "Bonus", "Other"};
                
                for (String cat : expenseCategories) {
                    stmt.execute("INSERT INTO categories (name, type, color) VALUES ('" + cat + "', 'Expense', '#00C897')");
                }
                for (String cat : incomeCategories) {
                    stmt.execute("INSERT INTO categories (name, type, color) VALUES ('" + cat + "', 'Income', '#00C897')");
                }
            }
            
            // Initialize default settings
            rs = stmt.executeQuery("SELECT COUNT(*) FROM app_settings");
            if (rs.next() && rs.getInt(1) == 0) {
                stmt.execute("INSERT INTO app_settings (key, value) VALUES ('theme', 'dark')");
                stmt.execute("INSERT INTO app_settings (key, value) VALUES ('currency', 'USD')");
                stmt.execute("INSERT INTO app_settings (key, value) VALUES ('user_name', 'User')");
            }
            
        } catch (SQLException e) {
            System.err.println("Error inserting sample data: " + e.getMessage());
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing connection: " + e.getMessage());
        }
    }
}