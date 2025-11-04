# FinSight AI Integration Guide

## Overview

FinSight now includes an intelligent AI Financial Advisor that analyzes your actual financial data and provides personalized recommendations. The AI can answer questions about your budget, suggest savings strategies, and even propose actionable steps.

## Features

### 🎯 Context-Aware Intelligence
- Analyzes your real transactions, budget, and spending patterns
- References actual numbers (not generic advice)
- Understands your financial health status

### 💡 Actionable Insights
- Returns structured actions you can execute with one click
- Examples: "Export transactions as CSV", "Create auto-save rule"
- Actions are presented as UI buttons for easy execution

### 🔒 Privacy-First Design
- **Two modes**: Minimal (aggregate data only) and Full (transaction details)
- Toggle control in Settings panel
- No PII sent without explicit user consent
- API keys stored securely, never logged

### 🔌 Pluggable Providers
- **Mock Mode** (default): Works without API key, intelligent fallback responses
- **OpenAI**: Use GPT models for advanced analysis
- **Custom LLM**: Configure your own endpoint

## Configuration

### Option 1: Environment Variables

Set these before starting the app:

```bash
# Windows (PowerShell)
$env:LLM_PROVIDER="mock"  # or "openai"
$env:LLM_API_URL="https://api.openai.com/v1/chat/completions"
$env:LLM_API_KEY="sk-your-key-here"

# Linux/Mac
export LLM_PROVIDER=mock
export LLM_API_URL=https://api.openai.com/v1/chat/completions
export LLM_API_KEY=sk-your-key-here
```

### Option 2: Settings Database

The app stores AI configuration in `app_settings` table:
- `ai_provider` - Provider name
- `ai_api_url` - API endpoint
- `ai_api_key` - Encrypted API key (future enhancement)
- `ai_enabled` - Master toggle

### Provider Options

#### Mock Provider (Development/Demo)
```
LLM_PROVIDER=mock
```
- No API key required
- Intelligent mock responses based on actual data
- Good for testing and demos
- Free to use

#### OpenAI Provider
```
LLM_PROVIDER=openai
LLM_API_URL=https://api.openai.com/v1/chat/completions
LLM_API_KEY=sk-proj-...
```
- Requires OpenAI API key
- Uses GPT-3.5-turbo by default
- Costs ~$0.002 per request
- Get key: https://platform.openai.com/api-keys

#### Custom LLM Provider
```
LLM_PROVIDER=custom
LLM_API_URL=http://localhost:11434/v1/chat/completions
LLM_API_KEY=optional
```
- Use local models (Ollama, LocalAI, etc.)
- Must implement OpenAI-compatible API format
- Free if running locally

## Usage

### Basic Workflow

1. **Open AI Advisor Panel** (sidebar menu)
2. **Click "Refresh Context"** button to load latest financial data
3. **Type your question** in the text area
4. **Click "Ask AI"** to get response
5. **Review suggestions** and execute any proposed actions

### Sample Questions

#### Budget Analysis
```
Am I over budget this month?
```
**Expected Response:**
> You're at 84.9% of your budget - well done! Your largest expense category is Groceries at $450. You have $452.50 remaining. Continue monitoring discretionary spending.

#### Savings Advice
```
How can I save more money?
```
**Expected Response:**
> Set up automatic transfers to savings. Try the 50/30/20 rule: 50% needs, 30% wants, 20% savings. Based on your $5000 income, aim to save $1000/month.
> 
> **Actions Available:**
> - [Create Auto-Save Rule] ($200/month)

#### Spending Insights
```
What are my biggest expenses?
```
**Expected Response:**
> Your top 3 expense categories this month:
> 1. Groceries: $450.00 (17.7%)
> 2. Transportation: $380.00 (14.9%)
> 3. Dining Out: $295.00 (11.6%)
> 
> Consider reducing dining out expenses by 25% to increase savings.

### Actionable Responses

When AI suggests actions, they appear as clickable buttons:

**Example Action Response:**
```json
{
  "reply": "You're overspending in Dining Out category...",
  "actions": [
    {
      "type": "export_csv",
      "target": "transactions",
      "range": "last_30_days"
    },
    {
      "type": "set_category_limit",
      "category": "Dining Out",
      "limit": 200
    }
  ]
}
```

**UI Shows:**
- [📊 Export Transactions (Last 30 Days)]
- [💰 Set Dining Out Limit to $200]

Click these buttons to execute the action.

## Privacy & Security

