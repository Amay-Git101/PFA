# FinSight Quick Start Guide

## ⚡ 5-Minute Setup

### Prerequisites
- ✅ Java JDK 11 or higher
- ✅ org.json library (download if not present)

### Step 1: Download org.json Library

If you don't have `org.json` in your `lib/` directory:

```powershell
# Create lib directory if it doesn't exist
New-Item -ItemType Directory -Force -Path lib

# Download org.json
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar" -OutFile "lib/json-20231013.jar"
```

### Step 2: Compile

```powershell
# Navigate to project directory
cd C:\Users\ASUS\Desktop\commit_3\PFA

# Create bin directory
New-Item -ItemType Directory -Force -Path bin

# Compile all Java files with UTF-8 encoding
javac -encoding UTF-8 -d bin -cp "lib/*" -sourcepath src src/ui/*.java src/service/*.java src/backend/*.java src/database/*.java src/models/*.java src/events/*.java
```

**Expected output**: No errors (warnings are OK)

### Step 3: Run

```powershell
# Run the application
java -cp "bin;lib/*" ui.Main
```

**Expected result**: Application window opens with title "FinSight — Personal Finance Advisor"

---

## 🎯 Quick Test

### 1. Check Window Title
- Look at the window title bar
- Should say: **"FinSight — Personal Finance Advisor"**

### 2. Check Sidebar
- Left sidebar should show: **"FinSight"**
- Menu items: 🏠 Dashboard, 🤖 AI Advisor, etc.
- **No** `[]` or `□` characters anywhere

### 3. Test AI (Mock Mode)
1. Click **"AI Advisor"** in sidebar
2. Header should say: **"FinSight AI — Financial Advisor"**
3. Click **"🔄 Refresh Context"** button
4. Type question: **"Am I over budget?"**
5. Click **"🔮 Ask AI"**
6. Should get a data-aware response referencing your actual budget data

### 4. Check Settings
1. Click **"Settings"** in sidebar
2. Look for **"Enable AI Data Upload"** checkbox
3. About section should say **"About FinSight"**

---

## 🔧 Troubleshooting

### Error: "package org.json does not exist"
**Solution**: Download org.json library (see Step 1 above)

### Error: "unmappable character for encoding"
**Solution**: Add `-encoding UTF-8` to javac command

### Error: "class file has wrong version"
**Solution**: Check Java version with `java -version` (need 11+)

### AI doesn't respond
**Solution**: 
1. Click "Refresh Context" first
2. Make sure you have some transactions in database
3. Check console for error messages

### Export button doesn't work
**Solution**: Make sure `backend/DataExportImport.java` exists and is compiled

---

## 🚀 Advanced: OpenAI Integration

### Enable Real AI (Optional)

```powershell
# Set environment variables
$env:LLM_PROVIDER="openai"
$env:LLM_API_URL="https://api.openai.com/v1/chat/completions"
$env:LLM_API_KEY="sk-your-actual-key-here"

# Run app
java -cp "bin;lib/*" ui.Main
```

### Enable in Settings
1. Open Settings panel
2. Check **"Enable AI Data Upload"**
3. Restart AI Advisor panel
4. Now AI has access to your full transaction details

**Note**: Costs ~$0.002 per question with OpenAI

---

## 📋 Sample Questions to Ask AI

### Budget Questions
- "Am I over budget this month?"
- "How much have I spent on groceries?"
- "What percentage of my budget am I using?"

### Savings Questions
- "How can I save more money?"
- "Should I create an auto-save rule?"
- "What's a good savings target for my income?"

### Spending Questions
- "What are my biggest expenses?"
- "Where am I overspending?"
- "Show me my top expense categories"

### Action Questions
- "Export my transactions"
- "Create a savings rule"
- "Set a budget limit for dining out"

**Expected**: AI responds with specific numbers from YOUR data + action buttons appear

---

## ✅ Success Checklist

