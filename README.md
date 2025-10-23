# 💰 Personal Finance Advisor

A comprehensive Java Swing desktop application for personal financial management with a modern dark-themed UI inspired by Omolon/Farhan aesthetic.

## ✨ Features

- **📊 Dashboard**: Overview of balance, income, expenses, and financial health
- **💰 Expense Tracker**: Add, view, and delete transactions with detailed categorization
- **📊 Budget Planner**: Set monthly income and spending limits with visual progress tracking
- **📈 Reports**: Placeholder for future advanced reporting features
- **🤖 AI Advisor**: Intelligent financial recommendations based on spending patterns
- **⚙️ Settings**: User preferences and application information

## 🎨 Design

- **Dark Theme**: Modern #1E1E1E background with #2A2A2A panels
- **Accent Color**: Mint-teal (#00C897) for highlights and buttons
- **Typography**: Segoe UI font family throughout
- **Modern UI**: Rounded components, hover effects, and clean layouts

## 🏗️ Architecture

```
/src/
 ├─ Main.java                 # Application entry point
 ├─ ui/                      # User interface components
 │    ├─ DashboardPanel.java  # Balance summary and recent transactions
 │    ├─ ExpensePanel.java    # Transaction management
 │    ├─ BudgetPanel.java     # Budget planning and monitoring  
 │    ├─ ReportsPanel.java    # Future reporting features
 │    ├─ AiPanel.java         # AI financial advisor
 │    └─ SettingsPanel.java   # User preferences
 ├─ database/                # Data access layer
 │    ├─ DBConnection.java    # SQLite connection management
 │    └─ TransactionDAO.java  # Transaction database operations
 ├─ models/                  # Data models
 │    └─ Transaction.java     # Transaction entity
 └─ backend/                 # Business logic
      └─ BudgetLogic.java     # Financial calculations and AI logic
```

## 💾 Database

- **SQLite**: Lightweight, embedded database (finance.db)
- **Auto-created**: Database and tables created automatically on first run
- **Sample data**: Pre-loaded with example transactions for demonstration

### Tables:
- `transactions`: Transaction records with type, category, amount, date, notes
- `budget`: Monthly income and spending limit settings

## 🚀 Getting Started

### Prerequisites
- Java 8 or higher
- SQLite JDBC driver (included: sqlite-jdbc-3.36.0.3.jar)

### Running the Application

#### Option 1: Use the batch file (Windows)
```bash
run.bat
```

#### Option 2: Command line
```bash
java -cp "sqlite-jdbc-3.36.0.3.jar;." Main
```

#### Option 3: Compile and run
```bash
# Compile
javac -cp "sqlite-jdbc-3.36.0.3.jar;." -encoding UTF-8 -d . src/*.java src/ui/*.java src/database/*.java src/models/*.java src/backend/*.java

# Run
java -cp "sqlite-jdbc-3.36.0.3.jar;." Main
```

## 🔮 AI Features

The AI Advisor provides personalized financial recommendations based on:
- Current budget usage percentage
- Spending patterns by category
- Income vs expense ratios
- Financial health indicators

### AI Responses for:
- **Budgeting**: Personalized advice based on current spending
- **Saving**: Strategies for building savings
- **Debt Management**: Tips for paying down debt
- **Investment**: Basic investment guidance
- **General**: Contextual financial advice

## 💡 Usage Tips

1. **Start with Budget Setup**: Go to Budget Planner to set your monthly income and spending limit
2. **Add Transactions**: Use Expense Tracker to log income and expenses
3. **Monitor Progress**: Check Dashboard for overview and Budget Planner for detailed progress
4. **Get Advice**: Ask the AI Advisor for personalized financial recommendations
5. **Customize**: Use Settings to personalize your experience

## 🛠️ Technology Stack

- **Frontend**: Java Swing with custom dark theme
- **Database**: SQLite with JDBC
- **Architecture**: MVC pattern with separated concerns
- **UI Design**: Modern dark theme with rounded components and hover effects

## 📊 Key Components

### Dashboard
- Real-time balance calculation
- Recent transactions table
- Financial health indicator
- Summary cards with color-coded values

### Expense Tracker  
- Transaction form with validation
- Category and type selection
- Full transaction history table
- Add/delete operations

### Budget Planner
- Monthly income and limit settings
- Visual progress bar with color coding
- Budget status indicators
- Remaining budget calculation

### AI Advisor
- Context-aware financial recommendations
- Keyword-based response system
- Personalized advice based on spending data
- Interactive question/answer interface

## 🎯 Future Enhancements

- Advanced reporting with charts and graphs
- Data export/import functionality
- Recurring transaction support
- Multi-currency support
- Cloud synchronization
- Enhanced AI with machine learning

## 🏆 Development Notes

This application demonstrates:
- Modern Java Swing UI development
- SQLite integration with proper database design
- MVC architecture with clean separation
- Custom theming and component styling
- Event-driven programming patterns
- Error handling and user input validation

Built with ❤️ for better financial management!