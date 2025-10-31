# Currency System Implementation Guide

## ✅ Completed Steps

1. **Created SettingsManager** (`src/backend/SettingsManager.java`)
   - Centralized settings management
   - Currency and theme support
   - Default currency: INR (₹)

2. **Updated DBConnection.java**
   - Changed default currency from USD to INR
   - App settings table already exists

3. **Updated Main.java**
   - Added `SettingsManager.loadSettings()` on startup
   - Settings loaded before UI initialization

## 🔄 Remaining Steps

### Update All Panels to Use Dynamic Currency

The following files need to be updated to replace hardcoded currency symbols with `SettingsManager.getCurrencySymbol()`:

#### Files to Update:
1. `src/ui/DashboardPanel.java`
2. `src/ui/ExpensePanel.java`
3. `src/ui/BudgetPanel.java`
4. `src/ui/InvestmentPanel.java`
5. `src/ui/ReportsPanel.java`

#### Search and Replace Pattern:

**Find:** `"$"` or `"₹"` or hardcoded currency in labels/formats

**Replace with:**
```java
// Add import at top
import backend.SettingsManager;

// Replace currency in String.format
// OLD: String.format("$%.2f", amount)
// NEW: String.format("%s%.2f", SettingsManager.getCurrencySymbol(), amount)

// Replace in labels
// OLD: "Amount ($):"
// NEW: "Amount (" + SettingsManager.getCurrencySymbol() + "):"
```

### Make Settings Panel Functional

Update `src/ui/SettingsPanel.java`:
1. Add Main frame reference in constructor
2. Add currency dropdown with INR, USD, EUR, GBP options
3. Implement save functionality that calls `SettingsManager.setCurrency()`
4. Call `mainFrame.refreshAllPanels()` after saving

### Update Main.java Constructor

Change SettingsPanel instantiation:
```java
// OLD: mainContentPanel.add(new SettingsPanel(), "Settings");
// NEW: mainContentPanel.add(new SettingsPanel(this), "Settings");
```

## Testing

After implementation:
1. Run application - should see "Settings loaded: Currency=INR" in console
2. Go to Settings panel
3. Change currency from INR to USD
4. Click Save
5. Verify all amounts show $ instead of ₹
6. Restart app - currency should persist

## Benefits

✅ No hardcoded currency symbols  
✅ User-configurable currency  
✅ Persistent across sessions  
✅ Easy to add new currencies  
✅ Centralized management
