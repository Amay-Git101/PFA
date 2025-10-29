package ui;

import javax.swing.*;
import java.awt.*;
import backend.BudgetLogic;

public class AiPanel extends JPanel implements Refreshable {
    private BudgetLogic budgetLogic;
    private JTextArea questionArea;
    private JTextArea responseArea;
    private Main mainFrame;
    
    // Theme colors
    private static final Color BACKGROUND_COLOR = new Color(30, 30, 30);
    private static final Color PANEL_COLOR = new Color(42, 42, 42);
    private static final Color ACCENT_COLOR = new Color(0, 200, 151);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color BORDER_COLOR = new Color(60, 60, 60);
    
    public AiPanel(Main mainFrame) {
        this.mainFrame = mainFrame;
        budgetLogic = new BudgetLogic();
        
        setBackground(BACKGROUND_COLOR);
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        initComponents();
    }
    
    private void initComponents() {
        // Header
        JLabel headerLabel = new JLabel("🤖 AI Financial Advisor");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        headerLabel.setForeground(TEXT_COLOR);
        add(headerLabel, BorderLayout.NORTH);
        
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
        
        // Show initial recommendations
        showPersonalizedRecommendations();
    }
    
    private JPanel createQuestionPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(PANEL_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        // Title
        JLabel titleLabel = new JLabel("💬 Ask Your AI Financial Advisor");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Question input
        questionArea = new JTextArea(3, 0);
        questionArea.setBackground(PANEL_COLOR.brighter());
        questionArea.setForeground(TEXT_COLOR);
        questionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        questionArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        questionArea.setLineWrap(true);
        questionArea.setWrapStyleWord(true);
        questionArea.setText("How can I improve my spending habits?");
        
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
        askButton.addActionListener(e -> generateAIResponse());
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
        JLabel titleLabel = new JLabel("🎯 AI Recommendations");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(TEXT_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        
        // Response area
        responseArea = new JTextArea();
        responseArea.setBackground(BACKGROUND_COLOR);
        responseArea.setForeground(TEXT_COLOR);
        responseArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        responseArea.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        responseArea.setLineWrap(true);
        responseArea.setWrapStyleWord(true);
        responseArea.setEditable(false);
        
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
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(TEXT_COLOR);
        button.setBackground(ACCENT_COLOR);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                button.setBackground(ACCENT_COLOR);
            }
        });
        
        return button;
    }
    
    private void generateAIResponse() {
        String question = questionArea.getText().trim().toLowerCase();
        String response;
        
        // Simple AI responses based on keywords
        if (question.contains("save") || question.contains("saving")) {
            response = """
                💡 SAVING STRATEGIES:
                
                • Set up automatic transfers to savings account
                • Follow the 50/30/20 rule (needs/wants/savings)
                • Use the envelope method for discretionary spending
                • Try the 52-week savings challenge
                • Consider high-yield savings accounts for better returns
                
                🎯 Start small with just $25/week and gradually increase!
                """;
        } else if (question.contains("budget") || question.contains("spending")) {
            String[] recommendations = budgetLogic.generateAIRecommendations();
            response = "🎯 PERSONALIZED BUDGET ADVICE:\\n\\n" +
                      "• " + recommendations[0] + "\\n\\n" +
                      "• " + recommendations[1] + "\\n\\n" +
                      "• " + recommendations[2] + "\\n\\n" +
                      "💡 Remember: Small consistent changes lead to big results!";
        } else if (question.contains("debt") || question.contains("loan")) {
            response = """
                📉 DEBT MANAGEMENT TIPS:
                
                • List all debts by interest rate (highest first)
                • Use the avalanche method: pay minimums on all, extra on highest rate
                • Consider debt consolidation if it reduces overall interest
                • Avoid taking new debt while paying off existing debt
                • Negotiate with creditors for better payment terms
                
                🚀 Focus on one debt at a time for maximum impact!
                """;
        } else if (question.contains("invest") || question.contains("investment")) {
            response = """
                📈 INVESTMENT GUIDANCE:
                
                • Start with an emergency fund (3-6 months expenses)
                • Consider low-cost index funds for beginners
                • Diversify across different asset classes
                • Invest regularly regardless of market conditions (dollar-cost averaging)
                • Don't invest money you'll need within 5 years
                
                ⚠️ Always do your research and consider consulting a financial advisor!
                """;
        } else {
            // Default personalized response
            String[] recommendations = budgetLogic.generateAIRecommendations();
            response = "🤖 PERSONALIZED FINANCIAL ADVICE:\\n\\n" +
                      recommendations[0] + "\\n\\n" +
                      recommendations[1] + "\\n\\n" +
                      recommendations[2] + "\\n\\n" +
                      "💼 Based on your current spending patterns and budget status.\\n\\n" +
                      "💡 Tip: Ask me specific questions about saving, budgeting, debt, or investing!";
        }
        
        responseArea.setText(response);
        responseArea.setCaretPosition(0); // Scroll to top
    }
    
    private void showPersonalizedRecommendations() {
        String[] recommendations = budgetLogic.generateAIRecommendations();
        String initialResponse = """
            🎉 WELCOME TO YOUR AI FINANCIAL ADVISOR!
            
            Based on your current financial data, here are my recommendations:
            
            """ + 
            "• " + recommendations[0] + "\n\n" +
            "• " + recommendations[1] + "\n\n" +
            "• " + recommendations[2] + "\n\n" +
            """
            🤖 Ask me anything about:
            • Budgeting and saving strategies
            • Debt management
            • Investment advice
            • Spending optimization
            
            Type your question above and click 'Ask AI'!
            """;
        
        responseArea.setText(initialResponse);
    }
    
    @Override
    public void refreshData() {
        showPersonalizedRecommendations();
    }
}
