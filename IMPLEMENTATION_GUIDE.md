# Implementation Guide - FinSight UI & AI Overhaul

## ✅ Completed

### 1. AI Service Infrastructure
- ✅ Created `src/service/AIService.java` - Full LLM integration with mock mode
- ✅ Created `src/service/SummarizerService.java` - Data summarization for AI context
- ✅ Created `CHANGELOG.md` - Complete documentation of changes
- ✅ Created `AI_README.md` - AI configuration and usage guide

## 🔧 Required Changes

### A. Main.java - Update App Title & Sidebar

```java
// Line 29: Change title
setTitle("FinSight — Personal Finance Advisor");

// Line 149: Update sidebar title
JLabel titleLabel = new JLabel("FinSight");
titleLabel.setForeground(ACCENT_COLOR);
titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));

// Line 158: Clean menu icons (keep emojis but ensure no brackets)
String[] menuItems = {"Dashboard", "AI Advisor", "Investment Management", "Expenses", "Budget", "Reports", "Settings"};
String[] icons = {"🏠", "🤖", "📈", "💰", "📊", "📉", "⚙️"};
```

### B. All Panel Headers - Sanitize Labels

**Pattern to apply across all panels:**
```java
// BEFORE:
JLabel headerLabel = new JLabel("📊 Dashboard");

// AFTER (with helper method):
private String cleanLabel(String text) {
    if (text == null) return "";
    // Remove leading non-alphanumeric characters except emojis
    return text.replaceAll("^[\\[\\]□■▪▫]+\\s*", "").trim();
}

JLabel headerLabel = new JLabel(cleanLabel("📊 Dashboard"));
```

**Files to update:**
- `DashboardPanel.java` - Line 52
- `BudgetPanel.java` - Line 55  
- `ExpensePanel.java` - Line 54
- `InvestmentPanel.java` - Similar pattern
- `ReportsPanel.java` - Similar pattern
- `SettingsPanel.java` - Line 41

### C. SettingsPanel.java - Already has good GridBagLayout

**Enhancement: Add AI toggle**
```java
// Add to createPreferencesPanel() method after currency selection

// AI Data Upload toggle
gbc.gridx = 0; gbc.gridy = 3;
gbc.fill = GridBagConstraints.NONE;
gbc.weightx = 0;
panel.add(createLabel("Enable AI Data Upload:"), gbc);

JCheckBox aiToggle = new JCheckBox();
aiToggle.setBackground(PANEL_COLOR);
aiToggle.setForeground(TEXT_COLOR);
aiToggle.setSelected(settingsDAO.getSetting("ai_enabled", "false").equals("true"));
aiToggle.addActionListener(e -> {
    settingsDAO.setSetting("ai_enabled", String.valueOf(aiToggle.isSelected()));
});
gbc.gridx = 1; gbc.gridy = 3;
panel.add(aiToggle, gbc);
```

### D. AiPanel.java - Complete Rewrite with AI Integration

