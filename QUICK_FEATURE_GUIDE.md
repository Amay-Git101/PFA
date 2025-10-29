# Quick Feature Guide - Buy/Sell & Enhanced SIP Frequencies

## 🎯 Feature Overview

### New Feature 1: Buy/Sell for Tradeable Assets
**Applies to:** Stocks, Gold, Real Estate

**How to Use:**
1. Open Investment Panel
2. Select Category: Stocks, Gold, or Real Estate
3. New "Action" field appears with Buy/Sell dropdown
4. Fill in: Investment Name, Amount, Date
5. Choose Action: Buy or Sell
6. Click "Add Investment"

**Result:**
- ✅ Transaction created (NOT in investments table)
- ✅ Buy → Expense transaction logged
- ✅ Sell → Income transaction logged
- ✅ Appears on Dashboard under "Stocks-Buy", "Gold-Sell", etc.

**Example:**
- Buy 100 shares of TCS @ ₹3,500 each = ₹350,000 Expense
- Sell 100 shares of TCS @ ₹3,700 each = ₹370,000 Income

---

### New Feature 2: Enhanced SIP Frequencies
**Applies to:** SIP investments

**Supported Frequencies:**

| Frequency | When It Processes | Example | Setup |
|-----------|------------------|---------|-------|
| **One-Time** | Once on/after start date | Single payment of ₹10,000 on 2025-01-15 | Start Date only |
| **Monthly** | Every month on payment day | ₹5,000 on 5th of each month | Start Date + Payment Day (5) |
| **Quarterly** | Jan, Apr, Jul, Oct on payment day | ₹10,000 on 5th of Q1, Q2, Q3, Q4 | Start Date + Payment Day (5) |
| **Yearly** | Anniversary of start date | ₹20,000 every March 15th | Start Date (extracts month/day) |

**How to Use:**
1. Open Investment Panel
2. Select Category: SIP
3. New "Frequency" and "Payment Day" fields appear
4. Choose Frequency: One-Time, Monthly, Quarterly, or Yearly
5. If Monthly/Quarterly: Set Payment Day (1-28)
6. Set Start Date
7. Click "Add Investment"

**Processing:**
- Click "Process Recurring SIPs" button to generate transactions
- Button processes all eligible SIPs for today
- Prevents duplicates automatically

**Example Workflows:**

### Example A: Monthly SIP
- Investment: "HDFC Index Fund"
- Amount: ₹5,000
- Category: SIP
- Frequency: Monthly
- Payment Day: 10
- Start Date: 2024-01-10

**Result:** Every 10th of month starting Jan 2024, a ₹5,000 Expense transaction created

### Example B: Quarterly SIP
- Investment: "Quarterly Dividend Plan"
- Amount: ₹15,000
- Category: SIP
- Frequency: Quarterly
- Payment Day: 1
- Start Date: 2024-01-01

**Result:** April 1, July 1, Oct 1 transactions created (₹15,000 each)

### Example C: Yearly SIP
- Investment: "Annual Bonus Investment"
- Amount: ₹100,000
- Category: SIP
- Frequency: Yearly
- Start Date: 2024-03-20

**Result:** Every March 20th (starting 2024), ₹100,000 Expense transaction created

### Example D: One-Time Investment
- Investment: "Goal Fund Contribution"
- Amount: ₹50,000
- Category: SIP
- Frequency: One-Time
- Start Date: 2025-02-28

**Result:** Single ₹50,000 transaction on or after 2025-02-28

---

## 📊 Transaction Categories Created

### Buy/Sell Categories:
- `Stocks-Buy` → Expense transactions
- `Stocks-Sell` → Income transactions
- `Gold-Buy` → Expense transactions
- `Gold-Sell` → Income transactions
- `Real Estate-Buy` → Expense transactions
- `Real Estate-Sell` → Income transactions

### SIP Categories:
- `Investment-SIP` → Always Expense (recurring payments)

---

## 🔍 Source Field Reference

When viewing transactions, check the "Source" field to identify investment type:

| Source | Meaning | Appears In |
|--------|---------|-----------|
| `investment-buy` | Buy transaction for stocks/gold/real estate | Transactions table |
| `investment-sell` | Sell transaction for stocks/gold/real estate | Transactions table |
| `investment-recurring` | Recurring SIP payment | Transactions table |
| `investment` | One-time investment (FD, etc.) | Transactions table |

---

## ⚠️ Important Notes

1. **No Duplicate SIPs:** The system prevents creating the same SIP transaction twice in a month
   - Verified via investment name + date check
   - Safe to click "Process Recurring SIPs" multiple times