- [ ] App starts without errors
- [ ] Window title shows "FinSight — Personal Finance Advisor"
- [ ] Sidebar shows "FinSight" (no brackets)
- [ ] All panels load correctly
- [ ] AI Advisor panel opens
- [ ] "Refresh Context" button works
- [ ] Can ask questions and get responses
- [ ] Responses reference actual data
- [ ] Action buttons appear and are clickable
- [ ] Settings panel has AI toggle
- [ ] No console errors

If all checked: **🎉 You're ready to go!**

---

## 🆘 Need Help?

### Check These Files
1. **CHANGELOG.md** - What changed and why
2. **AI_README.md** - Detailed AI configuration
3. **IMPLEMENTATION_GUIDE.md** - Technical details
4. **DEPLOYMENT_SUMMARY.md** - Complete overview

### Common Issues

**Q: App won't start**  
A: Check Java version (`java -version`) and ensure org.json is in lib/

**Q: AI says "features are disabled"**  
A: It's in mock mode (this is normal). Check Settings toggle if you want full mode.

**Q: Can't see action buttons**  
A: Ask questions that imply actions like "export my data" or "help me save"

**Q: Where are the CSV exports?**  
A: Look for files named `financial_backup_YYYYMMDD_HHMMSS.csv` in app directory

---

## 📊 File Structure Check

Your directory should look like this:

```
PFA/
├── src/
│   ├── ui/
│   │   ├── Main.java ✅ (modified)
│   │   ├── AiPanel.java ✅ (rewritten)
│   │   ├── SettingsPanel.java ✅ (modified)
│   │   ├── DashboardPanel.java ✅ (modified)
│   │   └── ... (other UI files)
│   ├── service/
│   │   ├── AIService.java ✅ (NEW)
│   │   └── SummarizerService.java ✅ (NEW)
│   ├── backend/
│   ├── database/
│   ├── models/
│   └── events/
├── lib/
│   └── json-20231013.jar ✅ (required)
├── bin/
│   └── (compiled classes)
├── CHANGELOG.md ✅ (NEW)
├── AI_README.md ✅ (NEW)
├── IMPLEMENTATION_GUIDE.md ✅ (NEW)
├── DEPLOYMENT_SUMMARY.md ✅ (NEW)
└── QUICKSTART.md ✅ (this file)
```

---

## 🎬 Watch It Work

### Demo Flow
1. **Start App** → Clean "FinSight" branding everywhere
2. **Add Transaction** → Dashboard updates
3. **Open AI Advisor** → Click "Refresh Context"
4. **Ask**: "Am I over budget?" → Get data-aware response
5. **Action Button** → "🎯 Export Transactions" appears
6. **Click Button** → Confirm → CSV created
7. **Settings** → Toggle "Enable AI Data Upload"
8. **Ask Again** → More detailed response with transaction specifics

### Expected Time
- First compile: ~10 seconds
- App startup: ~2 seconds
- AI response: ~instant (mock mode)
- Everything works offline (no internet needed for mock AI)

---

## 🎓 Learning Path

### For Developers
1. Read **IMPLEMENTATION_GUIDE.md** - Understand the architecture
2. Review **AIService.java** - See LLM integration pattern
3. Review **SummarizerService.java** - Learn data aggregation
4. Check **AiPanel.java** - UI/Service integration example

### For Users
1. Read **AI_README.md** - Comprehensive user guide
2. Try all sample questions
3. Explore action buttons
4. Configure privacy settings

### For Examiners
1. Read **DEPLOYMENT_SUMMARY.md** - Complete overview
2. Check **CHANGELOG.md** - What changed
3. Review code quality indicators
4. Test with provided checklist

---

## 🎉 You're Done!

The app is now:
- ✅ Professionally branded
- ✅ AI-enhanced
- ✅ Privacy-aware
- ✅ Fully functional
- ✅ Well-documented

**Enjoy FinSight!** 🚀

---

**Version**: 2.0.0  
**Last Updated**: November 2025  
**Support**: Check documentation files in project root