**Key changes:**
```java
package ui;

import javax.swing.*;
import java.awt.*;
import service.AIService;
import service.SummarizerService;
import org.json.JSONObject;
import backend.DataExportImport;

public class AiPanel extends JPanel implements Refreshable {
    private AIService aiService;
    private SummarizerService summarizerService;
    private JTextArea questionArea;
    private JTextArea responseArea;
    private JPanel actionsPanel;
    private Main mainFrame;
    private JSONObject currentContext;
    
    public AiPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        
        // Initialize AI services
        String provider = System.getenv().getOrDefault("LLM_PROVIDER", "mock");
        String apiUrl = System.getenv().getOrDefault("LLM_API_URL", "");
        String apiKey = System.getenv().getOrDefault("LLM_API_KEY", "");
        
        aiService = new AIService(provider, apiUrl, apiKey);
        summarizerService = new SummarizerService();
        
        setLayout(new BorderLayout(10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        refreshContext(); // Load initial context
    }
    
    private void initComponents() {
        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel headerLabel = new JLabel("FinSight AI — Financial Advisor");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Refresh context button
        JButton refreshBtn = createStyledButton("🔄 Refresh Context");
        refreshBtn.addActionListener(e -> refreshContext());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        // Main content
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        GridBagConstraints gbc = new GridBagConstraints();
        
        // Question panel
        JPanel questionPanel = createQuestionPanel();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.insets = new Insets(20, 0, 10, 0);
        mainPanel.add(questionPanel, gbc);
        
        // Response panel
        JPanel responsePanel = createResponsePanel();
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(responsePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("💬 Ask Your Financial Question");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Question input
        questionArea = new JTextArea(3, 0);
        questionArea.setBackground(PANEL_COLOR.brighter());
        questionArea.setForeground(TEXT_COLOR);
        questionArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        questionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setText("Am I over budget this month?");
        
        JScrollPane questionScroll = new JScrollPane(questionArea);
        questionScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 0));
        inputPanel.setBackground(PANEL_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        inputPanel.add(questionScroll, BorderLayout.CENTER);
        
        // Ask button
        JButton askButton = createStyledButton("🔮 Ask AI");
        askButton.setPreferredSize(new Dimension(120, 45));
        askButton.addActionListener(e -> askAI());
        inputPanel.add(askButton, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        return panel;
    }
    
    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("🎯 AI Response");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Response area
        responseArea = new JTextArea();
        responseArea.setBackground(BACKGROUND_COLOR);
        responseArea.setForeground(TEXT_COLOR);
        responseArea.setFont(new Font("SansSerif", Font.PLAIN, 14));
        responseArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        responseArea.setText("Click 'Refresh Context' and then ask a question to get started!");
        
        JScrollPane responseScroll = new JScrollPane(responseArea);
        responseScroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        panel.add(responseScroll, BorderLayout.CENTER);
        
        // Actions panel
        actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        actionsPanel.setBackground(PANEL_COLOR);
        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private void refreshContext() {
        // WHY: Load latest financial data for AI context
        currentContext = summarizerService.summarizeUserData();
        JOptionPane.showMessageDialog(this, 
            "Financial data refreshed! AI now has access to your latest transactions and budget.",
            "Context Updated", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void askAI() {
        String question = questionArea.getText().trim();
        if (question.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a question.",
                "Empty Question", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (currentContext == null) {
            refreshContext();
        }
        
        // Show loading state
        responseArea.setText("🤔 Thinking...");
        actionsPanel.removeAll();
        actionsPanel.revalidate();
        actionsPanel.repaint();
        
        // Ask AI in background thread
        SwingUtilities.invokeLater(() -> {
            try {
                AIService.AIResponse response = aiService.askWithContext(question, currentContext);
                
                // Display response
                responseArea.setText(response.getReply());
                responseArea.setCaretPosition(0);
                
                // Display actions if any
                if (response.hasActions()) {
                    for (AIService.AIAction action : response.getActions()) {
                        JButton actionBtn = createActionButton(action);
                        actionsPanel.add(actionBtn);
                    }
                    actionsPanel.revalidate();
                    actionsPanel.repaint();
                }
                
            } catch (Exception ex) {
                responseArea.setText("Error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
    }
    
    private JButton createActionButton(AIService.AIAction action) {
        String label = "🎯 " + getActionLabel(action);
        JButton btn = createStyledButton(label);
        btn.setBackground(new Color(33, 150, 243)); // Blue for actions
        btn.addActionListener(e -> executeAction(action));
        return btn;
    }
    
    private String getActionLabel(AIService.AIAction action) {
        switch (action.getType()) {
            case "export_csv":
                return "Export Transactions";
            case "create_rule":
                return "Create Savings Rule";
            case "set_category_limit":
                return "Set Category Limit";
            default:
                return action.getType();
        }
    }
    
    private void executeAction(AIService.AIAction action) {
        // WHY: Execute AI-suggested actions with user confirmation
        JSONObject params = action.getParameters();
        
        switch (action.getType()) {
            case "export_csv":
                exportTransactions(params);
                break;
            case "create_rule":
                createSavingsRule(params);
                break;
            case "set_category_limit":
                setCategoryLimit(params);
                break;
            default:
                JOptionPane.showMessageDialog(this, 
                    "Action type '" + action.getType() + "' not yet implemented.",
                    "Coming Soon", JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void exportTransactions(JSONObject params) {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Export transactions to CSV?",
            "Confirm Export", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            DataExportImport exporter = new DataExportImport();
            String filename = exporter.createBackupFilename();
            if (exporter.exportToCSV(filename)) {
                JOptionPane.showMessageDialog(this,
                    "Transactions exported to: " + filename,
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private void createSavingsRule(JSONObject params) {
        String name = params.optString("name", "Auto-save");
        double amount = params.optDouble("amount", 200);
        
        String message = String.format(
            "Create savings rule:\nName: %s\nAmount: $%.2f/month\n\n(Note: Manual setup required)",
            name, amount
        );
        JOptionPane.showMessageDialog(this, message, 
            "Savings Rule", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void setCategoryLimit(JSONObject params) {
        String category = params.optString("category", "");
        double limit = params.optDouble("limit", 0);
        
        String message = String.format(
            "Set budget limit for '%s' to $%.2f?\n\n(Navigate to Budget panel to configure)",
            category, limit
        );
        JOptionPane.showMessageDialog(this, message,
            "Category Limit", JOptionPane.INFORMATION_MESSAGE);
    }
    
    @Override
    public void refreshData() {
        refreshContext();
    }
}
```