2. **Buy/Sell Not Tracked:** Buy/Sell transactions don't create investment records
   - Only appear in transactions (for profit/loss tracking)
   - Keep investments table clean (long-term holdings only)

3. **SIP Manual Processing:** Must click "Process Recurring SIPs" button
   - Doesn't automatically run
   - Click whenever you want to process eligible SIPs for today
   - Ideal: Run daily or on specific schedule

4. **Date Format:** Always use YYYY-MM-DD
   - Example: 2025-12-25 (not 25-12-2025 or 12/25/2025)

5. **Payment Day Range:** 1-28 for Monthly/Quarterly frequencies
   - Day 29-31 not supported (compatibility with shorter months)

---

## 🧪 Testing Scenarios

### Test Buy/Sell:
```
✓ Buy Gold: Name="Sovereign Gold", Amount=10000, Date=2025-10-29, Action=Buy
  → Should see "Stocks-Buy" in Dashboard with -₹10,000
✓ Sell Gold: Name="Sovereign Gold", Amount=11000, Date=2025-10-29, Action=Sell
  → Should see "Gold-Sell" in Dashboard with +₹11,000
```

### Test Monthly SIP:
```
✓ Add SIP: Name="Mutual Fund", Amount=5000, Frequency=Monthly, PaymentDay=5
  → On 5th of each month, click "Process Recurring SIPs"
  → Should see recurring ₹5,000 Expense transactions
```

### Test Quarterly SIP:
```
✓ Add SIP: Name="Quarterly Plan", Amount=10000, Frequency=Quarterly, PaymentDay=1
  → On 1st of Jan, Apr, Jul, Oct, click "Process Recurring SIPs"
  → Should see ₹10,000 Expense transactions (4 times/year)
```

### Test Yearly SIP:
```
✓ Add SIP: Name="Annual Fund", Amount=50000, Frequency=Yearly, StartDate=2024-06-15
  → On or after 15th of June each year, click "Process Recurring SIPs"
  → Should see ₹50,000 Expense transaction (1 time/year)
```

---

## 💾 Data Storage

### Investments Table:
- Stores: SIPs, FDs, Real Estate holdings, Gold holdings, Stock holdings (if tracking)
- Does NOT store: One-time Buy/Sell trades

### Transactions Table:
- Stores: Buy/Sell trades, SIP recurring payments, Investment expenses
- Sources: `investment-buy`, `investment-sell`, `investment-recurring`

---

## 🆘 Troubleshooting

**Issue:** "Action" field not showing for Stocks category
- **Fix:** Make sure you're selecting "Stocks" (case-sensitive), not "Stock"

**Issue:** Quarterly SIP not processing
- **Fix:** Check if today is in Jan/Apr/Jul/Oct AND day >= Payment Day

**Issue:** Yearly SIP not processing
- **Fix:** Check if today's month/day matches start date's month/day

**Issue:** Duplicate SIP transactions
- **Fix:** This shouldn't happen (duplicate prevention enabled)
  - Check the date range using filters on Dashboard
  - If duplicates found, manually delete via Dashboard

---

## 📝 Field Summary

### For Buy/Sell (Stocks, Gold, Real Estate):
- ✓ Investment Name (required)
- ✓ Amount (required)
- ✓ Category (Stocks/Gold/Real Estate, required)
- ✓ Start Date (required)
- ✓ Action (Buy/Sell, required)
- ✗ Frequency (hidden)
- ✗ Payment Day (hidden)
- ✗ Maturity Date (hidden)
- ✗ Interest Rate (hidden)

### For SIP:
- ✓ Investment Name (required)
- ✓ Amount (required)
- ✓ Category (SIP, required)
- ✓ Start Date (required)
- ✓ Frequency (required)
- ✓ Payment Day (only if Monthly/Quarterly)
- ✗ Maturity Date (hidden)
- ✗ Interest Rate (hidden)
- ✗ Action (hidden)

### For Fixed Deposit (FD):
- ✓ Investment Name (required)
- ✓ Amount (required)
- ✓ Category (FD, required)
- ✓ Start Date (required)
- ✓ Maturity Date (optional but recommended)
- ✓ Interest Rate (optional but recommended)
- ✗ Frequency (hidden)
- ✗ Payment Day (hidden)
- ✗ Action (hidden)

---

## 🚀 Performance Notes

- Buy/Sell processing: Instant (immediate transaction creation)
- SIP processing: < 1 second per SIP (batch processing)
- Duplicate check: O(n) where n = transactions for investment name
- Recommended: Run "Process Recurring SIPs" once daily