### Data Sent to AI

#### Minimal Mode (Default, AI Upload Disabled)
```json
{
  "currency": "USD",
  "budget": {
    "totalExpenses": 2547.50,
    "usagePercent": 84.9,
    "remaining": 452.50
  },
  "monthlyTotals": {
    "income": 5000,
    "expenses": 2547.50
  }
}
```
- Only aggregate statistics
- No individual transaction details
- No dates or specific categories

#### Full Mode (AI Upload Enabled)
```json
{
  "recentTransactions": [
    {"date": "2025-10-15", "type": "Expense", "category": "Groceries", "amount": 45.50}
  ],
  "topExpenseCategories": [
    {"category": "Groceries", "amount": 450, "percent": 17.7}
  ]
}
```
- Includes last 10 transactions (no notes field)
- Top 5 expense categories
- Investment summaries (no personal notes)

### What's Never Sent
- Transaction notes/descriptions
- Sensitive account numbers
- Personal identification beyond app username
- Full transaction history (only recent 10)

### Security Measures
- API keys stored in database (encrypted in future version)
- Keys never displayed in UI or logs
- HTTPS required for external APIs
- User controls data sharing via toggle

## Troubleshooting

### "AI features are disabled"
**Cause:** No API provider configured or toggle disabled

**Fix:**
1. Go to Settings → Enable AI Data Upload
2. Set `LLM_PROVIDER` environment variable
3. For real API: Set `LLM_API_KEY`

### "Unable to connect to AI service"
**Cause:** API endpoint unreachable or invalid key

**Fix:**
- Check network connectivity
- Verify `LLM_API_URL` is correct
- Test API key with curl:
```bash
curl https://api.openai.com/v1/chat/completions \
  -H "Authorization: Bearer $LLM_API_KEY" \
  -H "Content-Type: application/json" \
  -d '{"model":"gpt-3.5-turbo","messages":[{"role":"user","content":"test"}]}'
```

### Mock responses not using my data
**Cause:** Context not refreshed

**Fix:**
- Click "Refresh Context" button before asking
- Ensure you have transactions in database

### Action buttons not appearing
**Cause:** AI didn't detect actionable request

**Fix:**
- Ask more specific questions
- Use action keywords: "export", "set limit", "create rule"
- Example: "Export my transactions" → Will show export button

## API Cost Estimation

### OpenAI Costs
- Average request: ~800 tokens (prompt + response)
- GPT-3.5-turbo: $0.002/1K tokens
- Cost per AI question: ~$0.0016
- 100 questions: ~$0.16
- 1000 questions: ~$1.60

### Optimization Tips
- Use Mock mode for testing/demos (free)
- Enable Full mode only when needed (reduces tokens)
- Clear old transactions periodically (smaller context)
- Cache common responses (future feature)

## Development

### Adding New Action Types

1. **Define action in AI response:**
```java
actions.add(new AIAction("custom_action",
    new JSONObject()
        .put("param1", "value1")
        .put("param2", value2)
));
```

2. **Handle in UI (AiPanel.java):**
```java
private void executeAction(AIAction action) {
    switch (action.getType()) {
        case "custom_action":
            handleCustomAction(action.getParameters());
            break;
        // ... other cases
    }
}
```

3. **Create UI button:**
```java
JButton actionBtn = createActionButton(
    "🎯 " + actionDescription,
    () -> executeAction(action)
);
```

### Testing AI Service

```java
// Unit test example
@Test
public void testAIResponse() {
    AIService ai = new AIService("mock", null, null);
    JSONObject context = new SummarizerService().summarizeUserData();
    
    AIService.AIResponse response = ai.askWithContext(
        "Am I over budget?",
        context
    );
    
    assertNotNull(response.getReply());
    assertTrue(response.getReply().contains("budget"));
}
```

## Future Enhancements

- [ ] Encrypted API key storage
- [ ] Response caching
- [ ] Multi-turn conversations (chat history)
- [ ] Voice input/output
- [ ] Scheduled AI reports via email
- [ ] Custom AI prompts per user
- [ ] Fine-tuned model on user's historical data

## Support

**Questions?**
- Check logs in `logs/app.log` (if enabled)
- Review example responses in Mock mode
- Test API connection independently

**Found a bug?**
- Report in issue tracker with:
  - Provider used (mock/openai/custom)
  - Question asked
  - Expected vs actual response
  - Privacy mode (minimal/full)

---

**Last Updated:** November 2025  
**Version:** 2.0.0
