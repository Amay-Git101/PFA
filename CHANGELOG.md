# Changelog - FinSight Personal Finance Advisor

## Version 2.0.0 - UI Polish & AI Integration

### 🎨 UI/UX Improvements

#### Global Changes
- **App Title**: Updated from "Personal Finance Advisor" to **"FinSight — Personal Finance Advisor"** across all screens
- **Sidebar Cleanup**: Removed all bracket/square glyphs ([], □) from menu items and section headers
- **Theme Enhancement**: Updated color palette to teal accent (#00C897) with improved contrast
- **Typography**: Standardized fonts (SansSerif) and sizes across all panels for consistency
- **Responsive Layouts**: Fixed overlap issues in Settings and improved window resize behavior

#### Files Modified:
- `src/ui/Main.java` - Updated app title, sidebar labels, improved UIManager theme settings
- `src/ui/DashboardPanel.java` - Removed emoji-only titles, improved header styling
- `src/ui/ExpensePanel.java` - Fixed layout initialization order, cleaned labels
- `src/ui/BudgetPanel.java` - Fixed layout, removed brackets from section titles
- `src/ui/SettingsPanel.java` - Complete GridBagLayout overhaul for proper alignment
- `src/ui/ReportsPanel.java` - Cleaned chart titles and improved legend positioning
- `src/ui/InvestmentPanel.java` - Updated headers and table formatting
- `src/ui/AiPanel.java` - Completely rewritten with AI service integration

### 🤖 AI Integration

#### New AI Service Infrastructure
- **AIService** (`src/service/AIService.java`):
  - Pluggable LLM provider support (OpenAI, local models, mock mode)
  - JSON-based request/response handling
  - Action extraction for UI-driven workflows
  - Fallback to mock responses when API unavailable
  
- **SummarizerService** (`src/service/SummarizerService.java`):
  - Generates compact JSON summaries of user financial data
  - Privacy-aware: masks PII, respects user settings
  - Provides context for AI: budget status, transactions, investments
  - Calculates top categories and spending patterns

#### AI Features
- **Context-Aware Responses**: AI analyzes actual user data (budget, transactions, categories)
- **Actionable Insights**: Returns JSON actions (export CSV, create rules) for one-click execution
- **Privacy Controls**: Toggle in Settings to enable/disable AI data upload
- **Mock Mode**: Works without API key using intelligent mock responses
- **Question Types Supported**:
  - Budget analysis ("Am I over budget?")
  - Savings strategies ("How can I save more?")
  - Debt management
  - Investment advice
  - Spending optimization

### 🛠️ Technical Improvements

#### Settings Panel Overhaul
- **Before**: Absolute positioning causing overlap at different window sizes
- **After**: GridBagLayout with proper constraints, responsive to resizing
- **Changes**:
  - Form rows use consistent GridBagConstraints
  - Labels aligned WEST with fixed insets (8,12,8,12)
  - Fields expand horizontally with weightx=1.0
  - Button panel centered with proper spacing

#### Table Improvements
- **Auto-resize modes**: Proper column sizing with horizontal scroll
- **Row height**: Increased to 24-30px for readability
- **Cell renderers**: Currency formatting using NumberFormat
- **Row striping**: Alternate background colors (future enhancement)
- **Header styling**: Bolded headers with theme colors

### 📦 Configuration

#### Environment Variables
Set these for AI integration:
- `LLM_PROVIDER` - Provider name: "openai", "local-llm", or "mock" (default)
- `LLM_API_URL` - API endpoint (e.g., "https://api.openai.com/v1/chat/completions")
- `LLM_API_KEY` - Your API key (kept secure, never logged)

#### Settings UI
New toggle in Settings panel:
- **Enable AI Data Upload** - Default: OFF
- When disabled: AI uses minimal aggregate data only
- When enabled: AI accesses transaction details for better insights

### 🔒 Security & Privacy

- API keys stored securely in app settings database
- Keys never logged or displayed in UI
- User controls data sharing via Settings toggle
- Minimal mode: only aggregate statistics sent (no individual transactions)
- Full mode: includes transaction summaries (still privacy-conscious)

### 📊 Data Summary Format

AI receives JSON context like:
```json
{
  "currency": "USD",
  "asOf": "2025-11-03",
  "budget": {
    "monthlyIncome": 5000,
    "budgetLimit": 3000,
    "totalExpenses": 2547.50,
    "usagePercent": 84.9,
    "remaining": 452.50,
    "status": "Within Budget"
  },
  "recentTransactions": [...],
  "topExpenseCategories": [
    {"category": "Groceries", "amount": 450.00, "percent": 17.7},
    ...
  ]
}
```

### 🎯 Testing

#### Manual Test Checklist
1. **Title Check**: Main window shows "FinSight — Personal Finance Advisor"
2. **Sidebar**: No [] or □ characters visible in menu
3. **Settings**: Resize window to 1280×720, 1366×768, 1920×1080 - no overlap
4. **AI Panel**:
   - Click "Refresh Context" before asking questions
   - Ask: "Am I over budget?" - should reference actual data
   - Ask: "How can I save more?" - should provide actionable advice
   - Verify action buttons appear when relevant
5. **Privacy Toggle**: Disable AI in Settings, verify limited responses

#### Unit Tests
- `src/test/java/service/AIServiceTest.java` - Mock LLM response parsing
- `src/test/java/service/SummarizerServiceTest.java` - Data summarization accuracy

### 📝 Breaking Changes
None - all changes are additive or cosmetic

### 🐛 Bug Fixes
- Fixed ExpensePanel layout causing toString() display bug
- Fixed BudgetPanel layout initialization order
- Fixed Settings panel field overlap at various resolutions
- Removed stray bracket characters from all UI labels

### 🚀 Migration Guide
1. Pull latest changes
2. Add `org.json` library to classpath if not present
3. Set environment variables for AI (optional):
   ```bash
   export LLM_PROVIDER=mock  # or openai
   export LLM_API_KEY=your_key_here  # if using real API
   ```
4. Rebuild and run

### 📖 Documentation
- See `AI_README.md` for detailed AI configuration
- See `README.md` for general app usage

---

**Authors**: Development Team  
**Date**: November 2025  
**Commit**: `ui: polish theme & remove stray glyphs; ai: add AI service & LLM integration; fix settings layout`
