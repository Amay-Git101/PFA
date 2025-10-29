# Implementation Roadmap for Personal Finance Advisor Enhancements

## Completed in This Session
✅ Task 3 Database Schema: Added maturity_date and interest_rate columns
✅ Task 3 Model & DAO: Updated Investment model and DAO methods  
✅ Task 3 Helper Method: Added hasTransactionForMonth to TransactionDAO
✅ Task 3 SIP Processing: Added processRecurringSIPs button and logic
✅ Task 2 Dynamic UI: Added form fields with visibility control
✅ TransactionDAO: Added deleteTransactionsByName() method
✅ InvestmentDAO: Added updateInvestmentStatus() method

## Remaining High-Priority Tasks

### Group 1.2: Delete Functionality for Investment Panel
**Status:** READY FOR IMPLEMENTATION

In `ui/InvestmentPanel.java`:
1. Add "Delete Selected" button near the table
2. Extract investment ID and name from selected row
3. Show confirmation dialog
4. Call investmentDAO.deleteInvestment(id)
5. Call transactionDAO.deleteTransactionsByName(name) to remove associated transactions
6. Refresh UI with mainFrame.refreshAllPanels()

### Group 2.1: Fix SIP Duplicate Transaction Bug
**Status:** NEEDS IMPLEMENTATION

In `ui/InvestmentPanel.java` Add Investment button:
- Remove ALL transaction creation logic for "SIP" category
- Only add to investments table
- Let processRecurringSIPs handle all SIP transactions

### Group 2.2: Fix Dynamic Form UI Visibility
**Status:** PARTIALLY DONE (needs revalidate/repaint)

Add to category listener:
```java
formPanel.revalidate();
formPanel.repaint();
```

### Group 2.3: Buy/Sell Logic for Stocks/Gold/Real Estate
**Status:** NEEDS IMPLEMENTATION

In `ui/InvestmentPanel.java`:
1. Add "Action" JComboBox ["Buy", "Sell"] - visibility tied to category
2. For these categories, create transactions directly (not investments)
3. Buy: type="Expense", category="Investment-[CategoryName]", source="investment"
4. Sell: type="Income", category="Investment-[CategoryName]", source="investment"

### Group 2.4: Enhanced SIP Frequency Logic
**Status:** NEEDS IMPLEMENTATION

Enhance processRecurringSIPs() for:
- One-Time: Check startDate == today or past
- Monthly: Current day >= paymentDay
- Quarterly: Day matches AND month in [1,4,7,10]
- Yearly: Month/day matches startDate

### Group 2.5: FD Maturity Logic
**Status:** NEEDS DATABASE MIGRATION

Database Updates Needed:
```sql
ALTER TABLE investments ADD COLUMN status TEXT DEFAULT 'Active';
```

In `ui/InvestmentPanel.java`:
1. Add "Check for Matured FDs" button
2. Calculate MaturedAmount: Principal + (Principal * Rate * Time / 100)
3. Create Income transaction with type="Income", category="Investment-FD-Matured"
4. Update status to "Matured"

### Group 1.1: Calendar Date Pickers
**Status:** DEFERRED (requires external library)

Recommendation: Use org.jdatepicker.JDatePicker
- Add JAR to classpath
- Replace JTextField date fields with JDatePicker
- Format java.util.Date to "YYYY-MM-DD" using SimpleDateFormat

## Testing Checklist
- [ ] Delete investment removes both investment and transactions
- [ ] Adding SIP creates only one transaction (via processRecurringSIPs)
- [ ] Form fields show/hide correctly based on category selection
- [ ] Buy/Sell for stocks creates transactions correctly
- [ ] SIP frequencies work for all types (One-Time, Monthly, Quarterly, Yearly)
- [ ] Matured FDs generate income transactions correctly

## Next Steps
1. Implement Group 1.2 (Delete functionality)
2. Fix Group 2.1 (SIP duplicate bug)
3. Enhance Group 2.2 (Dynamic UI)
4. Add Group 2.3 (Buy/Sell logic)
5. Expand Group 2.4 (Frequency logic)
6. Implement Group 2.5 (FD maturity)
7. Optional: Integrate Group 1.1 (Calendar date pickers)