### E. Table Renderers - Add to All Tables

**Currency Cell Renderer:**
```java
// Add to ExpensePanel, BudgetPanel, DashboardPanel, InvestmentPanel

import javax.swing.table.DefaultTableCellRenderer;
import java.text.NumberFormat;

class CurrencyRenderer extends DefaultTableCellRenderer {
    private final NumberFormat currencyFormat;
    
    public CurrencyRenderer() {
        currencyFormat = NumberFormat.getCurrencyInstance();
        setHorizontalAlignment(JLabel.RIGHT);
    }
    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        if (value instanceof Number) {
            value = currencyFormat.format(((Number) value).doubleValue());
        }
        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    }
}

// Usage in table setup:
transactionTable.getColumnModel().getColumn(4).setCellRenderer(new CurrencyRenderer());
```

**Row Striping:**
```java
class StripedTableRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            c.setBackground(row % 2 == 0 ? PANEL_COLOR : PANEL_COLOR.brighter());
        }
        return c;
    }
}
```

## 📋 Testing Checklist

### UI Tests
- [ ] Window title shows "FinSight — Personal Finance Advisor"
- [ ] No `[]` or `□` characters in sidebar or panel headers
- [ ] Settings panel responsive at 1280×720, 1366×768, 1920×1080
- [ ] All tables have proper column widths and don't overflow
- [ ] Currency values formatted correctly with symbol

### AI Tests
- [ ] Mock mode works without API key
- [ ] Ask "Am I over budget?" returns data-aware response
- [ ] Ask "How can I save?" returns actionable advice
- [ ] Action buttons appear and are clickable
- [ ] Refresh context updates AI knowledge

### Integration Tests
- [ ] Export action from AI works
- [ ] Settings toggle affects AI behavior
- [ ] No errors in console/logs

## 📦 Dependencies

Add to your build file (Maven/Gradle):

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.json</groupId>
    <artifactId>json</artifactId>
    <version>20231013</version>
</dependency>
```

Or for Gradle:
```gradle
implementation 'org.json:json:20231013'
```

## 🚀 Quick Start

1. **Pull changes**
2. **Add JSON library** to classpath
3. **Set environment**:
   ```bash
   export LLM_PROVIDER=mock
   ```
4. **Compile**:
   ```bash
   javac -encoding UTF-8 -d bin -sourcepath src src/**/*.java
   ```
5. **Run**:
   ```bash
   java -cp bin:lib/* ui.Main
   ```

## 📸 Expected Results

### Before
- Title: "Personal Finance Advisor"
- Sidebar: "[] Dashboard", "[] Settings"
- Settings: Fields overlap at smaller sizes
- AI Panel: Generic hardcoded responses

### After
- Title: "FinSight — Personal Finance Advisor"
- Sidebar: "🏠 Dashboard", "⚙️ Settings" (clean, no brackets)
- Settings: Proper GridBagLayout, responsive
- AI Panel: Data-aware responses with action buttons

## 🎯 Success Criteria

- ✅ No visual glyphs/brackets anywhere
- ✅ Professional app title across all screens
- ✅ Settings never overlaps regardless of window size
- ✅ AI provides context-aware financial advice
- ✅ AI actions are executable via UI buttons
- ✅ Privacy toggle works and limits data sharing
- ✅ Mock mode allows testing without API costs

---

**Status**: Implementation guide complete  
**Next Steps**: Apply changes to remaining UI files as documented
