package ui;

import javax.swing.*;
import java.awt.*;
import service.AIService;
import service.GeminiService;
import service.OpenRouterService;
import service.SummarizerService;
import org.json.JSONObject;
import backend.DataExportImport;
import database.AppSettingsDAO;

public class AiPanel extends JPanel implements Refreshable {
    private GeminiService geminiService;
    private OpenRouterService openRouterService;
    private SummarizerService summarizerService;
    private JTextArea questionArea;
    private JTextArea responseArea;
    private JLabel statusLabel;
    private Main mainFrame;
    private JSONObject currentContext;
    private boolean useOpenRouter = false;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public AiPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        
        // Initialize AI services
        geminiService = new GeminiService();
        summarizerService = new SummarizerService();
        
        // Check if we should use OpenRouter
        String apiKey = getApiKeyFromConfig();
        if (apiKey != null && apiKey.startsWith("sk-or-v1-")) {
            useOpenRouter = true;
            // Use LLaMA 3.3 70B - excellent quality
            openRouterService = new OpenRouterService(apiKey, "meta-llama/llama-3.3-70b-instruct:free");
        }
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
        refreshContext(); // Load initial context
    }
    
    private void initComponents() {
        // Header panel with refresh button
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BACKGROUND_COLOR);
        
        JLabel headerLabel = new JLabel("FinSight AI — Personal Finance Advisor");
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        headerLabel.setForeground(TEXT_COLOR);
        headerPanel.add(headerLabel, BorderLayout.WEST);
        
        // Status label
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(255, 193, 7));
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBackground(BACKGROUND_COLOR);
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        headerPanel.add(statusPanel, BorderLayout.CENTER);
        
        // Refresh context button
        JButton refreshBtn = createStyledButton("🔄 Refresh Context");
        refreshBtn.setBackground(new Color(33, 150, 243));
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
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(10, 0, 0, 0);
        mainPanel.add(responsePanel, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
    }
    
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
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
        questionScroll.setPreferredSize(new Dimension(0, 80));
        
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBackground(PANEL_COLOR);
        inputPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        inputPanel.add(questionScroll, BorderLayout.CENTER);
        
        // Ask button
        JButton askButton = createStyledButton("🔮 Ask AI");
        askButton.setPreferredSize(new Dimension(120, 45));
        askButton.addActionListener(e -> askAI());
        JPanel askButtonPanel = new JPanel(new BorderLayout());
        askButtonPanel.setBackground(PANEL_COLOR);
        askButtonPanel.add(askButton, BorderLayout.NORTH);
        inputPanel.add(askButtonPanel, BorderLayout.EAST);
        
        panel.add(inputPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createResponsePanel() {
        JPanel panel = new JPanel(new BorderLayout());
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
        responseScroll.getVerticalScrollBar().setBackground(PANEL_COLOR);
        
        JPanel responseContentPanel = new JPanel(new BorderLayout());
        responseContentPanel.setBackground(PANEL_COLOR);
        responseContentPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0));
        responseContentPanel.add(responseScroll, BorderLayout.CENTER);
        
        panel.add(responseContentPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private void refreshContext() {
        // WHY: Load latest financial data for AI context
        // Reload Gemini config in case API key was just added
        geminiService.reloadConfig();
        
        // Re-check which service to use
        String apiKey = getApiKeyFromConfig();
        if (apiKey != null && apiKey.startsWith("sk-or-v1-")) {
            useOpenRouter = true;
            if (openRouterService == null) {
                // Use LLaMA 3.3 70B - excellent quality
                openRouterService = new OpenRouterService(apiKey, "meta-llama/llama-3.3-70b-instruct:free");
            }
        }
        
        currentContext = summarizerService.summarizeUserData();
        
        boolean isEnabled = useOpenRouter ? 
            (openRouterService != null && openRouterService.isEnabled()) : 
            geminiService.isEnabled();
        
        if (isEnabled) {
            String keyStatus = (apiKey != null && !apiKey.isEmpty()) ? maskApiKey(apiKey) : "Built-in";
            statusLabel.setText("✅ AI Advisor Ready | Key: " + keyStatus);
            statusLabel.setForeground(ACCENT_COLOR);
        } else {
            statusLabel.setText("✅ AI Advisor Ready");
            statusLabel.setForeground(ACCENT_COLOR);
        }
        
        responseArea.setText("Financial data refreshed! Your AI Advisor now has access to your latest information.\n\n" +
            "Try asking questions like:\n" +
            "• Am I over budget this month?\n" +
            "• How can I save more money?\n" +
            "• What are my biggest expenses?\n" +
            "• Give me spending insights\n" +
            "• Should I reduce spending in any category?\n\n" +
            "Tip: For enhanced responses, add your own API key in Settings!");
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
        
        // AI is always available (either custom key or built-in)
        // No need to check configuration
        
        // Show loading state
        responseArea.setText("🤔 Analyzing your financial data...");
        statusLabel.setText("🔄 Processing your question...");
        statusLabel.setForeground(new Color(33, 150, 243));
        
        // Ask AI in background thread to avoid freezing UI
        new Thread(() -> {
            try {
                String aiResponse;
                if (useOpenRouter) {
                    aiResponse = openRouterService.ask(question, currentContext);
                } else {
                    aiResponse = geminiService.askGemini(question, currentContext);
                }
                
                // Update UI on EDT
                SwingUtilities.invokeLater(() -> {
                    responseArea.setText(aiResponse);
                    responseArea.setCaretPosition(0);
                    statusLabel.setText("✅ Response received");
                    statusLabel.setForeground(ACCENT_COLOR);
                });
                
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    responseArea.setText("❌ Error: " + ex.getMessage() + "\n\n" +
                        "Please check:\n" +
                        "1. Your internet connection\n" +
                        "2. Your API key is valid\n" +
                        "3. You haven't exceeded rate limits\n\n" +
                        "Error details: " + ex.toString());
                    statusLabel.setText("❌ Error occurred");
                    statusLabel.setForeground(new Color(220, 53, 69));
                });
                ex.printStackTrace();
            }
        }).start();
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            Color originalColor = button.getBackground();
            
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(originalColor);
            }
        });
        
        return button;
    }
    
    @Override
    public void refreshData() {
        refreshContext();
    }
    
    /**
     * Mask API key for display (show only last 4 chars)
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            return "Not Set";
        }
        if (apiKey.length() <= 4) {
            return "****";
        }
        return "****" + apiKey.substring(apiKey.length() - 4);
    }
    
    /**
     * Get API key from config for display purposes
     */
    private String getApiKeyFromConfig() {
        try {
            java.util.Properties props = new java.util.Properties();
            java.io.File configFile = new java.io.File("config.properties");
            if (configFile.exists()) {
                try (java.io.FileInputStream fis = new java.io.FileInputStream(configFile)) {
                    props.load(fis);
                    return props.getProperty("gemini_api_key", "");
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return "";
    }
}
